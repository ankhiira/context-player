package com.gabchmel.contextmusicplayer.data.local.model

import android.graphics.Bitmap
import android.net.Uri

data class Song(
    val title: String?,
    val author: String?,
    val albumArt: Bitmap?,
    val uri: Uri
)