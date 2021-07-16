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
        val conn: ServiceConnection
    ) {
        fun unbind() {
            context.unbindService(conn)
        }
    }

    // call within a coroutine to bind service, waiting for onServiceConnected
    // before the coroutine resumes
    suspend fun bindServiceAndWait(context: Context, intent: Intent, flags: Int) =
        suspendCoroutine<BoundService> { continuation ->

            val conn = object: ServiceConnection {
                override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
                    val binder = service as LocalBinder<MediaPlaybackService>
                    val serviceVal = binder.getService()
                    continuation.resume(BoundService(context, name,
                        serviceVal
                        , this))

                }
                override fun onServiceDisconnected(name: ComponentName?) {
                    // ignore, not much we can do
                }

            }
            context.bindService(intent, conn, flags)
        }

    private val bs = viewModelScope.async {
//        MediaPlaybackService.getInstance(app)
        val intent = Intent(app, MediaPlaybackService::class.java)
        intent.putExtra("is_binding", true)
        bindServiceAndWait(app,
            intent, Context.BIND_AUTO_CREATE)
    }

    var songs = flow {
        val bs = bs.await()
        val service = bs.service
        emitAll(service.songs)
    }.stateIn(viewModelScope, SharingStarted.Lazily,null)

    private val _isRefreshing = MutableStateFlow(false)

    val isRefreshing: StateFlow<Boolean>
        get() = _isRefreshing.asStateFlow()

    fun refresh() {
        // This doesn't handle multiple 'refreshing' tasks, don't use this
        viewModelScope.launch {

            _isRefreshing.value = true
            bs.await().service.loadSongs()
            delay(1000)
            _isRefreshing.value = false
        }
    }

    fun loadSongs() {
        try {bs.getCompleted().service.loadSongs()} catch (e:Exception){}
    }

    override fun onCleared() {
//        super.onCleared()
        bs.getCompleted().unbind()
    }
}