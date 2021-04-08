package com.gabchmel.contextmusicplayer

import android.app.Notification
import android.content.Context
import android.content.Intent
import android.graphics.BitmapFactory
import android.media.*
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.support.v4.media.MediaBrowserCompat
import android.support.v4.media.MediaMetadataCompat
import android.support.v4.media.session.MediaSessionCompat
import android.support.v4.media.session.PlaybackStateCompat
import androidx.media.MediaBrowserServiceCompat
import androidx.media.session.MediaButtonReceiver
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
    private lateinit var player: MediaPlayer

    private lateinit var notification: Notification

    private lateinit var timer: Timer

//    private val noisyAudioStreamReceiver = BroadcastReceiver()

    // Update metadata
    private val metadataRetriever = MediaMetadataRetriever()

    private var uri = Uri.parse("android.resource://com.gabchmel.contextmusicplayer/" + R.raw.gaga)
//            Uri.parse("https://files.freemusicarchive.org/storage-freemusicarchive-org/music/no_curator/First_Rebirth/Last_Runaway/First_Rebirth_-_01_-_Prisoner_Of_Infinity.mp3?download=1&name=First%20Rebirth%20-%20Prisoner%20Of%20Infinity.mp3")

    override fun onCreate() {
        super.onCreate()

        player = MediaPlayer.create(
            baseContext,
            uri
        )

        metadataRetriever.setDataSource(applicationContext, uri)

        if (player == MediaPlayer()) {
            // After the song finishes go to the initial design
            player.setOnCompletionListener {
                // TODO
            }
        }

        // Create and initialize MediaSessionCompat
        mediaSession = MediaSessionCompat(baseContext, "MusicService")
            .apply {

//                deprecated
//                Support BT headphones
                setFlags(
                    MediaSessionCompat.FLAG_HANDLES_MEDIA_BUTTONS
//                    support Android Wear, Android Auto
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
                    }

                    override fun onStop() {

                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                            am.abandonAudioFocusRequest(audioFocusRequest)
                        } else {
                            am.abandonAudioFocus(afChangeListener)
                        }
                        stopForeground(true)

                        isActive = false
                    }

                    override fun onPause() {

                        player.pause()

                        updateState()
                        updateNotification(false)
                        // Take the service out of foreground, keep the notification
                        stopForeground(false)

//                    TODO dodelat
//                    // unregister BECOME_NOISY BroadcastReceiver
//                    unregisterReceiver(myNoisyAudioStreamReceiver)
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

        timer = fixedRateTimer(period = 10000) {
            updateState()
        }
    }

    fun updateState() {
        // Update playback state
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
}