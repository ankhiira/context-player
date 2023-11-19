package com.gabchmel.contextmusicplayer.data.local.model

import android.net.Uri

data class Song(
    val uri: Uri,
    val title: String?,
    val artist: String?,
    val artworkUri: Uri?
)