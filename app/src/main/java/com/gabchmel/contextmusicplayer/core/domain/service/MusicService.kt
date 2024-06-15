package com.gabchmel.contextmusicplayer.core.domain.service

import android.content.Intent
import androidx.media3.common.AudioAttributes
import androidx.media3.common.C
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.session.MediaLibraryService
import androidx.media3.session.MediaSession

class MusicService : MediaLibraryService() {

    private var mediaLibrarySession: MediaLibrarySession? = null

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
    }

    override fun onGetSession(controllerInfo: MediaSession.ControllerInfo): MediaLibrarySession? =
        mediaLibrarySession

    @UnstableApi
    override fun onCreate() {
        super.onCreate()

        val player = ExoPlayer.Builder(this)
            .setHandleAudioBecomingNoisy(true)
            .setWakeMode(C.WAKE_MODE_LOCAL)
            .setAudioAttributes(AudioAttributes.DEFAULT, /* handleAudioFocus = */ true)
            .build()

        mediaLibrarySession = MediaLibrarySession.Builder(
            this,
            player,
            callback
        ).build()
    }

    override fun onDestroy() {
        releaseMediaSession()
        super.onDestroy()
    }

    override fun onTaskRemoved(rootIntent: Intent?) {
        super.onTaskRemoved(rootIntent)

        releaseMediaSession()
        stopSelf()
    }

    private fun releaseMediaSession() {
        mediaLibrarySession?.run {
            release()

            if (player.playbackState != Player.STATE_IDLE) {
                player.release()
            }

            mediaLibrarySession = null
        }
    }
}