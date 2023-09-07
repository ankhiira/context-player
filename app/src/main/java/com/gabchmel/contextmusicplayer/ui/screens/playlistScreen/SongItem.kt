package com.gabchmel.contextmusicplayer.ui.screens.playlistScreen

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.unit.dp
import com.gabchmel.contextmusicplayer.R
import com.gabchmel.contextmusicplayer.data.local.model.Song
import com.gabchmel.contextmusicplayer.ui.theme.spacing
import com.google.accompanist.glide.rememberGlidePainter

@Composable
fun SongItem(
    song: Song,
    onItemSelected: () -> Unit
) {
    Row(
        Modifier
            .clickable(
                onClick = {
                    onItemSelected()
                }
            ),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Image(
            painter = song.albumArt?.let {
                rememberGlidePainter(it)
            }
                ?: rememberVectorPainter(ImageVector.vectorResource(R.drawable.ic_album_cover_vector3)),
            contentDescription = "Album Art",
            contentScale = ContentScale.Fit,
            modifier = Modifier
                .size(40.dp)
                .clip(RoundedCornerShape(percent = 20))
        )
        Column(
            modifier = Modifier.padding(start = 16.dp),
            verticalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.extraSmall)
        ) {
            Text(
                text = song.title ?: stringResource(id = R.string.general_unknown),
                style = MaterialTheme.typography.bodyMedium
            )
            Text(
                text = song.author ?: stringResource(id = R.string.general_unknown),
                modifier = Modifier.alpha(0.54f),
                style = MaterialTheme.typography.bodySmall
            )
        }
    }
}