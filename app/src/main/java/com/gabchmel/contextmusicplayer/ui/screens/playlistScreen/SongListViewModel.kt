package com.gabchmel.contextmusicplayer.ui.screens.playlistScreen

import android.Manifest
import android.app.Application
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.content.pm.PackageManager
import android.os.Build
import android.os.IBinder
import android.support.v4.media.MediaBrowserCompat
import android.support.v4.media.MediaMetadataCompat
import android.support.v4.media.session.MediaControllerCompat
import android.support.v4.media.session.PlaybackStateCompat
import androidx.core.app.ActivityCompat
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.gabchmel.common.data.LocalBinder
import com.gabchmel.contextmusicplayer.data.local.MetaDataReaderImpl
import com.gabchmel.contextmusicplayer.data.local.model.Song
import com.gabchmel.contextmusicplayer.service.MusicService
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.async
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
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

    val songs = MutableStateFlow<List<Song>?>(null)

    inner class BoundService(
        private val context: Context,
        val name: ComponentName?,
        val service: MusicService,
        private val conn: ServiceConnection
    ) {
        fun unbind() {
            context.unbindService(conn)
        }
    }

    init {
        if (ActivityCompat.checkSelfPermission(
                app,
                Manifest.permission.READ_MEDIA_AUDIO
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                app,
                Manifest.permission.READ_EXTERNAL_STORAGE
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
        }
        viewModelScope.launch {
            val permission =
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU)
                    Manifest.permission.READ_MEDIA_AUDIO
                else
                    Manifest.permission.READ_EXTERNAL_STORAGE

            if (ActivityCompat.checkSelfPermission(
                    app,
                    permission
                ) != PackageManager.PERMISSION_GRANTED
            ) return@launch

            songs.value = MetaDataReaderImpl(app).loadLocalStorageSongs()
        }

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
//        mediaBrowser = MediaBrowserCompat(
//            app,
//            ComponentName(app, MusicService::class.java),
//            connectionCallbacks,
//            null
//        ).apply {
//            // Connects to the MediaBrowseService
//            connect()
//        }
    }

    private val boundService = viewModelScope.async {
        val intent = Intent(app, MusicService::class.java)
            .putExtra("is_binding", true)
        bindService(app, intent, Context.BIND_AUTO_CREATE)
    }

//    // List of songs as a State Flow to get always current data
//    var songs = flow {
//        val service = boundService.await().service
//        emitAll(service.songs)
//    }.stateIn(viewModelScope, SharingStarted.Lazily, null)

    // Detect if the list refresh was triggered
    private val _isRefreshing = MutableStateFlow(false)
    val isRefreshing: StateFlow<Boolean>
        get() = _isRefreshing.asStateFlow()

    private suspend fun bindService(
        context: Context,
        intent: Intent,
        flags: Int
    ) =
        suspendCoroutine { continuation ->

            val connection = object : ServiceConnection {
                override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
                    val binder = service as LocalBinder<MusicService>
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

            context.bindService(intent, connection, flags)
        }

    // Function to refresh list of song on pull down
    fun refresh() {
        // This doesn't handle multiple 'refreshing' tasks, don't use this
        viewModelScope.launch {
            _isRefreshing.value = true
//            boundService.await().service.loadSongs()
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
//            boundService.getCompleted().service.loadSongs()
        } catch (e: Exception) {
            println(e)
        }
    }

    // Function to play song from a bottom bar
    fun playSong() {
        val playbackState = this.musicState.value?.state ?: return
        when (playbackState) {
            PlaybackStateCompat.STATE_PLAYING -> {
                this.pause()
                // Preemptively set icon
                // binding.btnPlay.setBackgroundResource(R.drawable.ic_play_arrow_black_24dp)
            }

            else -> {
                this.play()
                // Preemptively set icon
                // binding.btnPlay.setBackgroundResource(R.drawable.ic_pause_black_24dp)
            }
        }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    override fun onCleared() {
        //TODO crashed with This job has not completed yet
        boundService.getCompleted().unbind()
        super.onCleared()
    }
}