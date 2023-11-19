package com.gabchmel.contextmusicplayer.data.local.model

import android.net.Uri
import androidx.media3.common.MediaMetadata

data class Song(
    val uri: Uri,
    val title: String?,
    val artist: String?,
    val metaData: MediaMetadata?
)