package com.gabchmel.contextmusicplayer

import android.content.ComponentName
import android.content.Context
import android.net.Uri
import android.support.v4.media.MediaBrowserCompat
import android.support.v4.media.MediaMetadataCompat
import android.support.v4.media.session.MediaControllerCompat
import android.support.v4.media.session.PlaybackStateCompat
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.whenCreated
import com.gabchmel.contextmusicplayer.nowPlayingScreen.NowPlayingFragmentArgs
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class MediaBrowserConnector(lifecycleOwner: LifecycleOwner, context: Context) : LifecycleObserver {

    private lateinit var mediaBrowser: MediaBrowserCompat
    lateinit var mediaController: MediaControllerCompat

    val service = CompletableDeferred<MediaPlaybackService>()

    lateinit var args: NowPlayingFragmentArgs

    var notPlayed = true

    private val _musicState = MutableStateFlow<PlaybackStateCompat?>(null)
    val musicState: StateFlow<PlaybackStateCompat?> = _musicState

    private val _musicMetadata = MutableStateFlow<MediaMetadataCompat?>(null)
    val musicMetadata: StateFlow<MediaMetadataCompat?> = _musicMetadata

    private val connectionCallbacks = object : MediaBrowserCompat.ConnectionCallback() {
        override fun onConnected() {

            mediaBrowser.sessionToken.also { token ->

                // Create MediaControllerCompat
                mediaController = MediaControllerCompat(
                    context,
                    token
                )
            }

            // Display initial state
            val metadata = mediaController.metadata
            val pbstate = mediaController.playbackState

            _musicState.value = pbstate

            // Register a callback to stay in sync
            mediaController.registerCallback(controllerCallback)

            // Play after fragment is open
            if (args.play && notPlayed) {
                play(args.uri)
                notPlayed = false
            }

            lifecycleOwner.lifecycleScope.launch {
                service.complete(MediaPlaybackService.getInstance(context))
            }
        }

        override fun onConnectionSuspended() {
            super.onConnectionSuspended()
        }

        override fun onConnectionFailed() {
            super.onConnectionFailed()
        }
    }

    private var controllerCallback = object : MediaControllerCompat.Callback() {

        override fun onMetadataChanged(metadata: MediaMetadataCompat) {
            _musicMetadata.value = metadata
        }

        override fun onPlaybackStateChanged(state: PlaybackStateCompat) {
            _musicState.value = state
        }
    }

    init {
//        lifecycleOwner.lifecycle.addObserver(this)
        lifecycleOwner.lifecycleScope.launch {
            lifecycleOwner.whenCreated {
                // Setting MediaBrowser for connecting to the MediaBrowserService
                mediaBrowser = MediaBrowserCompat(
                    context,
                    ComponentName(context, MediaPlaybackService::class.java),
                    connectionCallbacks,
                    null
                )

                // Connects to the MediaBrowseService
                mediaBrowser.connect()
            }
        }
    }

    // To play for the first time
    fun play(uri: Uri) {
        mediaController.transportControls.playFromUri(uri, null)
    }

    // To play any other time
    fun play() {
        mediaController.transportControls.play()
    }

    fun pause() {
        mediaController.transportControls.pause()
    }

    fun next() {
        mediaController.transportControls.skipToNext()
    }

    fun prev() {
        mediaController.transportControls.skipToPrevious()
    }

    fun setMusicProgress(progress: Float) {
        mediaController.transportControls.seekTo(progress.toLong())
    }

//    @OnLifecycleEvent(Lifecycle.Event.ON_CREATE)
//    fun connect() {
//
//    }
}