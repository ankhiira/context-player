package com.gabchmel.contextmusicplayer.utils

import android.media.MediaPlayer
import android.support.v4.media.session.PlaybackStateCompat
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.res.vectorResource
import androidx.media3.common.MediaMetadata
import com.gabchmel.contextmusicplayer.R
import com.google.accompanist.glide.rememberGlidePainter

// Extension function to declare player state
fun MediaPlayer.toPlaybackStateBuilder(): PlaybackStateCompat.Builder {
    return PlaybackStateCompat.Builder()
        .setState(
            if (this.isPlaying) PlaybackStateCompat.STATE_PLAYING else PlaybackStateCompat.STATE_PAUSED,
            this.currentPosition.toLong(), 1.0F
        )
}

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