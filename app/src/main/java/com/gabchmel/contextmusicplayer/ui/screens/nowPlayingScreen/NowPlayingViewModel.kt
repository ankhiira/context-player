package com.gabchmel.contextmusicplayer.ui.screens.nowPlayingScreen

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

    private val controllerCallback = object : MediaControllerCompat.Callback() {
        override fun onMetadataChanged(metadata: MediaMetadataCompat) {
            _musicMetadata.value = metadata
        }

        override fun onPlaybackStateChanged(playbackState: PlaybackStateCompat) {
            _musicState.value = playbackState
        }
    }

    init {
        val connectionCallbacks = object : MediaBrowserCompat.ConnectionCallback() {
            override fun onConnected() {

                // Creation of the mediaBrowser
                mediaBrowser.sessionToken.also { sessionToken ->
                    mediaController = MediaControllerCompat(app, sessionToken)
                }

                // Display initial state
                _musicState.value = mediaController?.playbackState

                // Register a callback to stay in sync
                mediaController?.registerCallback(controllerCallback)

                // Play after fragment is open
                if (args.play && notPlayed) {
                    play(args.uri)
                    notPlayed = false
                }
            }
        }

        // Setting MediaBrowser for connecting to the MediaBrowserService
        mediaBrowser = MediaBrowserCompat(
            app,
            ComponentName(app, MediaPlaybackService::class.java),
            connectionCallbacks,
            null
        ).apply {
            // Connects to the MediaBrowseService
            connect()
        }
    }

    override fun onCleared() {
        mediaController?.unregisterCallback(controllerCallback)
        mediaBrowser.disconnect()
        super.onCleared()
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