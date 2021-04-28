package com.gabchmel.contextmusicplayer.playlist

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

@RequiresApi(Build.VERSION_CODES.Q)
class SongListViewModel(val app: Application) : AndroidViewModel(app) {

    var songs by mutableStateOf(emptyList<Song>())

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
}