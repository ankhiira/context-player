package com.gabchmel.contextmusicplayer.ui.screens.playlistScreen

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.gabchmel.contextmusicplayer.R
import com.gabchmel.contextmusicplayer.data.model.Song
import com.gabchmel.contextmusicplayer.ui.theme.spacing
import com.google.accompanist.glide.rememberGlidePainter

@Composable
fun SongItem(
    song: Song,
    onItemSelected: () -> Unit
) {
    val fontColor = MaterialTheme.colorScheme.onPrimary

    Row(
        Modifier
            .clickable(onClick = {
                onItemSelected()

            })
    ) {
        // Album art
        Image(
            painter = song.albumArt?.let {
                rememberGlidePainter(it)
            }
                ?: rememberVectorPainter(ImageVector.vectorResource(R.drawable.ic_album_cover_vector3)),
            contentDescription = "Album Art",
            contentScale = ContentScale.Fit,
            modifier = Modifier
                .padding(top = 14.dp)
                .clip(RoundedCornerShape(percent = 10))
                .height(36.dp)
        )
        Column(
            Modifier
                .padding(MaterialTheme.spacing.small)
                .fillMaxWidth()
        ) {
            Text(
                text = song.title ?: stringResource(id = R.string.general_unknown),
                color = fontColor,
                fontSize = 16.sp,
                fontWeight = FontWeight.W400
            )
            Text(
                text = song.author ?: stringResource(id = R.string.general_unknown),
                modifier = Modifier.alpha(0.54f),
                color = fontColor,
                fontSize = 14.sp,
                fontWeight = FontWeight.W400
            )
        }
    }
}