package com.gabchmel.contextmusicplayer.playlist.presentation

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.res.vectorResource
import androidx.media3.common.MediaMetadata
import coil.compose.rememberAsyncImagePainter
import com.gabchmel.contextmusicplayer.R

@Composable
fun MediaMetadata?.getArtworkPainter(): Painter {
    return when {
        this?.artworkUri != null -> {
            rememberAsyncImagePainter(this.artworkUri)
        }

        this?.artworkData != null -> {
            rememberAsyncImagePainter(this.artworkData)
        }

        else -> {
            rememberVectorPainter(ImageVector.vectorResource(R.drawable.ic_album_cover_vector))
        }
    }
}