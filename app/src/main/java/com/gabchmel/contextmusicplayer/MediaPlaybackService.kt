package com.gabchmel.contextmusicplayer

import android.content.Context
import android.content.Intent
import android.media.AudioAttributes
import android.media.AudioFocusRequest
import android.media.AudioManager
import android.media.MediaPlayer
import android.media.session.PlaybackState
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.support.v4.media.MediaBrowserCompat
import android.support.v4.media.MediaMetadataCompat
import android.support.v4.media.session.MediaSessionCompat
import android.support.v4.media.session.PlaybackStateCompat
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.media.AudioManagerCompat
import androidx.media.MediaBrowserServiceCompat
import androidx.media.session.MediaButtonReceiver
import com.gabchmel.contextmusicplayer.homeScreen.HomeFragment
import com.google.android.exoplayer2.ext.mediasession.MediaSessionConnector
import android.media.session.MediaSession




private const val MY_MEDIA_ROOT_ID = "media_root_id"


class MediaPlaybackService : MediaBrowserServiceCompat() {

    private lateinit var mediaSession : MediaSessionCompat
    private lateinit var mediaSessionConnector: MediaSessionConnector

    private lateinit var stateBuilder : PlaybackStateCompat.Builder
    private lateinit var notificationManager : NotificationManager
    private lateinit var mediaSource: MediaMetadataCompat
    private lateinit var audioFocusRequest: AudioFocusRequest
    private lateinit var afChangeListener: AudioManager.OnAudioFocusChangeListener
    private lateinit var player: MediaPlayer
    private lateinit var service: MediaBrowserServiceCompat

    override fun onCreate() {
        super.onCreate()

        player = MediaPlayer.create(
            baseContext,
//            Uri.parse("https://files.freemusicarchive.org/storage-freemusicarchive-org/music/no_curator/First_Rebirth/Last_Runaway/First_Rebirth_-_01_-_Prisoner_Of_Infinity.mp3?download=1&name=First%20Rebirth%20-%20Prisoner%20Of%20Infinity.mp3")
            Uri.parse("android.resource://com.gabchmel.contextmusicplayer/" + R.raw.gaga)
        )

        // Create and initialize MediaSessionCompat
        mediaSession = MediaSessionCompat(baseContext, "MusicService")
            .apply {

//                deprecated
//                Support BT headphones
            setFlags(MediaSessionCompat.FLAG_HANDLES_MEDIA_BUTTONS
//                    support Android Wear, Android Auto
            or MediaSessionCompat.FLAG_HANDLES_TRANSPORT_CONTROLS)

            // Set of initial PlaybackState set to ACTION_PLAY, so the media buttons can start the player
            // (current operational state of the player - transport state - playing, paused, buffering)
            stateBuilder = PlaybackStateCompat.Builder()
                .setActions(PlaybackStateCompat.ACTION_PLAY
                or PlaybackStateCompat.ACTION_PLAY_PAUSE
                or PlaybackStateCompat.ACTION_PAUSE
                or PlaybackStateCompat.ACTION_SKIP_TO_NEXT
                or PlaybackStateCompat.ACTION_SKIP_TO_PREVIOUS)

            setPlaybackState(stateBuilder.build())

//            val controller = mediaSession.controller

            val mediaSessionCallback = object : MediaSessionCompat.Callback() {
                override fun onPlay() {

                    val am = applicationContext.getSystemService(Context.AUDIO_SERVICE) as AudioManager

                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {

                        audioFocusRequest = AudioFocusRequest.Builder(AudioManager.AUDIOFOCUS_GAIN).run {
                            setOnAudioFocusChangeListener(afChangeListener)
                            setAudioAttributes(AudioAttributes.Builder().run {
                                setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                                build()
                            })
                            build()
                        }

                        val result = am.requestAudioFocus(audioFocusRequest)
                        if (result == AudioManager.AUDIOFOCUS_REQUEST_GRANTED) {
                            // Start the service
                            startService(Intent(applicationContext, MediaBrowserServiceCompat::class.java))

                            // Set session active, set to use media buttons now
                            isActive = true

                            // Start the player
                            player.start()

                            val state = PlaybackState.Builder()
                                .setState(
                                    PlaybackState.STATE_PLAYING,
                                    player.currentPosition.toLong(), 1.0F
                                )
                                .build()
                            setPlaybackState(PlaybackStateCompat.fromPlaybackState(state))

                            //service.startForeground(HomeFragment.notificationID, notificationManager.builder)
                        }
                    } else {

                        var afChangeListener : AudioManager.OnAudioFocusChangeListener? = null

//                        // Request audio focus for playback
//                        val result: Int = am.requestAudioFocus(
//                            afChangeListener,
//                            // Use the music stream.
//                            AudioManager.STREAM_MUSIC,
//                            // Request permanent focus.
//                            AudioManager.AUDIOFOCUS_GAIN_TRANSIENT_MAY_DUCK
//                        )

                        // Request audio focus for playback
                        val result: Int = am.requestAudioFocus(
                            afChangeListener,
                            // Use the music stream.
                            AudioManager.STREAM_MUSIC,
                            // Request permanent focus.
                            AudioManager.AUDIOFOCUS_GAIN_TRANSIENT_MAY_DUCK
                        )

                        if (result == AudioManager.AUDIOFOCUS_REQUEST_GRANTED) {
                            startService(
                                Intent(
                                    applicationContext,
                                    MediaBrowserServiceCompat::class.java
                                )
                            )

                            // Set session active, set to use media buttons now
                            isActive = true

                            // Start the player
                            player.start()

                            val state = PlaybackState.Builder()
                                .setState(
                                    PlaybackState.STATE_PLAYING,
                                    player.currentPosition.toLong(), 1.0F
                                )
                                .build()
                            setPlaybackState(PlaybackStateCompat.fromPlaybackState(state))
                        }
                    }
                }

                override fun onStop() {
                    val am = applicationContext.getSystemService(Context.AUDIO_SERVICE) as AudioManager

                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                        am.abandonAudioFocusRequest(audioFocusRequest)

                        service.stopSelf()

                        isActive = false

                        player.stop()

                        val state = PlaybackState.Builder()
                            .setState(
                                PlaybackState.STATE_STOPPED,
                                player.currentPosition.toLong(), 1.0F
                            )
                            .build()
                        setPlaybackState(PlaybackStateCompat.fromPlaybackState(state))

//                        TODO create foreground service in onPlay and onStop
//                        service.stopForeground(false)
                    } else {
                        am.abandonAudioFocus(afChangeListener)

                        service.stopSelf()

                        isActive = false

                        player.stop()

                        val state = PlaybackState.Builder()
                            .setState(
                                PlaybackState.STATE_STOPPED,
                                player.currentPosition.toLong(), 1.0F
                            )
                            .build()
                        setPlaybackState(PlaybackStateCompat.fromPlaybackState(state))
                    }
                }

                override fun onPause() {
                    val am = applicationContext.getSystemService(Context.AUDIO_SERVICE) as AudioManager

                    player.pause()

                    val state = PlaybackState.Builder()
                        .setState(
                            PlaybackState.STATE_PAUSED,
                            player.currentPosition.toLong(), 1.0F
                        )
                        .build()
                    setPlaybackState(PlaybackStateCompat.fromPlaybackState(state))

//                    TODO dodelat
//                    // unregister BECOME_NOISY BroadcastReceiver
//                    unregisterReceiver(myNoisyAudioStreamReceiver)
//                    // Take the service out of the foreground, retain the notification
//                    service.stopForeground(false)
                }

            }

            setCallback(mediaSessionCallback)

            // Set session token, so the client activities can communicate with it
            setSessionToken(sessionToken)
        }

        // Handling the notification with session token
        notificationManager = NotificationManager (baseContext, mediaSession.sessionToken, mediaSession)

//        mediaSessionConnector = MediaSessionConnector(mediaSession!!)

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

    override fun onDestroy() {
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