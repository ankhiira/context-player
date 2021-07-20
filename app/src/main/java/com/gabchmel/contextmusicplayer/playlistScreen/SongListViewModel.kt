package com.gabchmel.contextmusicplayer.playlistScreen

import android.app.Application
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.IBinder
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.gabchmel.common.LocalBinder
import com.gabchmel.contextmusicplayer.MediaPlaybackService
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.async
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

class SongListViewModel(val app: Application) : AndroidViewModel(app) {

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

    // call within a coroutine to bind service, waiting for onServiceConnected
    // before the coroutine resumes
    private suspend fun bindService(context: Context, intent: Intent, flags: Int) =
        suspendCoroutine<BoundService> { continuation ->

            // Create a connection object
            val conn = object: ServiceConnection {
                override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
                    val binder = service as LocalBinder<MediaPlaybackService>
                    val serviceVal = binder.getService()
                    continuation.resume(BoundService(context, name,
                        serviceVal
                        , this))

                }

                override fun onServiceDisconnected(name: ComponentName?) {
                }
            }

            // Bind to a service using connection
            context.bindService(intent, conn, flags)
        }

    private val boundService = viewModelScope.async {
        val intent = Intent(app, MediaPlaybackService::class.java)
        intent.putExtra("is_binding", true)
        bindService(app, intent, Context.BIND_AUTO_CREATE)
    }

    var songs = flow {
        val service = boundService.await().service
        emitAll(service.songs)
    }.stateIn(viewModelScope, SharingStarted.Lazily,null)

    private val _isRefreshing = MutableStateFlow(false)

    val isRefreshing: StateFlow<Boolean>
        get() = _isRefreshing.asStateFlow()

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

    @ExperimentalCoroutinesApi
    fun loadSongs() {
        try {boundService.getCompleted().service.loadSongs()} catch (e:Exception){}
    }

    @ExperimentalCoroutinesApi
    override fun onCleared() {
        boundService.getCompleted().unbind()
        super.onCleared()
    }
}