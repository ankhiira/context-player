package com.gabchmel.contextmusicplayer.homeScreen

import android.app.Application
import android.content.ComponentName
import android.media.MediaMetadataRetriever
import android.net.Uri
import android.support.v4.media.MediaBrowserCompat
import android.support.v4.media.MediaMetadataCompat
import android.support.v4.media.session.MediaControllerCompat
import android.support.v4.media.session.PlaybackStateCompat
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.viewbinding.ViewBindings
import com.gabchmel.contextmusicplayer.*
import kotlinx.android.synthetic.main.fragment_home.*


class NowPlayingViewModel(val app: Application) : AndroidViewModel(app) {

    private var uri = Uri.parse("android.resource://com.gabchmel.contextmusicplayer/" + R.raw.gaga)

    private lateinit var mediaBrowser: MediaBrowserCompat
    private var mediaBrowserConnectionCallback = MediaBrowserCompat.ConnectionCallback()

    lateinit var mediaController: MediaControllerCompat

    private val _musicState = MutableLiveData<PlaybackStateCompat>()
    val musicState: LiveData<PlaybackStateCompat> = _musicState

    private val _musicMetadata = MutableLiveData<MediaMetadataCompat>()
    val musicMetadata: LiveData<MediaMetadataCompat> = _musicMetadata


    private val connectionCallbacks = object : MediaBrowserCompat.ConnectionCallback() {
        override fun onConnected() {

            mediaBrowser.sessionToken.also { token ->

                // Create MediaControllerCompat
                mediaController = MediaControllerCompat(
                    app,
                    token
                )

                mediaController.registerCallback(controllerCallback)
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

    init {
        // Setting MediaBrowser for connecting to the MediaBrowserService
        mediaBrowser = MediaBrowserCompat(
            app,
            ComponentName(app, MediaPlaybackService::class.java),
            connectionCallbacks,
            null // optional
        )

        // Connects to the MediaBrowseService
        mediaBrowser.connect()
    }

    override fun onCleared() {
        super.onCleared()

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

    fun play() {
        mediaController.transportControls.play()
    }

    fun pause() {
        mediaController.transportControls.pause()
    }

    fun setMusicProgress(progress: Int) {
        mediaController.transportControls.seekTo(progress.toLong())
    }

}