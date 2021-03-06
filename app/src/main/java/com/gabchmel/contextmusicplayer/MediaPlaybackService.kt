package com.gabchmel.contextmusicplayer

import android.os.Bundle
import android.support.v4.media.MediaBrowserCompat
import android.support.v4.media.MediaMetadataCompat
import android.support.v4.media.session.MediaSessionCompat
import android.support.v4.media.session.PlaybackStateCompat
import androidx.media.MediaBrowserServiceCompat
import com.google.android.exoplayer2.ext.mediasession.MediaSessionConnector


class MediaPlaybackService : MediaBrowserServiceCompat() {

    private lateinit var MY_MEDIA_ROOT_ID: String

    protected lateinit var mediaSession : MediaSessionCompat
    protected lateinit var mediaSessionConnector: MediaSessionConnector

    private lateinit var stateBuilder : PlaybackStateCompat.Builder
    private lateinit var notificationManager : NotificationManager
    private lateinit var mediaSource: MediaMetadataCompat

    override fun onCreate() {
        super.onCreate()

        // Create and initialize the media session
        mediaSession = MediaSessionCompat(baseContext, "MusicService")
            .apply {

//            setFlags(MediaSessionCompat.FLAG_HANDLES_MEDIA_BUTTONS
//            or MediaSessionCompat.FLAG_HANDLES_TRANSPORT_CONTROLS)

            // Initial PlaybackState set to ACTION_PLAY, so the media buttons can start the player
            stateBuilder = PlaybackStateCompat.Builder()
                .setActions(PlaybackStateCompat.ACTION_PLAY
                or PlaybackStateCompat.ACTION_PLAY_PAUSE)

            setPlaybackState(stateBuilder.build())

//            setCallback(MySessionCallback())

            // Set session token, so the client activities can communicate with it
            setSessionToken(sessionToken)
        }

        // Handling the notification with session token
        notificationManager = NotificationManager ()

        mediaSessionConnector = MediaSessionConnector(mediaSession)

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
        TODO("Not yet implemented")

    }

//    override fun MediaSessionCompat.Callback.onPlay() {
//            //todo
//    }

}