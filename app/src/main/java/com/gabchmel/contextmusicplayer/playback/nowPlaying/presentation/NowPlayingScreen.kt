package com.gabchmel.contextmusicplayer.playback.nowPlaying.presentation

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.absolutePadding
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ArrowBack
import androidx.compose.material.icons.rounded.SkipNext
import androidx.compose.material.icons.rounded.SkipPrevious
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.gabchmel.contextmusicplayer.R
import com.gabchmel.contextmusicplayer.playlist.presentation.getArtworkPainter
import com.gabchmel.contextmusicplayer.ui.Settings
import kotlinx.coroutines.delay

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NowPlayingScreen(
    viewModel: NowPlayingViewModel = viewModel(),
    navigateToSettings: (entry: Settings) -> Unit = {},
    popBackStack: () -> Unit = {}
) {
    val isPlaying by viewModel.isPlaying.collectAsStateWithLifecycle()
    val playbackPosition by viewModel.songProgress.collectAsStateWithLifecycle()
    val songDuration by viewModel.songDuration.collectAsStateWithLifecycle()
    val songMetadata by viewModel.songMetadata.collectAsStateWithLifecycle()

    var currentSongPosition by remember {
        mutableFloatStateOf(playbackPosition)
    }

    LaunchedEffect(key1 = Unit) {
        viewModel.playOrPause()
    }

    LaunchedEffect(key1 = playbackPosition) {
        currentSongPosition = playbackPosition
    }

    LaunchedEffect(key1 = isPlaying) {
        while (isPlaying) {
            currentSongPosition += 1000.0f
            delay(1000)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        stringResource(id = R.string.now_playing_title),
                        style = MaterialTheme.typography.titleMedium
                    )
                },
                navigationIcon = {
                    IconButton(onClick = {
                        popBackStack()
                    }) {
                        Icon(
                            imageVector = Icons.Rounded.ArrowBack,
                            contentDescription = "Back",
                            modifier = Modifier.size(24.dp),
                            tint = MaterialTheme.colorScheme.onPrimary
                        )
                    }
                },
                actions = {
                    IconButton(
                        onClick = {
                            navigateToSettings(Settings)
                        }
                    ) {
                        Icon(
                            imageVector = ImageVector.vectorResource(R.drawable.ic_settings),
                            contentDescription = "Settings",
                            modifier = Modifier.size(24.dp),
                            tint = MaterialTheme.colorScheme.onPrimary
                        )
                    }
                },
            )
        },
        content = { padding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                verticalArrangement = Arrangement.SpaceAround,
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                // Album art
                Image(
                    painter = songMetadata.getArtworkPainter(),
                    contentDescription = "Album Art",
                    contentScale = ContentScale.Fit,
                    modifier = Modifier
                        .padding(24.dp)
                        .size(260.dp)
                        .clip(RoundedCornerShape(percent = 18))
                )
                Column(
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = if (songMetadata != null) songMetadata?.title.toString() else "Loading",
                        fontSize = 24.sp,
                        color = MaterialTheme.colorScheme.onPrimary,
                        fontWeight = FontWeight.Bold,
                    )
                    Text(
                        text = if (songMetadata != null) songMetadata?.artist.toString() else "Loading",
                        fontSize = 18.sp,
                        color = MaterialTheme.colorScheme.secondary,
                        modifier = Modifier.absolutePadding(bottom = 16.dp)
                    )
                }
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    Slider(
                        value = currentSongPosition,
                        onValueChange = {
                            viewModel.setMusicProgress(it)
                        },
                        modifier = Modifier.padding(horizontal = 16.dp),
                        valueRange = 0f..songDuration
                    )

                    Row(
                        modifier = Modifier
                            .padding(vertical = 16.dp, horizontal = 16.dp)
                            .fillMaxWidth()
                            .height(92.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(
                            16.dp, Alignment.CenterHorizontally
                        )
                    ) {
                        IconButton(
                            onClick = {
                                viewModel.prev()
                            },
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.SkipPrevious,
                                contentDescription = "Skip to previous",
                                modifier = Modifier.size(45.dp),
                                tint = MaterialTheme.colorScheme.secondary
                            )
                        }
                        IconButton(
                            onClick = {
                                viewModel.playOrPause()
                            },
                            modifier = Modifier
                                .size(92.dp)
                                .padding(horizontal = 8.dp)
                        ) {
                            Image(
                                painter = rememberVectorPainter(
                                    ImageVector.vectorResource(
                                        if (isPlaying) R.drawable.ic_pause_filled
                                        else R.drawable.ic_play_filled
                                    )
                                ),
                                contentDescription = "Play button icon"
                            )
                        }
                        IconButton(
                            onClick = {
                                viewModel.next()
                            }
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.SkipNext,
                                contentDescription = "Skip to next",
                                modifier = Modifier.size(45.dp),
                                tint = MaterialTheme.colorScheme.secondary
                            )
                        }
                    }
                }
            }
        }
    )
}