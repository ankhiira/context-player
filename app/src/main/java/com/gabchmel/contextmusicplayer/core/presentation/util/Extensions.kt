package com.gabchmel.contextmusicplayer.core.presentation.util

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.res.vectorResource
import androidx.media3.common.MediaMetadata
import com.gabchmel.contextmusicplayer.R
import com.google.accompanist.glide.rememberGlidePainter

@Composable
fun MediaMetadata?.getArtworkPainter(): Painter {
    return when {
        this?.artworkUri != null -> {
            rememberGlidePainter(this.artworkUri)
        }

        this?.artworkData != null -> {
            rememberGlidePainter(this.artworkData)
        }

        else -> {
            rememberVectorPainter(ImageVector.vectorResource(R.drawable.ic_album_cover_vector))
        }
    }
}