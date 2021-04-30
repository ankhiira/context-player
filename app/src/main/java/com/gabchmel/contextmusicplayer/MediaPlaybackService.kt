package com.gabchmel.contextmusicplayer

import android.Manifest
import android.app.Notification
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.graphics.BitmapFactory
import android.media.*
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.support.v4.media.MediaBrowserCompat
import android.support.v4.media.MediaMetadataCompat
import android.support.v4.media.session.MediaSessionCompat
import android.support.v4.media.session.PlaybackStateCompat
import androidx.annotation.RequiresApi
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.core.app.ActivityCompat
import androidx.media.MediaBrowserServiceCompat
import androidx.media.session.MediaButtonReceiver
import com.gabchmel.contextmusicplayer.playlistScreen.Song
import com.gabchmel.contextmusicplayer.playlistScreen.SongScanner
import java.util.*
import kotlin.concurrent.fixedRateTimer


class MediaPlaybackService : MediaBrowserServiceCompat() {

    companion object {
        private const val MY_MEDIA_ROOT_ID = "media_root_id"
    }

    private lateinit var mediaSession: MediaSessionCompat
    private lateinit var stateBuilder: PlaybackStateCompat.Builder
    private lateinit var metadataBuilder: MediaMetadataCompat.Builder

    private lateinit var audioFocusRequest: AudioFocusRequest

//    private lateinit var player: MediaPlayer
    private var player = MediaPlayer()

    private lateinit var notification: Notification

    private lateinit var timer: Timer

    // class to detect BECOMING_NOISY broadcast
    private inner class BecomingNoisyReceiver : BroadcastReceiver() {

        override fun onReceive(context: Context, intent: Intent) {
            if (intent.action == AudioManager.ACTION_AUDIO_BECOMING_NOISY) {
                // Pause the playback
                if(player.isPlaying) {
                    player.pause()
                    updateState()
                }
            }
        }
    }

    private val myNoisyAudioStreamReceiver = BecomingNoisyReceiver()

    // Update metadata
    private val metadataRetriever = MediaMetadataRetriever()

    private var songs by mutableStateOf(emptyList<Song>())

    @RequiresApi(Build.VERSION_CODES.Q)
    override fun onCreate() {
        super.onCreate()

        loadSongs()

        // Create MediaPlayer with the given URI
//        player = MediaPlayer.create(
//            baseContext,
//            uri
//        )

        // If the player is MediaPlayer, set OnCompletionListener
        if (player == MediaPlayer()) {
            // After the song finishes go to the initial design
            player.setOnCompletionListener {
                // TODO completion listener
            }
        }

        // Create and initialize MediaSessionCompat
        mediaSession = MediaSessionCompat(baseContext, "MusicService")
            .apply {

                // deprecated
                // Support BT headphones
                setFlags(
                    MediaSessionCompat.FLAG_HANDLES_MEDIA_BUTTONS
                            // support Android Wear, Android Auto
                            or MediaSessionCompat.FLAG_HANDLES_TRANSPORT_CONTROLS
                )

                // Set of initial PlaybackState set to ACTION_PLAY, so the media buttons can start the player
                // (current operational state of the player - transport state - playing, paused, buffering)
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

                        onPlay()
                    }

                    override fun onPlay() {

                        val result: Int

                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {

                            audioFocusRequest =
                                AudioFocusRequest.Builder(AudioManager.AUDIOFOCUS_GAIN).run {
                                    setOnAudioFocusChangeListener(afChangeListener)
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

                        player.pause()

                        updateState()

                        updateNotification(false)

                        // Take the service out of foreground, keep the notification
                        stopForeground(false)

                        // unregister BECOME_NOISY BroadcastReceiver
                        unregisterReceiver(myNoisyAudioStreamReceiver)
                    }

                    override fun onSeekTo(pos: Long) {
                        player.seekTo(pos.toInt())

                        updateState()
                    }

                    override fun onMediaButtonEvent(mediaButtonEvent: Intent?): Boolean {
                        return super.onMediaButtonEvent(mediaButtonEvent)
                    }
                }

                setCallback(mediaSessionCallback)

                // Set session token, so the client activities can communicate with it
                setSessionToken(sessionToken)
            }

        // Every one second update state of the playback
        timer = fixedRateTimer(period = 10000) {
            updateState()
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

        metadataBuilder = MediaMetadataCompat.Builder()
            .putString(MediaMetadataCompat.METADATA_KEY_TITLE, metadataRetriever.getTitle())
            .putString(MediaMetadataCompat.METADATA_KEY_AUTHOR, metadataRetriever.getArtist())
            .putString(MediaMetadataCompat.METADATA_KEY_ALBUM, metadataRetriever.getAlbum())

        metadataRetriever.getDuration()?.let { duration ->
            metadataBuilder.putLong(MediaMetadataCompat.METADATA_KEY_DURATION, duration)
        }
        mediaSession.setMetadata(metadataBuilder.build())
    }

    fun updateNotification(isPlaying: Boolean) {
        // Update notification
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

        player.stop()
        player.seekTo(0)

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

    @RequiresApi(Build.VERSION_CODES.Q)
    fun loadSongs() {
        if (ActivityCompat.checkSelfPermission(
                baseContext,
                Manifest.permission.READ_EXTERNAL_STORAGE
            ) != PackageManager.PERMISSION_GRANTED
        ) return

        songs = SongScanner.loadSongs(baseContext)
    }

    fun preparePlayer(uri: Uri) {
        val songUri = Uri.parse(uri.toString())

        player.setDataSource(baseContext, songUri)

        player.prepare()

        // Set source to current song
        metadataRetriever.setDataSource(applicationContext, songUri)
    }
}