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
import androidx.core.app.ActivityCompat
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import androidx.media3.common.MediaMetadata
import androidx.media3.common.Player
import androidx.media3.session.MediaBrowser
import androidx.media3.session.SessionToken
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
import kotlinx.coroutines.guava.asDeferred
import kotlinx.coroutines.launch
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

class SongListViewModel(val app: Application) : AndroidViewModel(app) {

    private lateinit var mediaBrowser: MediaBrowser
    private val sessionToken = SessionToken(app, ComponentName(app, MusicService::class.java))

    private val _isPlaying = MutableStateFlow(false)
    val isPlaying: StateFlow<Boolean> = _isPlaying

    private val _songMetadata = MutableStateFlow<MediaMetadata?>(null)
    val musicMetadata: StateFlow<MediaMetadata?> = _songMetadata

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
        viewModelScope.launch {
            mediaBrowser = MediaBrowser.Builder(app, sessionToken)
                    .buildAsync().asDeferred().await()

            mediaBrowser.addListener(object : Player.Listener {
                override fun onMediaMetadataChanged(mediaMetadata: MediaMetadata) {
                    _songMetadata.value = mediaMetadata

                    _connected.value = true
                }

                override fun onEvents(player: Player, events: Player.Events) {
                    super.onEvents(player, events)

                    _isPlaying.value = player.isPlaying
                }
            })
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

    fun refreshSongList() {
        // This doesn't handle multiple 'refreshing' tasks, don't use this
        viewModelScope.launch {
            _isRefreshing.value = true
//            boundService.await().service.loadSongs()
            delay(1000)
            _isRefreshing.value = false
        }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    fun loadSongs() {
        try {
//            boundService.getCompleted().service.loadSongs()
        } catch (e: Exception) {
            println(e)
        }
    }

    // Play song from a bottom bar
    fun playSong() {
        val isPlaying = this._isPlaying.value ?: false

        if (isPlaying) {
            this.pause()
        } else {
            this.play()
        }
    }

    private fun play() {
        mediaBrowser.play()
    }

    private fun pause() {
        mediaBrowser.pause()
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    override fun onCleared() {
        //TODO crashed with This job has not completed yet
        boundService.getCompleted().unbind()
        super.onCleared()
    }
}