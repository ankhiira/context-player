package com.gabchmel.contextmusicplayer.playlistScreen

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.gabchmel.contextmusicplayer.MediaPlaybackService
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.async
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class SongListViewModel(val app: Application) : AndroidViewModel(app) {

    val service = viewModelScope.async { MediaPlaybackService.getInstance(app)}

    var songs = flow {
        val service = service.await()
        emitAll(service.songs)
    }.stateIn(viewModelScope, SharingStarted.Lazily,null)

    private val _isRefreshing = MutableStateFlow(false)

    val isRefreshing: StateFlow<Boolean>
        get() = _isRefreshing.asStateFlow()

    fun refresh() {
        // This doesn't handle multiple 'refreshing' tasks, don't use this
        viewModelScope.launch {

            _isRefreshing.value = true
            service.await().loadSongs()
            delay(1000)
            _isRefreshing.value = false
        }
    }

    @ExperimentalCoroutinesApi
    fun loadSongs() {
        try {service.getCompleted().loadSongs()} catch (e:Exception){}
    }
}