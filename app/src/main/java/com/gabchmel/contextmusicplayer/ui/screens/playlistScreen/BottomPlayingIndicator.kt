package com.gabchmel.contextmusicplayer.ui.screens.playlistScreen

import android.support.v4.media.MediaMetadataCompat
import android.support.v4.media.session.PlaybackStateCompat
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.unit.dp
import com.gabchmel.contextmusicplayer.R
import com.gabchmel.contextmusicplayer.utils.getAlbumArt
import com.gabchmel.contextmusicplayer.utils.getArtist
import com.gabchmel.contextmusicplayer.utils.getTitle
import com.google.accompanist.glide.rememberGlidePainter

@Composable
fun BottomPlayingIndicator(
    songMetadata: MediaMetadataCompat?,
    playbackState: PlaybackStateCompat?,
    onPlayClicked: () -> Unit
) {
    BottomAppBar(
        containerColor = MaterialTheme.colorScheme.surface
    ) {
        Row(
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxSize()
                .padding(8.dp)
        ) {
            Row(
                modifier = Modifier.padding(8.dp)
            ) {
                // Album art
                Image(
                    painter = songMetadata?.getAlbumArt()?.let {
                        rememberGlidePainter(it)
                    }
                        ?: rememberVectorPainter(
                            ImageVector.vectorResource(
                                R.drawable.ic_album_cover_vector3_colored
                            )
                        ),
                    contentDescription = "Album Art",
                    contentScale = ContentScale.Fit,
                    modifier = Modifier
                        .clip(RoundedCornerShape(percent = 10))
                        .height(46.dp)
                )
                Column(
                    modifier = Modifier.padding(horizontal = 8.dp)
                ) {
                    Text(
                        text = songMetadata?.getTitle() ?: "Loading",
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                    Text(
                        text = songMetadata?.getArtist() ?: "Loading",
                        color = MaterialTheme.colorScheme.onPrimary,
                        modifier = Modifier.alpha(0.54f)
                    )
                }
            }
            IconButton(
                onClick = onPlayClicked,
                modifier = Modifier
                    .size(34.dp)
                    .padding(horizontal = 8.dp)
            ) {
                Icon(
                    painter = rememberVectorPainter(
                        ImageVector.vectorResource(
                            when (playbackState?.state) {
                                PlaybackStateCompat.STATE_PLAYING ->
                                    R.drawable.ic_pause_new

                                else ->
                                    R.drawable.ic_play_button_arrowhead
                            }
                        )
                    ),
                    contentDescription = "Play button",
                    tint = Color(0xFFB1B1B1)
                )
            }
        }
    }
}