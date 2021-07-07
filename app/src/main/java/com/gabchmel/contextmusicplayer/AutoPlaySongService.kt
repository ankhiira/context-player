package com.gabchmel.contextmusicplayer

import android.app.Notification
import android.content.*
import android.graphics.BitmapFactory
import android.media.*
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.IBinder
import android.support.v4.media.MediaBrowserCompat
import android.support.v4.media.MediaMetadataCompat
import android.support.v4.media.session.MediaControllerCompat
import android.support.v4.media.session.MediaSessionCompat
import android.support.v4.media.session.PlaybackStateCompat
import androidx.lifecycle.LifecycleService
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.whenCreated
import androidx.media.MediaBrowserServiceCompat
import com.gabchmel.common.utilities.bindService
import com.gabchmel.contextmusicplayer.extensions.*
import com.gabchmel.sensorprocessor.SensorProcessService
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

@Deprecated("SDFSDF")
class AutoPlaySongService : LifecycleService() {

    private lateinit var mediaBrowser: MediaBrowserCompat
    private var mediaBrowserConnectionCallback = MediaBrowserCompat.ConnectionCallback()

    lateinit var mediaController: MediaControllerCompat

    private val _musicState = MutableStateFlow<PlaybackStateCompat?>(null)
    val musicState: StateFlow<PlaybackStateCompat?> = _musicState

    private val _musicMetadata = MutableStateFlow<MediaMetadataCompat?>(null)
    val musicMetadata: StateFlow<MediaMetadataCompat?> = _musicMetadata

    private var player = MediaPlayer()

    // URI of current song played
    val currentSongUri = MutableStateFlow<Uri?>(null)

    // Update metadata
    private val metadataRetriever = MediaMetadataRetriever()

    var isPlaying = false

    private lateinit var audioFocusRequest: AudioFocusRequest

    private val myNoisyAudioStreamReceiver = BecomingNoisyReceiver()

    private lateinit var mediaSession: MediaSessionCompat

    // class to detect BECOMING_NOISY broadcast
    private inner class BecomingNoisyReceiver : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            if (intent.action == AudioManager.ACTION_AUDIO_BECOMING_NOISY) {
                // Pause the playback
                if (player.isPlaying) {
                    player.pause()
                    updateState()
                }
            }
        }
    }

    // retrieve index of currently played song
    private val currSongIndex = currentSongUri.filterNotNull().map { uri ->
        songs.value?.indexOfFirst { song ->
            song.URI == uri
        }
    }.stateIn(lifecycleScope, SharingStarted.Eagerly, null)
    private val currentSong = currSongIndex.filterNotNull().map { index ->
        songs.value?.getOrNull(index)
    }.stateIn(lifecycleScope, SharingStarted.Eagerly, null)
    val nextSong = currSongIndex.filterNotNull().map { index ->
        songs.value?.getOrNull(index + 1)
    }.stateIn(lifecycleScope, SharingStarted.Eagerly, null)
    val prevSong = currSongIndex.filterNotNull().map { index ->
        songs.value?.getOrNull(index - 1)
    }.stateIn(lifecycleScope, SharingStarted.Eagerly, null)

    var prediction = flow {
        val sensorProcessService = sensorProcessService.await()
        emitAll(sensorProcessService.prediction)
    }.filterNotNull()

    var notPlayed = true

    private lateinit var notification: Notification

    private val sensorProcessService = lifecycleScope.async {
        whenCreated {
            val service = this@AutoPlaySongService.bindService(SensorProcessService::class.java)

            service.createModel()
            service.triggerPrediction()

            service
        }
    }

    val service = CompletableDeferred<MediaPlaybackService>()

    private var songs = flow {
        // Service awaits for complete call
        val service = service.await()
        // Collects values from songs from services
        emitAll(service.songs)
    }.stateIn(lifecycleScope, SharingStarted.Lazily, null)

    private val connectionCallbacks = object : MediaBrowserCompat.ConnectionCallback() {
        override fun onConnected() {

            mediaBrowser.sessionToken.also { token ->

                // Create MediaControllerCompat
                mediaController = MediaControllerCompat(
                    this@AutoPlaySongService,
                    token
                )
            }

            // Display initial state
            val metadata = mediaController.metadata
            val pbstate = mediaController.playbackState

            _musicState.value = pbstate

            // Register a callback to stay in sync
            mediaController.registerCallback(controllerCallback)
        }

        override fun onConnectionSuspended() {
            super.onConnectionSuspended()
        }

        override fun onConnectionFailed() {
            super.onConnectionFailed()
        }
    }

    override fun onCreate() {
        super.onCreate()

        // Setting MediaBrowser for connecting to the MediaBrowserService
        mediaBrowser = MediaBrowserCompat(
            this,
            ComponentName(this, MediaPlaybackService::class.java),
            connectionCallbacks,
            null
        )

        // Connects to the MediaBrowseService
        mediaBrowser.connect()

        // Create and initialize MediaSessionCompat
        mediaSession = MediaSessionCompat(this@AutoPlaySongService, "MusicService")
            .apply {

                // Set of initial PlaybackState set to ACTION_PLAY, so the media buttons can start the player
                // (current operational state of the player - transport state - playing, paused)
                val stateBuilder = PlaybackStateCompat.Builder()
                    .setActions(
                        PlaybackStateCompat.ACTION_PLAY_PAUSE or
                                PlaybackStateCompat.ACTION_PLAY
                                or PlaybackStateCompat.ACTION_PAUSE
                                or PlaybackStateCompat.ACTION_SKIP_TO_NEXT
                                or PlaybackStateCompat.ACTION_SKIP_TO_PREVIOUS
                    )

                setPlaybackState(stateBuilder.build())

                val mediaSessionCallback = object : MediaSessionCompat.Callback() {

                    val am =
                        applicationContext.getSystemService(Context.AUDIO_SERVICE) as AudioManager

                    val afChangeListener: AudioManager.OnAudioFocusChangeListener =
                        AudioManager.OnAudioFocusChangeListener { }

                    override fun onPlayFromUri(uri: Uri, extras: Bundle?) {
                        preparePlayer(uri)

                        currentSongUri.value = uri

                        updateMetadata()

                        onPlay()
                    }

                    override fun onPlay() {

                        val result: Int

                        isPlaying = true

                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {

                            audioFocusRequest =
                                AudioFocusRequest.Builder(AudioManager.AUDIOFOCUS_GAIN).run {
                                    setOnAudioFocusChangeListener(afChangeListener)
                                    // Set audio stream type to music
                                    setAudioAttributes(AudioAttributes.Builder().run {
                                        setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                                        build()
                                    })
                                    build()
                                }

                            result = am.requestAudioFocus(audioFocusRequest)
                        } else {

                            // Request audio focus for playback
                            result = am.requestAudioFocus(
                                afChangeListener,
                                // Use the music stream.
                                AudioManager.STREAM_MUSIC,
                                // Request permanent focus.
                                AudioManager.AUDIOFOCUS_GAIN_TRANSIENT_MAY_DUCK
                            )
                        }

                        if (result == AudioManager.AUDIOFOCUS_REQUEST_GRANTED) {
                            startService(
                                Intent(
                                    applicationContext,
                                    MediaBrowserServiceCompat::class.java
                                )
                            )
                        }

                        // Set session active, set to use media buttons now
                        isActive = true

                        player.setOnCompletionListener {
                            updateState()
                        }

                        // Start the player
                        player.start()

                        updateState()
                        updateNotification(true)
                        startForeground(NotificationManager.notificationID, notification)

                        // register BECOME_NOISY BroadcastReceiver
                        registerReceiver(
                            myNoisyAudioStreamReceiver,
                            IntentFilter(AudioManager.ACTION_AUDIO_BECOMING_NOISY)
                        )
                    }

                    override fun onStop() {

                        isPlaying = false

                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                            am.abandonAudioFocusRequest(audioFocusRequest)
                        } else {
                            am.abandonAudioFocus(afChangeListener)
                        }
                        stopForeground(true)

                        // unregister BECOME_NOISY BroadcastReceiver if it was registered
                        unregisterReceiver(myNoisyAudioStreamReceiver)

                        isActive = false
                    }

                    override fun onPause() {

                        isPlaying = false

                        player.pause()

                        updateState()

                        updateNotification(false)

                        // Take the service out of foreground, keep the notification
                        stopForeground(false)
                    }

                    override fun onSeekTo(pos: Long) {
                        player.seekTo(pos.toInt())

                        updateState()
                    }

                    override fun onSkipToNext() {
                        nextSong.value?.let { nextSong ->
                            onPlayFromUri(nextSong.URI, null)
                        }
                    }

                    override fun onSkipToPrevious() {
                        prevSong.value?.let { prevSong ->
                            onPlayFromUri(prevSong.URI, null)
                        }
                    }

                    override fun onMediaButtonEvent(mediaButtonEvent: Intent?): Boolean {
                        return super.onMediaButtonEvent(mediaButtonEvent)
                    }
                }

                setCallback(mediaSessionCallback)

//        // Set session token, so the client activities can communicate with it
//        setSessionToken(sessionToken)
            }

        playSong()
    }

    override fun onBind(intent: Intent): IBinder? {
        return super.onBind(intent)
    }

    override fun onDestroy() {
        super.onDestroy()

        mediaController.unregisterCallback(controllerCallback)
        mediaBrowserConnectionCallback.onConnectionSuspended()
        mediaBrowser.disconnect()
    }

    private var controllerCallback = object : MediaControllerCompat.Callback() {

        override fun onMetadataChanged(metadata: MediaMetadataCompat) {
            _musicMetadata.value = metadata
        }

        override fun onPlaybackStateChanged(state: PlaybackStateCompat) {
            _musicState.value = state
        }
    }

    // To play for the first time
    fun play(uri: Uri) {
        mediaController.transportControls.playFromUri(uri, null)
    }

    // To play any other time
    fun play() {
        mediaController.transportControls.play()
    }

    fun pause() {
        mediaController.transportControls.pause()
    }

    fun next() {
        mediaController.transportControls.skipToNext()
    }

    fun prev() {
        mediaController.transportControls.skipToPrevious()
    }

    fun setMusicProgress(progress: Float) {
        mediaController.transportControls.seekTo(progress.toLong())
    }

    // Function to set metadata of the song
    fun updateMetadata() {

        // provide metadata
        val metadataBuilder: MediaMetadataCompat.Builder = MediaMetadataCompat.Builder()
            .putString(MediaMetadataCompat.METADATA_KEY_TITLE, metadataRetriever.getTitle())
            .putString(MediaMetadataCompat.METADATA_KEY_ARTIST, metadataRetriever.getArtist())
            .putString(MediaMetadataCompat.METADATA_KEY_ALBUM, metadataRetriever.getAlbum())
            .putBitmap(MediaMetadataCompat.METADATA_KEY_ALBUM_ART, metadataRetriever.getAlbumArt())

        // set song duration
        metadataRetriever.getDuration()?.let { duration ->
            metadataBuilder.putLong(MediaMetadataCompat.METADATA_KEY_DURATION, duration)
        }

        mediaSession.setMetadata(metadataBuilder.build())
    }

    // Function to update a notification
    fun updateNotification(isPlaying: Boolean) {
        // Recreate notification
        notification = NotificationManager.createNotification(
            baseContext,
            null,
            _musicMetadata.value?.getTitle() ?: "unknown",
            _musicMetadata.value?.getArtist() ?: "unknown",
            _musicMetadata.value?.getAlbumArt() ?: BitmapFactory.decodeResource(
                resources,
                R.raw.album_cover_clipart
            ),
            isPlaying
        )

        NotificationManager.displayNotification(baseContext, notification)
    }

    fun preparePlayer(uri: Uri) {
        val songUri = Uri.parse(uri.toString())

        player.reset()

        player.setDataSource(baseContext, songUri)

        player.prepare()

        // Set source to current song
        metadataRetriever.setDataSource(applicationContext, songUri)
    }

    // Function to update playback state of the service
    fun updateState() {
        // Update playback state
        // TODO update playback state
//        val newPlaybackState = PlaybackStateCompat.Builder(mediaSession)
        mediaSession.setPlaybackState(
            player.toPlaybackStateBuilder().setActions(
                PlaybackStateCompat.ACTION_PLAY_PAUSE or
                        PlaybackStateCompat.ACTION_PLAY
                        or PlaybackStateCompat.ACTION_PAUSE
                        or PlaybackStateCompat.ACTION_SKIP_TO_NEXT
                        or PlaybackStateCompat.ACTION_SKIP_TO_PREVIOUS
                        or PlaybackStateCompat.ACTION_STOP
            ).build()
        )
    }

    fun playSong() {
        // Check predictions
        lifecycleScope.launch {

            launch {
                prediction.collectLatest { prediction ->
                    for (song in songs.filterNotNull().first()) {
                        if ("${song.title},${song.author}".hashCode().toUInt().toString() == prediction) {
                            metadataRetriever.setDataSource(applicationContext, song.URI)
                            updateNotification(false)
                                play(song.URI)
                            notPlayed = false
                        }
                    }
                }
            }

            service.complete(MediaPlaybackService.getInstance(this@AutoPlaySongService))
        }
    }
}