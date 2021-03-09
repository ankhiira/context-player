package com.gabchmel.contextmusicplayer.homeScreen

import android.app.Application
import android.content.ComponentName
import android.media.MediaMetadataRetriever
import android.util.Log
import androidx.lifecycle.ViewModel
import android.net.Uri
import android.support.v4.media.MediaBrowserCompat
import android.support.v4.media.MediaMetadataCompat
import android.support.v4.media.session.MediaControllerCompat
import android.support.v4.media.session.PlaybackStateCompat
import android.widget.Button
import androidx.lifecycle.AndroidViewModel
import com.gabchmel.contextmusicplayer.*


class NowPlayingViewModel(val app : Application) : AndroidViewModel(app) {

    var uri = Uri.parse("android.resource://com.gabchmel.contextmusicplayer/" + R.raw.gaga)

    val musicMetadata: MusicMetadata

    private lateinit var mediaBrowser: MediaBrowserCompat

    init {
        val metadataRetriever = MediaMetadataRetriever()
        metadataRetriever.setDataSource(app,uri)
        musicMetadata = metadataRetriever.toMusicMetadata()

//        mediaBrowser = MediaBrowserCompat(
//            app,
//            ComponentName(app, MediaPlaybackService::class.java),
//            connectionCallbacks,
//            null // optional
//        )
//
//        mediaBrowser.connect()
    }

    override fun onCleared() {
        super.onCleared()

//        MediaControllerCompat.getMediaController(requireactivity)?.unregisterCallback(controllerCallback)
//        mediaBrowserConnectionCallback.onConnectionSuspended()
        mediaBrowser.disconnect()
    }

    private val connectionCallbacks = object : MediaBrowserCompat.ConnectionCallback() {
        override fun onConnected() {

            mediaBrowser.sessionToken.also { token ->

                // Create MediaControllerCompat
                val mediaController = MediaControllerCompat(
                    app,
                    token
                )

//                MediaControllerCompat.setMediaController(app, mediaController)

                mediaController.registerCallback(controllerCallback)
            }

//            buildTransportControls()
        }

        override fun onConnectionSuspended() {
            super.onConnectionSuspended()
        }

        override fun onConnectionFailed() {
            super.onConnectionFailed()
        }
    }

//    fun buildTransportControls() {
//
////        val mediaController = MediaControllerCompat.getMediaController(app)
//
////        runOnUiThread {
////            var playPause = findViewById<Button>(R.id.btn_play).apply {
////                setOnClickListener {
////                    val pbState = mediaController.playbackState.state
////                    if (pbState == PlaybackStateCompat.STATE_PLAYING) {
////                        mediaController.transportControls.pause()
////                    } else {
////                        mediaController.transportControls.play()
////                    }
////                }
////            }
////        }
//
//        // Display initial state
//        val metadata = mediaController.metadata
//        val pbstate = mediaController.playbackState
//
//        // Register a callback to stay in sync
//        mediaController.registerCallback(controllerCallback)
//    }

    private var controllerCallback = object : MediaControllerCompat.Callback() {

        override fun onMetadataChanged(metadata: MediaMetadataCompat?) {
            super.onMetadataChanged(metadata)
        }

        override fun onPlaybackStateChanged(state: PlaybackStateCompat?) {
            super.onPlaybackStateChanged(state)
        }
    }

//    fun playBtnPlay () {
//        val pbState = mediaController.playbackState.state
//        if (pbState == PlaybackStateCompat.STATE_PLAYING) {
//            mediaController.transportControls.pause()
//        } else {
//            mediaController.transportControls.play()
//        }
//    }
}