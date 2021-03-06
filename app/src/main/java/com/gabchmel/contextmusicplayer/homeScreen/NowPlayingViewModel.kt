package com.gabchmel.contextmusicplayer.homeScreen

import android.app.Application
import android.media.MediaMetadataRetriever
import android.util.Log
import androidx.lifecycle.ViewModel
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import com.gabchmel.contextmusicplayer.MusicMetadata
import com.gabchmel.contextmusicplayer.MusicMetadataExtractor
import com.gabchmel.contextmusicplayer.R
import com.gabchmel.contextmusicplayer.toMusicMetadata


class NowPlayingViewModel(val app : Application) : AndroidViewModel(app) {


    var uri = Uri.parse("android.resource://com.gabchmel.contextmusicplayer/" + R.raw.gaga)

    val musicMetadata: MusicMetadata


    init {
//        Log.i("NowPlayModel", "NowPlayModel created!")

        val metadataRetriever = MediaMetadataRetriever()
        metadataRetriever.setDataSource(app,uri)
        musicMetadata = metadataRetriever.toMusicMetadata()

    }

//    data class NowPlayingData(
//        val title : String?,
//        val subtitle :String?
//    )

    override fun onCleared() {
        super.onCleared()

//        Log.i("NowPlayModel", "NowPlayModel destroyed!")
    }
}