package com.gabchmel.contextmusicplayer.playlistScreen

import android.Manifest
import android.app.Application
import android.content.pm.PackageManager
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.core.app.ActivityCompat
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

@RequiresApi(Build.VERSION_CODES.Q)
class SongListViewModel(val app: Application) : AndroidViewModel(app) {

    var songs by mutableStateOf(emptyList<Song>())

    private val _isRefreshing = MutableStateFlow(false)

    val isRefreshing: StateFlow<Boolean>
        get() = _isRefreshing.asStateFlow()

    init {
        // To load song list on opening
        loadSongs()
    }

    @RequiresApi(Build.VERSION_CODES.Q)
    fun loadSongs() {
        if (ActivityCompat.checkSelfPermission(
                app,
                Manifest.permission.READ_EXTERNAL_STORAGE
            ) != PackageManager.PERMISSION_GRANTED
        ) return

        songs = SongScanner.loadSongs(app)
    }

    fun refresh() {
        // This doesn't handle multiple 'refreshing' tasks, don't use this
        viewModelScope.launch {

            _isRefreshing.value = true
            loadSongs()
            delay(1000)
            _isRefreshing.value = false
        }
    }

    fun play() {

    }
}