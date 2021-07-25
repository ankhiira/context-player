package com.gabchmel.contextmusicplayer

import android.graphics.Bitmap
import android.net.Uri

data class Song(
    val title: String?,
    val author: String?,
    val albumArt: Bitmap?,
    val URI: Uri
)
