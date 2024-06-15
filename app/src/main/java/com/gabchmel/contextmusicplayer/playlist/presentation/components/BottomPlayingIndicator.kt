package com.gabchmel.contextmusicplayer.playlist.presentation.components

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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.unit.dp
import androidx.media3.common.MediaMetadata
import com.gabchmel.contextmusicplayer.R
import com.google.accompanist.glide.rememberGlidePainter

@Composable
fun BottomPlayingIndicator(
    songMetadata: MediaMetadata?,
    isPlaying: Boolean,
    onPlayClicked: () -> Unit
) {
    BottomAppBar(
        modifier = Modifier.clip(RoundedCornerShape(10.dp, 10.dp)),
        containerColor = MaterialTheme.colorScheme.secondaryContainer
    ) {
        Row(
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp, vertical = 8.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Image(
                    painter = songMetadata?.artworkData?.let {
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
                        .clip(RoundedCornerShape(10.dp))
                        .height(46.dp)
                )
                Column(
                    modifier = Modifier.padding(horizontal = 10.dp)
                ) {
                    Text(
                        text = songMetadata?.title?.toString() ?: "Loading",
                        color = MaterialTheme.colorScheme.onPrimary,
                        style = MaterialTheme.typography.bodySmall
                    )
                    Text(
                        text = songMetadata?.artist?.toString() ?: "Loading",
                        color = MaterialTheme.colorScheme.onPrimary,
                        modifier = Modifier.alpha(0.54f),
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
            IconButton(
                onClick = onPlayClicked,
                modifier = Modifier.size(48.dp)
            ) {
                Icon(
                    painter = rememberVectorPainter(
                        ImageVector.vectorResource(
                            if (isPlaying) R.drawable.ic_pause_new
                            else R.drawable.ic_play_button_arrowhead
                        )
                    ),
                    contentDescription = "Play button",
                    modifier = Modifier.size(20.dp),
                    tint = MaterialTheme.colorScheme.onPrimary
                )
            }
        }
    }
}