package com.gabchmel.contextmusicplayer

import android.Manifest
import android.app.Notification
import android.content.*
import android.content.pm.PackageManager
import android.graphics.BitmapFactory
import android.media.*
import android.net.Uri
import android.os.Binder
import android.os.Build
import android.os.Bundle
import android.os.IBinder
import android.support.v4.media.MediaBrowserCompat
import android.support.v4.media.MediaMetadataCompat
import android.support.v4.media.session.MediaSessionCompat
import android.support.v4.media.session.PlaybackStateCompat
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.media.MediaBrowserServiceCompat
import androidx.media.session.MediaButtonReceiver
import com.gabchmel.contextmusicplayer.extensions.*
import com.gabchmel.contextmusicplayer.playlistScreen.Song
import com.gabchmel.contextmusicplayer.playlistScreen.SongScanner
import com.gabchmel.sensorprocessor.LocalBinder
import com.gabchmel.sensorprocessor.SensorProcessService
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.flow.*
import java.util.*
import kotlin.concurrent.fixedRateTimer
import kotlin.coroutines.suspendCoroutine


class MediaPlaybackService : MediaBrowserServiceCompat() {

    companion object {
        private const val MY_MEDIA_ROOT_ID = "/"

        suspend fun getInstance(context: Context) = suspendCoroutine<MediaPlaybackService> { cont ->
            val intent = Intent(context, MediaPlaybackService::class.java)
            intent.putExtra("is_binding", true)

            context.bindService(intent, object : ServiceConnection {
                override fun onServiceConnected(name: ComponentName?, service: IBinder) {
                    cont.resumeWith(kotlin.Result.success((service as MediaBinder).getService()))
                }

                override fun onServiceDisconnected(name: ComponentName?) {
                }
            }, Context.BIND_AUTO_CREATE)
        }
    }

    private lateinit var mediaSession: MediaSessionCompat
    private lateinit var stateBuilder: PlaybackStateCompat.Builder
    private lateinit var metadataBuilder: MediaMetadataCompat.Builder

    private lateinit var audioFocusRequest: AudioFocusRequest

    private var player = MediaPlayer()

    private lateinit var notification: Notification

    private lateinit var timer: Timer

    private lateinit var broadcastReceiver: BroadcastReceiver

    private val binder = MediaBinder()

    // Binder to a service
    inner class MediaBinder : Binder() {
        fun getService(): MediaPlaybackService = this@MediaPlaybackService
    }

    private var sensorProcessService = MutableStateFlow<SensorProcessService?>(null)

    /** Defines callbacks for service binding, passed to bindService()  */
    private val connection = object : ServiceConnection {

        override fun onServiceConnected(className: ComponentName, service: IBinder) {
            // We've bound to SensorProcessService, cast the IBinder and get SensorProcessService instance
            val binder = service as LocalBinder<SensorProcessService>
            sensorProcessService.value = binder.getService()
        }

        override fun onServiceDisconnected(arg0: ComponentName) {
        }
    }

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

    private val myNoisyAudioStreamReceiver = BecomingNoisyReceiver()

    // Update metadata
    private val metadataRetriever = MediaMetadataRetriever()

    var songs = MutableStateFlow(emptyList<Song>())

    // URI of current song played
    val currentSongUri = MutableStateFlow<Uri?>(null)

    // retrieve index of currently played song
    private val currSongIndex = currentSongUri.filterNotNull().map { uri ->
        songs.value.indexOfFirst { song ->
            song.URI == uri
        }
    }.stateIn(GlobalScope, SharingStarted.Eagerly, null)
    private val currentSong = currSongIndex.filterNotNull().map { index ->
        songs.value.getOrNull(index)
    }.stateIn(GlobalScope, SharingStarted.Eagerly, null)
    val nextSong = currSongIndex.filterNotNull().map { index ->
        songs.value.getOrNull(index + 1)
    }.stateIn(GlobalScope, SharingStarted.Eagerly, null)
    val prevSong = currSongIndex.filterNotNull().map { index ->
        songs.value.getOrNull(index - 1)
    }.stateIn(GlobalScope, SharingStarted.Eagerly, null)

    var isPlaying = false

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
        val intent = Intent(this, SensorProcessService::class.java)
        this.applicationContext.bindService(
            intent, connection,
            Context.BIND_AUTO_CREATE
        )

        broadcastReceiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context?, intent: Intent) {
                val action = intent.action
                if (Intent.ACTION_HEADSET_PLUG == action) {
                    val headphonesPluggedIn = intent.getIntExtra("state", -1)
                    if (headphonesPluggedIn == 0) {
                        Toast.makeText(
                            applicationContext,
                            "Headphones not plugged in",
                            Toast.LENGTH_LONG
                        ).show()
                    } else if (headphonesPluggedIn == 1) {
                        player.setVolume(0.5f,0.5f)
                        Toast.makeText(
                            applicationContext,
                            "Headphones plugged in",
                            Toast.LENGTH_LONG
                        ).show()
                    }
                }
            }
        }

        val receiverFilter = IntentFilter(Intent.ACTION_HEADSET_PLUG)
        registerReceiver(broadcastReceiver, receiverFilter)

        // Create and initialize MediaSessionCompat
        mediaSession = MediaSessionCompat(baseContext, "MusicService")
            .apply {

                // Set of initial PlaybackState set to ACTION_PLAY, so the media buttons can start the player
                // (current operational state of the player - transport state - playing, paused)
                stateBuilder = PlaybackStateCompat.Builder()
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

                        // unregister BECOME_NOISY BroadcastReceiver
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

                // Set session token, so the client activities can communicate with it
                setSessionToken(sessionToken)
            }

        // Every second update state of the playback
        timer = fixedRateTimer(period = 1000) {
            updateState()
        }

        val prediction = sensorProcessService.value?.prediction?.value

        if(prediction != null) {
            for (song in songs.value) {
                if("$song.title,$song.author".hashCode().toUInt().toString() == prediction) {

                }
            }
        }

        // Every 10 second write to file sensor measurements with the song ID
        fixedRateTimer(period = 10000) {
            if (isPlaying)
                currentSong.value?.title?.let { title ->
                    currentSong.value?.author?.let { author ->
                        // Create a hashCode to use it as ID of the song
                        val titleAuthor = "$title,$author".hashCode().toUInt()
                        sensorProcessService.value?.writeToFile(titleAuthor.toString())
                    }
                }
        }
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

    // Function to set metadata of the song
    fun updateMetadata() {
        // provide metadata
        metadataBuilder = MediaMetadataCompat.Builder()
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
            mediaSession.sessionToken,
            metadataRetriever.getTitle() ?: "unknown",
            metadataRetriever.getArtist() ?: "unknown",
            metadataRetriever.getAlbumArt() ?: BitmapFactory.decodeResource(
                resources,
                R.raw.album_cover_clipart
            ),
            isPlaying
        )

        NotificationManager.displayNotification(baseContext, notification)
    }

    // controls access to the service
    override fun onGetRoot(
        clientPackageName: String,
        clientUid: Int,
        rootHints: Bundle?
    ): BrowserRoot {
        return BrowserRoot(MY_MEDIA_ROOT_ID, null)
    }

    // client can build and display menu from MediaBrowserService's content hierarchy
    override fun onLoadChildren(
        parentId: String,
        result: Result<MutableList<MediaBrowserCompat.MediaItem>>
    ) {

        val mediaItems = emptyList<MediaBrowserCompat.MediaItem>()

        result.sendResult(mediaItems as MutableList<MediaBrowserCompat.MediaItem>?)
    }

    override fun onTaskRemoved(rootIntent: Intent?) {

        player.stop()
        stopSelf()

        super.onTaskRemoved(rootIntent)
    }

    override fun onDestroy() {

        // unregister BECOME_NOISY BroadcastReceiver
        unregisterReceiver(myNoisyAudioStreamReceiver)

        player.stop()
        player.seekTo(0)

        this.applicationContext.unbindService(connection)

        stopSelf()

        mediaSession.run {
            isActive = false
            release()
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        MediaButtonReceiver.handleIntent(mediaSession, intent)
        return super.onStartCommand(intent, flags, startId)
    }

    // Load songs from local storage
    fun loadSongs() {
        if (ActivityCompat.checkSelfPermission(
                baseContext,
                Manifest.permission.READ_EXTERNAL_STORAGE
            ) != PackageManager.PERMISSION_GRANTED
        ) return

        songs.value = SongScanner.loadSongs(baseContext)
    }

    fun preparePlayer(uri: Uri) {
        val songUri = Uri.parse(uri.toString())

        player.reset()

        player.setDataSource(baseContext, songUri)

        player.prepare()

        // Set source to current song
        metadataRetriever.setDataSource(applicationContext, songUri)
    }
}