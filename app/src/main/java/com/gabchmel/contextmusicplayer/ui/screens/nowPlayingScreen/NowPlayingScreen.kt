package com.gabchmel.contextmusicplayer.ui.screens.nowPlayingScreen

import android.annotation.SuppressLint
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.absolutePadding
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.navigation.NavHostController
import com.gabchmel.contextmusicplayer.R
import com.google.accompanist.glide.rememberGlidePainter


@OptIn(ExperimentalMaterial3Api::class)
@SuppressLint("RestrictedApi")
@Composable
fun NowPlayingScreen(
    navController: NavHostController
) {
    val viewModel: NowPlayingViewModel = viewModel()
    val isPlaying by viewModel.isPlaying.collectAsStateWithLifecycle()
    val playbackPosition by viewModel.playbackPosition.collectAsStateWithLifecycle()
    val songDuration by viewModel.songDuration.collectAsStateWithLifecycle()
    val songMetadata by viewModel.songMetadata.collectAsStateWithLifecycle()

//    LaunchedEffect(key1 = Unit) {
//        viewModel.playOrPause()
//    }

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
                        navController.popBackStack()
                    }) {
                        Icon(
                            imageVector = ImageVector.vectorResource(R.drawable.ic_back),
                            contentDescription = "Back",
                            modifier = Modifier.size(24.dp),
                            tint = MaterialTheme.colorScheme.onPrimary
                        )
                    }
                },
                actions = {
                    IconButton(
                        onClick = {
                            navController.navigate("settings")
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
                    painter = songMetadata?.artworkUri?.let {
                        rememberGlidePainter(it)
                    }
                        ?: rememberVectorPainter(ImageVector.vectorResource(R.drawable.ic_album_cover_vector)),
                    contentDescription = "Album Art",
                    contentScale = ContentScale.Fit,
                    modifier = Modifier
                        .size(215.dp)
                        .clip(RoundedCornerShape(percent = 10))
                        .padding(24.dp)
                )
                Column(
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = songMetadata?.displayTitle.toString() ?: "Loading",
                        fontSize = 24.sp,
                        color = MaterialTheme.colorScheme.onPrimary,
                        fontWeight = FontWeight.Bold,
                    )
                    Text(
                        text = songMetadata?.artist.toString() ?: "Loading",
                        fontSize = 18.sp,
                        color = MaterialTheme.colorScheme.secondary,
                        modifier = Modifier.absolutePadding(bottom = 16.dp)
                    )
                }
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    var sliderPosition by remember { mutableFloatStateOf(0f) }

                    LaunchedEffect(playbackPosition) {
                        if (isPlaying) {
                            sliderPosition = playbackPosition
                        }
                    }

                    Slider(
                        value = sliderPosition,
                        onValueChange = {
                            sliderPosition = it
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
                                imageVector = ImageVector.vectorResource(R.drawable.ic_skip_prev),
                                contentDescription = "Skip to previous",
                                modifier = Modifier.fillMaxHeight(0.4f),
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
                                imageVector = ImageVector.vectorResource(R.drawable.ic_skip_next),
                                contentDescription = "Skip to next",
                                modifier = Modifier.fillMaxHeight(0.4f),
                                tint = MaterialTheme.colorScheme.secondary
                            )
                        }
                    }
                }
            }
        }
    )
}