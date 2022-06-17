package com.gabchmel.contextmusicplayer.ui.screens.playlistScreen

import android.app.Application
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.IBinder
import android.support.v4.media.MediaBrowserCompat
import android.support.v4.media.MediaMetadataCompat
import android.support.v4.media.session.MediaControllerCompat
import android.support.v4.media.session.PlaybackStateCompat
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.gabchmel.common.data.LocalBinder
import com.gabchmel.contextmusicplayer.data.service.MediaPlaybackService
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.async
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

class SongListViewModel(val app: Application) : AndroidViewModel(app) {

    private lateinit var mediaBrowser: MediaBrowserCompat
    lateinit var mediaController: MediaControllerCompat

    private val _musicState = MutableStateFlow<PlaybackStateCompat?>(null)
    val musicState: StateFlow<PlaybackStateCompat?> = _musicState

    private val _musicMetadata = MutableStateFlow<MediaMetadataCompat?>(null)
    val musicMetadata: StateFlow<MediaMetadataCompat?> = _musicMetadata

    private val _connected = MutableStateFlow(false)
    val connected: StateFlow<Boolean> = _connected

    inner class BoundService(
        private val context: Context,
        val name: ComponentName?,
        val service: MediaPlaybackService,
        private val conn: ServiceConnection
    ) {
        fun unbind() {
            context.unbindService(conn)
        }
    }

    init {
        val connectionCallbacks = object : MediaBrowserCompat.ConnectionCallback() {
            override fun onConnected() {
                mediaBrowser.sessionToken.also { token ->
                    // Create MediaControllerCompat
                    mediaController = MediaControllerCompat(
                        app,
                        token
                    )
                }

                // Display initial state
                _musicMetadata.value = mediaController.metadata
                _musicState.value = mediaController.playbackState

                // Media Controller callback to detect data or state change
                val controllerCallback = object : MediaControllerCompat.Callback() {
                    // on Metadata change save the new value
                    override fun onMetadataChanged(metadata: MediaMetadataCompat) {
                        _musicMetadata.value = metadata
                        _connected.value = true
                    }

                    // on PlaybackState change save the new value
                    override fun onPlaybackStateChanged(state: PlaybackStateCompat) {
                        _musicState.value = state
                    }
                }

                // Register a media controller callback
                mediaController.registerCallback(controllerCallback)
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

    private val boundService = viewModelScope.async {
        val intent = Intent(app, MediaPlaybackService::class.java)
            .putExtra("is_binding", true)
        bindService(app, intent, Context.BIND_AUTO_CREATE)
    }

    // List of songs as a State Flow to get always current data
    var songs = flow {
        val service = boundService.await().service
        emitAll(service.songs)
    }.stateIn(viewModelScope, SharingStarted.Lazily, null)

    // Detect if the list refresh was triggered
    private val _isRefreshing = MutableStateFlow(false)
    val isRefreshing: StateFlow<Boolean>
        get() = _isRefreshing.asStateFlow()

    // Binds to service and waits for onServiceConnected
    private suspend fun bindService(context: Context, intent: Intent, flags: Int) =
        suspendCoroutine<BoundService> { continuation ->

            // Create a connection object
            val connection = object : ServiceConnection {
                override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
                    val binder = service as LocalBinder<MediaPlaybackService>
                    val serviceVal = binder.getService()
                    continuation.resume(
                        BoundService(
                            context, name,
                            serviceVal, this
                        )
                    )
                }

                override fun onServiceDisconnected(name: ComponentName?) {
                }
            }

            // Bind to a service using connection
            context.bindService(intent, connection, flags)
        }

    // Function to refresh list of song on pull down
    fun refresh() {
        // This doesn't handle multiple 'refreshing' tasks, don't use this
        viewModelScope.launch {
            _isRefreshing.value = true
            boundService.await().service.loadSongs()
            delay(1000)
            _isRefreshing.value = false
        }
    }

    fun play() {
        mediaController.transportControls.play()
    }

    fun pause() {
        mediaController.transportControls.pause()
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    fun loadSongs() {
        try {
            boundService.getCompleted().service.loadSongs()
        } catch (e: Exception) {
            println(e)
        }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    override fun onCleared() {
        boundService.getCompleted().unbind()
        super.onCleared()
    }
}