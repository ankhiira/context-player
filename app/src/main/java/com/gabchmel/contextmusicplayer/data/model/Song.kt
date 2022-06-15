package com.gabchmel.contextmusicplayer.data.model

import android.graphics.Bitmap
import android.net.Uri

// Data class for saving one Song instance
data class Song(
    val title: String?,
    val author: String?,
    val albumArt: Bitmap?,
    val URI: Uri
)
