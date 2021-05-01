package com.gabchmel.contextmusicplayer.nowPlayingScreen

import android.app.Application
import android.content.ComponentName
import android.net.Uri
import android.os.Build
import android.support.v4.media.MediaBrowserCompat
import android.support.v4.media.MediaMetadataCompat
import android.support.v4.media.session.MediaControllerCompat
import android.support.v4.media.session.PlaybackStateCompat
import androidx.annotation.RequiresApi
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.gabchmel.contextmusicplayer.MediaPlaybackService
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch


@RequiresApi(Build.VERSION_CODES.Q)
class NowPlayingViewModel(val app: Application) : AndroidViewModel(app) {

    private lateinit var mediaBrowser: MediaBrowserCompat
    private var mediaBrowserConnectionCallback = MediaBrowserCompat.ConnectionCallback()

    lateinit var mediaController: MediaControllerCompat

    private val _musicState = MutableLiveData<PlaybackStateCompat>()
    val musicState: LiveData<PlaybackStateCompat> = _musicState

    private val _musicMetadata = MutableLiveData<MediaMetadataCompat>()
    val musicMetadata: LiveData<MediaMetadataCompat> = _musicMetadata

    lateinit var args: NowPlayingFragmentArgs

    var notPlayed = true

    val service = CompletableDeferred<MediaPlaybackService>()

    private var songs = flow {
        val service = service.await()
        emitAll(service.songs)
    }.stateIn(viewModelScope, SharingStarted.Lazily,null)

    private val connectionCallbacks = object : MediaBrowserCompat.ConnectionCallback() {
        override fun onConnected() {

            mediaBrowser.sessionToken.also { token ->

                // Create MediaControllerCompat
                mediaController = MediaControllerCompat(
                    app,
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

            viewModelScope.launch {
                service.complete(MediaPlaybackService.getInstance(app))
            }
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
            null
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

    fun setMusicProgress(progress: Int) {
        mediaController.transportControls.seekTo(progress.toLong())
    }
}