package com.gabchmel.contextmusicplayer.playlist.presentation.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.gabchmel.contextmusicplayer.R
import com.gabchmel.contextmusicplayer.core.data.song.Song
import com.gabchmel.contextmusicplayer.playlist.presentation.getArtworkPainter
import com.gabchmel.contextmusicplayer.ui.theme.spacing

@Composable
fun SongListItem(
    song: Song,
    onItemSelected: () -> Unit
) {
    Row(
        Modifier
            .fillMaxWidth()
            .clickable(
                onClick = {
                    onItemSelected()
                }
            )
            .padding(
                horizontal = MaterialTheme.spacing.medium,
                vertical = 6.dp
            ),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Image(
            painter = song.metaData.getArtworkPainter(),
            contentDescription = "Album Art",
            contentScale = ContentScale.Fit,
            modifier = Modifier
                .size(48.dp)
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
                text = song.artist ?: stringResource(id = R.string.general_unknown),
                modifier = Modifier.alpha(0.54f),
                style = MaterialTheme.typography.bodySmall
            )
        }
    }
}