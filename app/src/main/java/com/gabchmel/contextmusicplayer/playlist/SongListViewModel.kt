package com.gabchmel.contextmusicplayer.playlist

import android.Manifest
import android.app.Application
import android.content.pm.PackageManager
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.core.app.ActivityCompat
import androidx.lifecycle.AndroidViewModel

class SongListViewModel(val app: Application) : AndroidViewModel(app) {

    var songs by mutableStateOf(emptyList<Song>())
    val texts = "bla"

    init {
        loadSongs()

//        app.contentResolver.openAssetFileDescriptor(songs[2].URI,"r")
    }

    fun loadSongs() {
        if (ActivityCompat.checkSelfPermission(
                app,
                Manifest.permission.READ_EXTERNAL_STORAGE
            ) != PackageManager.PERMISSION_GRANTED
        ) return

        songs = SongScanner.loadSongs(app)
    }
}