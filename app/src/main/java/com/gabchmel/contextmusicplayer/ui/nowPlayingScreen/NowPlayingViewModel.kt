package com.gabchmel.contextmusicplayer.ui.nowPlayingScreen

import android.app.Application
import android.content.ComponentName
import android.net.Uri
import android.support.v4.media.MediaBrowserCompat
import android.support.v4.media.MediaMetadataCompat
import android.support.v4.media.session.MediaControllerCompat
import android.support.v4.media.session.PlaybackStateCompat
import androidx.lifecycle.AndroidViewModel
import com.gabchmel.contextmusicplayer.data.service.MediaPlaybackService
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow


class NowPlayingViewModel(val app: Application) : AndroidViewModel(app) {

    private lateinit var mediaBrowser: MediaBrowserCompat
    val service = CompletableDeferred<MediaPlaybackService>()
    var mediaController: MediaControllerCompat? = null

    private val _musicState = MutableStateFlow<PlaybackStateCompat?>(null)
    val musicState: StateFlow<PlaybackStateCompat?> = _musicState

    private val _musicMetadata = MutableStateFlow<MediaMetadataCompat?>(null)
    val musicMetadata: StateFlow<MediaMetadataCompat?> = _musicMetadata

    lateinit var args: NowPlayingFragmentArgs
    var notPlayed = true

    private val connectionCallbacks = object : MediaBrowserCompat.ConnectionCallback() {
        override fun onConnected() {

            // Creation of the mediaBrowser
            mediaBrowser.sessionToken.also { token ->
                // Create MediaControllerCompat
                mediaController = MediaControllerCompat(
                    app,
                    token
                )
            }

            // Display initial state
            val metadata = mediaController?.metadata
            val pbstate = mediaController?.playbackState

            _musicState.value = pbstate

            // Register a callback to stay in sync
            mediaController?.registerCallback(controllerCallback)

            // Play after fragment is open
            if (args.play && notPlayed) {
                play(args.uri)
                notPlayed = false
            }
        }
    }

    init {
        // Setting MediaBrowser for connecting to the MediaBrowserService
        mediaBrowser = MediaBrowserCompat(
            app,
            ComponentName(app, MediaPlaybackService::class.java),
            connectionCallbacks,
            null
        )

        // Connects to the MediaBrowseService
        mediaBrowser.connect()
    }

    override fun onCleared() {
        mediaController?.unregisterCallback(controllerCallback)
        mediaBrowser.disconnect()
        super.onCleared()
    }

    private var controllerCallback = object : MediaControllerCompat.Callback() {

        override fun onMetadataChanged(metadata: MediaMetadataCompat) {
            _musicMetadata.value = metadata
        }

        override fun onPlaybackStateChanged(state: PlaybackStateCompat) {
            _musicState.value = state
        }
    }

    // To play for the first time
    fun play(uri: Uri) {
        mediaController?.transportControls?.playFromUri(uri, null)
    }

    // To play any other time
    fun play() {
        mediaController?.transportControls?.play()
    }

    fun pause() {
        mediaController?.transportControls?.pause()
    }

    fun next() {
        mediaController?.transportControls?.skipToNext()
    }

    fun prev() {
        mediaController?.transportControls?.skipToPrevious()
    }

    fun setMusicProgress(progress: Float) {
        mediaController?.transportControls?.seekTo(progress.toLong())
    }
}