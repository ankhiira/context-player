package com.gabchmel.contextmusicplayer.data.service

import android.Manifest
import android.annotation.SuppressLint
import android.app.Notification
import android.content.*
import android.content.pm.PackageManager
import android.graphics.BitmapFactory
import android.media.*
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.IBinder
import android.support.v4.media.MediaBrowserCompat
import android.support.v4.media.MediaMetadataCompat
import android.support.v4.media.session.MediaSessionCompat
import android.support.v4.media.session.PlaybackStateCompat
import android.util.Log
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationManagerCompat
import androidx.media.MediaBrowserServiceCompat
import androidx.media.session.MediaButtonReceiver
import com.gabchmel.common.data.LocalBinder
import com.gabchmel.contextmusicplayer.R
import com.gabchmel.contextmusicplayer.data.local.LocalSongsRetriever
import com.gabchmel.contextmusicplayer.data.model.Song
import com.gabchmel.contextmusicplayer.ui.notifications.PlaybackNotificationCreator
import com.gabchmel.contextmusicplayer.utils.*
import com.gabchmel.sensorprocessor.data.service.SensorDataProcessingService
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlin.concurrent.fixedRateTimer
import kotlin.coroutines.suspendCoroutine


@Suppress("DEPRECATION")
class MediaPlaybackService : MediaBrowserServiceCompat() {

    private lateinit var mediaSession: MediaSessionCompat
    private lateinit var audioFocusRequest: AudioFocusRequest
    private lateinit var notification: Notification
    private lateinit var headsetPlugReceiver: BroadcastReceiver

    private var player = MediaPlayer()

    var audioFocusGranted = 0

    private var isBecomingNoisyRegistered = false
    private var isSongPredicted = false
    var isBound = false
    var isPlaying = false

    // Update metadata
    private val metadataRetriever = MediaMetadataRetriever()
    private val myNoisyAudioStreamReceiver = BecomingNoisyReceiver()

    private val sensorDataProcessingService = MutableStateFlow<SensorDataProcessingService?>(null)

    private val binder = object : LocalBinder<MediaPlaybackService>() {
        override fun getService() = this@MediaPlaybackService
    }

    /** Defines callbacks for service binding, passed to bindService()  */
    private val connection = object : ServiceConnection {

        @Suppress("UNCHECKED_CAST")
        override fun onServiceConnected(className: ComponentName, service: IBinder) {
            // We've bound to SensorProcessService, cast the IBinder and get SensorProcessService instance
            val binder = service as LocalBinder<SensorDataProcessingService>
            sensorDataProcessingService.value = binder.getService()
            isBound = true
        }

        override fun onServiceDisconnected(arg0: ComponentName) {
            isBound = false
        }

        override fun onBindingDied(name: ComponentName?) {
            isBound = false
        }

        override fun onNullBinding(name: ComponentName?) {
            isBound = false
        }
    }

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

    val songs = MutableStateFlow(emptyList<Song>())

    // URI of current song played
    val currentSongUri = MutableStateFlow<Uri?>(null)

    // retrieve index of currently played song
    private val currSongIndex = currentSongUri.filterNotNull().map { uri ->
        songs.value.indexOfFirst { song ->
            song.uri == uri
        }
    }.stateIn(CoroutineScope(Dispatchers.Default), SharingStarted.Eagerly, null)
    private val currentSong = currSongIndex.filterNotNull().map { index ->
        songs.value.getOrNull(index)
    }.stateIn(CoroutineScope(Dispatchers.Default), SharingStarted.Eagerly, null)
    val nextSong = currSongIndex.filterNotNull().map { index ->
        songs.value.getOrNull(index + 1)
    }.stateIn(CoroutineScope(Dispatchers.Default), SharingStarted.Eagerly, null)
    val prevSong = currSongIndex.filterNotNull().map { index ->
        songs.value.getOrNull(index - 1)
    }.stateIn(CoroutineScope(Dispatchers.Default), SharingStarted.Eagerly, null)

    // On Service bind
    override fun onBind(intent: Intent): IBinder? {
        if (intent.getBooleanExtra("is_binding", false)) {
            return binder
        }
        return super.onBind(intent)
    }

    override fun onCreate() {
        super.onCreate()

        // Load list of songs from local storage
        loadSongs()

        // Bind to SensorProcessService to later write to the file
        this.bindService(
            Intent(this, SensorDataProcessingService::class.java),
            connection,
            Context.BIND_AUTO_CREATE
        )

        registerReceiver(
            myNoisyAudioStreamReceiver,
            IntentFilter(AudioManager.ACTION_AUDIO_BECOMING_NOISY)
        )

        isBecomingNoisyRegistered = true

        val afChangeListener: AudioManager.OnAudioFocusChangeListener =
            AudioManager.OnAudioFocusChangeListener { }

        // Set the audio focus
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
        }

        // Set the volume level on headphones plugged in
        headsetPlugReceiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context?, intent: Intent) {
                val action = intent.action
                if (Intent.ACTION_HEADSET_PLUG == action) {
                    val headphonesPluggedIn = intent.getIntExtra("state", -1)
                    if (headphonesPluggedIn == 1) {
                        player.setVolume(0.5f, 0.5f)
                    }
                }
            }
        }

        // Register receiver of the headphones plugged in
        registerReceiver(headsetPlugReceiver, IntentFilter(Intent.ACTION_HEADSET_PLUG))

        mediaSession = MediaSessionCompat(baseContext, "MusicService")

        // Setup the mediaSession
        with(mediaSession) {
            val stateBuilder: PlaybackStateCompat.Builder = PlaybackStateCompat.Builder()
                .setActions(
                    PlaybackStateCompat.ACTION_PLAY_PAUSE or
                            PlaybackStateCompat.ACTION_PLAY
                            or PlaybackStateCompat.ACTION_PAUSE
                            or PlaybackStateCompat.ACTION_SKIP_TO_NEXT
                            or PlaybackStateCompat.ACTION_SKIP_TO_PREVIOUS
                )

            setPlaybackState(stateBuilder.build())

            // Set the mediaSession callback
            val mediaSessionCallback = object : MediaSessionCompat.Callback() {

                val audioManager =
                    applicationContext.getSystemService(Context.AUDIO_SERVICE) as AudioManager

                override fun onPlayFromUri(uri: Uri, extras: Bundle?) {
                    preparePlayer(uri)
                    currentSongUri.value = uri
                    updateMetadata()
                    onPlay()
                }

                override fun onPlay() {
                    if (isSongPredicted) {
                        currentSongUri.value?.let { preparePlayer(it) }
                    }

                    isPlaying = true

                    audioFocusGranted = when {
                        Build.VERSION.SDK_INT >= Build.VERSION_CODES.O -> {
                            // Request audio focus so only on app is playing audio at a time
                            audioManager.requestAudioFocus(audioFocusRequest)
                        }

                        else -> {
                            // Request audio focus for playback
                            audioManager.requestAudioFocus(
                                afChangeListener,
                                // Use the music stream.
                                AudioManager.STREAM_MUSIC,
                                // Request permanent focus.
                                AudioManager.AUDIOFOCUS_GAIN_TRANSIENT_MAY_DUCK
                            )
                        }
                    }

                    if (audioFocusGranted == AudioManager.AUDIOFOCUS_REQUEST_GRANTED) {
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

                    updateMetadata()
                    updateState()
                    updateNotification(isPlaying)
                    startForeground(
                        PlaybackNotificationCreator.playbackNotificationID,
                        notification
                    )

                    // register BECOME_NOISY BroadcastReceiver
                    registerReceiver(
                        myNoisyAudioStreamReceiver,
                        IntentFilter(AudioManager.ACTION_AUDIO_BECOMING_NOISY)
                    )

                    isBecomingNoisyRegistered = true
                }

                override fun onStop() {
                    isPlaying = false

                    // Check if the audio focus was requested
                    if (AudioManager.AUDIOFOCUS_REQUEST_GRANTED == audioFocusGranted) {
                        when {
                            Build.VERSION.SDK_INT >= Build.VERSION_CODES.O ->
                                audioManager.abandonAudioFocusRequest(audioFocusRequest)

                            else ->
                                audioManager.abandonAudioFocus(afChangeListener)
                        }
                    }

                    stopForeground(true)

                    // unregister BECOME_NOISY BroadcastReceiver if it was registered
                    if (isBecomingNoisyRegistered) {
                        unregisterReceiver(myNoisyAudioStreamReceiver)
                        isBecomingNoisyRegistered = false
                    }

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

                override fun onSeekTo(position: Long) {
                    player.seekTo(position.toInt())
                    updateState()
                }

                override fun onSkipToNext() {
                    nextSong.value?.let { nextSong ->
                        onPlayFromUri(nextSong.uri, null)
                    }
                }

                override fun onSkipToPrevious() {
                    prevSong.value?.let { prevSong ->
                        onPlayFromUri(prevSong.uri, null)
                    }
                }

                override fun onMediaButtonEvent(mediaButtonEvent: Intent?): Boolean {
                    return super.onMediaButtonEvent(mediaButtonEvent)
                }

                override fun onCustomAction(action: String?, extras: Bundle?) {
                    if (action.equals("skip")) {
                        extras?.let { setMetadata(extras.get("songUri") as Uri) }
                        onSkipToNext()
                    }
                }
            }

            // Set the created callback to the mediaSession
            setCallback(mediaSessionCallback)

            // Set session token, so the client activities can communicate with it
            setSessionToken(sessionToken)
        }

        // Every second update state of the playback
        fixedRateTimer(period = 1000) {
            updateState()
        }

        // Every 10 seconds write to file sensor measurements with the song ID
        fixedRateTimer(period = 10000) {
            if (isPlaying)
                currentSong.value?.title?.let { title ->
                    currentSong.value?.author?.let { author ->
                        // Create a hashCode to use it as ID of the song
                        val titleAuthor = "$title,$author".hashCode().toUInt()
                        sensorDataProcessingService.value?.writeToFile(titleAuthor.toString())
                    }
                }
        }
    }


    // Function to update playback state of the service
    fun updateState() {
        // Update playback state
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

    // Function to set metadata of the song
    fun updateMetadata() {
        // provide metadata
        val metadataBuilder: MediaMetadataCompat.Builder = MediaMetadataCompat.Builder()
            .putString(MediaMetadataCompat.METADATA_KEY_TITLE, metadataRetriever.getTitle())
            .putString(MediaMetadataCompat.METADATA_KEY_ARTIST, metadataRetriever.getArtist())
            .putString(MediaMetadataCompat.METADATA_KEY_ALBUM, metadataRetriever.getAlbum())
            .putBitmap(MediaMetadataCompat.METADATA_KEY_ALBUM_ART, metadataRetriever.getAlbumArt())

        metadataRetriever.getDuration()?.let { duration ->
            metadataBuilder.putLong(MediaMetadataCompat.METADATA_KEY_DURATION, duration)
        }

        mediaSession.setMetadata(metadataBuilder.build())
    }

    fun updateNotification(isPlaying: Boolean) {
        notification = PlaybackNotificationCreator.createNotification(
            baseContext,
            null,
            Song(
                metadataRetriever.getTitle() ?: "unknown",
                metadataRetriever.getArtist() ?: "unknown",
                metadataRetriever.getAlbumArt() ?: BitmapFactory.decodeResource(
                    resources,
                    R.raw.album_cover_clipart
                ),
                Uri.EMPTY
            ),
            isPlaying
        )

        PlaybackNotificationCreator.displayNotification(baseContext, notification)
    }

    // Function to set the metadata for a current song from URI
    fun setMetadata(songUri: Uri): MediaMetadataRetriever {
        // Set source to current song to retrieve metadata
        metadataRetriever.setDataSource(applicationContext, songUri)

        currentSongUri.value = songUri
        updateMetadata()

        isSongPredicted = true

        return metadataRetriever
    }

    // To control access to the service
    override fun onGetRoot(
        clientPackageName: String,
        clientUid: Int,
        rootHints: Bundle?
    ): BrowserRoot {
        return BrowserRoot(MY_MEDIA_ROOT_ID, null)
    }

    // For clients to be able to display offered music hierarchy
    override fun onLoadChildren(
        parentId: String,
        result: Result<MutableList<MediaBrowserCompat.MediaItem>>
    ) {
        val mediaItems = emptyList<MediaBrowserCompat.MediaItem>()
        result.sendResult(mediaItems as MutableList<MediaBrowserCompat.MediaItem>?)
    }

    @SuppressLint("ServiceCast")
    override fun onTaskRemoved(rootIntent: Intent?) {
        if (isBecomingNoisyRegistered)
            unregisterReceiver(headsetPlugReceiver)
        isBecomingNoisyRegistered = false

        val notificationManager =
            applicationContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManagerCompat
        notificationManager.cancelAll()

        player.stop()
        stopForeground(true)
        stopSelf()
        super.onTaskRemoved(rootIntent)
    }

    override fun onDestroy() {
        // unregister BECOME_NOISY and headphones plugged in BroadcastReceiver
        if (isBecomingNoisyRegistered)
            unregisterReceiver(myNoisyAudioStreamReceiver)
        unregisterReceiver(headsetPlugReceiver)
        isBecomingNoisyRegistered = false

        player.run {
            seekTo(0)
            stop()
        }

        if (isBound) {
            try {
                this.applicationContext.unbindService(connection)
            } catch (e: Exception) {
                Log.e("Exception", e.toString())
            }
        }

        mediaSession.run {
            isActive = false
            release()
        }

        stopForeground(true)
        stopSelf()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        MediaButtonReceiver.handleIntent(mediaSession, intent)
        return super.onStartCommand(intent, flags, startId)
    }

    private val permission =
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU)
            Manifest.permission.READ_MEDIA_AUDIO
        else
            Manifest.permission.READ_EXTERNAL_STORAGE

    // Load songs from local storage
    fun loadSongs() {
        if (ActivityCompat.checkSelfPermission(
                baseContext,
                permission
            ) != PackageManager.PERMISSION_GRANTED
        ) return

        CoroutineScope(Dispatchers.Default).launch {
            songs.value = LocalSongsRetriever.loadLocalStorageSongs(baseContext)
        }
    }

    // Prepare the player for the song play
    fun preparePlayer(uri: Uri) {
        val songUri = Uri.parse(uri.toString())

        player.run {
            reset()
            setDataSource(baseContext, songUri)
            prepare()
        }

        // Set source to current song to retrieve metadata
        metadataRetriever.setDataSource(applicationContext, songUri)
    }

    companion object {
        private const val MY_MEDIA_ROOT_ID = "/"

        suspend fun getInstance(context: Context) = suspendCoroutine<MediaPlaybackService> { cont ->
            val intent = Intent(context, MediaPlaybackService::class.java)
            intent.putExtra("is_binding", true)

            @Suppress("UNCHECKED_CAST")
            context.bindService(intent, object : ServiceConnection {
                override fun onServiceConnected(name: ComponentName?, service: IBinder) {
                    cont.resumeWith(kotlin.Result.success((service as LocalBinder<MediaPlaybackService>).getService()))
                }

                override fun onServiceDisconnected(name: ComponentName?) {
                }
            }, Context.BIND_AUTO_CREATE)
        }
    }
}