package com.gabchmel.contextmusicplayer.service

import android.app.Notification
import android.content.Intent
import androidx.media3.common.AudioAttributes
import androidx.media3.common.C
import androidx.media3.common.ForwardingPlayer
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.session.MediaLibraryService
import androidx.media3.session.MediaSession

class MusicService : MediaLibraryService() {

    private lateinit var mediaLibrarySession: MediaLibrarySession

    private lateinit var notification: Notification

    private val callback = @UnstableApi object : MediaLibrarySession.Callback {
        override fun onConnect(
            session: MediaSession,
            controller: MediaSession.ControllerInfo
        ): MediaSession.ConnectionResult {
            val connectionResult = super.onConnect(session, controller)
            val sessionCommands =
                connectionResult.availableSessionCommands
                    .buildUpon()
                    .build()
            return MediaSession.ConnectionResult.accept(
                sessionCommands, connectionResult.availablePlayerCommands
            )
        }

        // TODO to resume playback when connected to another device
//        override fun onPlaybackResumption(
//            mediaSession: MediaSession,
//            controller: MediaSession.ControllerInfo
//        ): ListenableFuture<MediaSession.MediaItemsWithStartPosition> {
//            val settable = SettableFuture.create<MediaSession.MediaItemsWithStartPosition>()
//            CoroutineScope(Dispatchers.Default).launch {
//                // Your app is responsible for storing the playlist and the start position
//                // to use here
//                val resumptionPlaylist = restorePlaylist()
//                settable.set(resumptionPlaylist)
//            }
//            return settable
//        }
    }

    @UnstableApi
    override fun onCreate() {
        super.onCreate()

        val player = ExoPlayer.Builder(this)
            .setHandleAudioBecomingNoisy(true)
            .setWakeMode(C.WAKE_MODE_LOCAL)
            .setAudioAttributes(AudioAttributes.DEFAULT, /* handleAudioFocus = */ true)
            .build()

        val forwardingPlayer = object : ForwardingPlayer(player) {
            override fun play() {
                // TODO custom logic
                super.play()
            }

            override fun setPlayWhenReady(playWhenReady: Boolean) {
                // Add custom logic
                super.setPlayWhenReady(playWhenReady)
            }
        }

        mediaLibrarySession = MediaLibrarySession.Builder(
            this,
            forwardingPlayer,
            callback
        ).build()
    }

    override fun onTaskRemoved(rootIntent: Intent?) {
        super.onTaskRemoved(rootIntent)

//        val notificationManager =
//            applicationContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManagerCompat
//        notificationManager.cancelAll()

        releaseMediaSession()
        stopSelf()
    }

    override fun onGetSession(controllerInfo: MediaSession.ControllerInfo): MediaLibrarySession =
        mediaLibrarySession

    override fun onDestroy() {
        releaseMediaSession()
        super.onDestroy()
    }

    private fun releaseMediaSession() {
        mediaLibrarySession.run {
            release()
            if (player.playbackState != Player.STATE_IDLE) {
                player.release()
            }
        }
    }
}