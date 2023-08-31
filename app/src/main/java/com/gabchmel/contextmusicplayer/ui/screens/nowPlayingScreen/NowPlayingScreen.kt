package com.gabchmel.contextmusicplayer.ui.screens.nowPlayingScreen

import android.annotation.SuppressLint
import android.support.v4.media.session.PlaybackStateCompat
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
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Scaffold
import androidx.compose.material.Slider
import androidx.compose.material.Text
import androidx.compose.material.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import com.gabchmel.contextmusicplayer.R
import com.gabchmel.contextmusicplayer.ui.theme.appFontFamily
import com.gabchmel.contextmusicplayer.utils.getAlbumArt
import com.gabchmel.contextmusicplayer.utils.getArtist
import com.gabchmel.contextmusicplayer.utils.getDuration
import com.gabchmel.contextmusicplayer.utils.getTitle
import com.google.accompanist.glide.rememberGlidePainter


@SuppressLint("RestrictedApi")
@Composable
fun NowPlayingScreen(navController: NavHostController) {

    val viewModel: NowPlayingViewModel = viewModel()
    val musicState by viewModel.musicState.collectAsStateWithLifecycle()
    val musicMetadata by viewModel.musicMetadata.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Playing from library",
                        fontFamily = appFontFamily
                    )
                },
                navigationIcon = {
                    IconButton(onClick = {
                        navController.popBackStack()
                    }) {
                        Icon(
                            imageVector = ImageVector.vectorResource(R.drawable.ic_back),
                            contentDescription = "Back",
                            modifier = Modifier.fillMaxHeight(0.4f),
                            tint = MaterialTheme.colors.onPrimary
                        )
                    }
                },
                actions = {
                    IconButton(
                        onClick = {
                            navController.navigate("settings")
                        },
                    ) {
                        Icon(
                            imageVector = ImageVector.vectorResource(R.drawable.ic_settings),
                            contentDescription = "Settings",
                            modifier = Modifier.fillMaxHeight(0.4f),
                            tint = MaterialTheme.colors.onPrimary
                        )
                    }
                },
                elevation = 0.dp,
                backgroundColor = Color.Transparent
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
                    painter = musicMetadata?.getAlbumArt()?.let {
                        rememberGlidePainter(it)
                    }
                        ?: rememberVectorPainter(ImageVector.vectorResource(R.drawable.ic_album_cover_vector)),
                    contentDescription = "Album Art",
                    contentScale = ContentScale.Fit,
                    modifier = Modifier
                        .clip(RoundedCornerShape(percent = 10))
                        .fillMaxWidth()
                        .padding(24.dp)
                        .height(215.dp)
                )

                Column(
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Title
                    Text(
                        text = musicMetadata?.getTitle() ?: "Loading",
                        fontSize = 24.sp,
                        color = MaterialTheme.colors.onPrimary,
                        fontWeight = FontWeight.Bold,
                    )

                    // Author
                    Text(
                        text = musicMetadata?.getArtist() ?: "Loading",
                        fontSize = 18.sp,
                        color = MaterialTheme.colors.secondary,
                        modifier = Modifier.absolutePadding(bottom = 16.dp)
                    )
                }

                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    // Slider
                    var sliderPosition by remember { mutableFloatStateOf(0f) }
                    val songLength = musicMetadata?.getDuration()?.toFloat() ?: 0.0f
                    val songPosition =
                        musicState?.getCurrentPosition(null)?.toFloat() ?: 0.0f

                    LaunchedEffect(songPosition) {
                        val pbState = viewModel.musicState.value?.state
                            ?: PlaybackStateCompat.STATE_PAUSED
                        if (pbState == PlaybackStateCompat.STATE_PLAYING) {
                            sliderPosition = songPosition
                        }
                    }

                    Slider(
                        value = sliderPosition,
                        onValueChange = {
                            sliderPosition = it
                            viewModel.setMusicProgress(it)
                        },
                        modifier = Modifier.padding(horizontal = 16.dp),
                        valueRange = 0f..songLength
                    )

                    Row(
                        modifier = Modifier
                            .padding(vertical = 16.dp, horizontal = 16.dp)
                            .fillMaxWidth()
                            .height(92.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        IconButton(
                            onClick = {
                                viewModel.prev()
                            },
                        ) {
                            Icon(
                                imageVector =
                                ImageVector.vectorResource(R.drawable.ic_skip_prev),
                                contentDescription = "Skip to previous",
                                tint = MaterialTheme.colors.secondary,
                                modifier = Modifier.fillMaxHeight(0.6f)
                            )
                        }
                        IconButton(
                            onClick = {
                                viewModel.playSong()
                            },
                            modifier = Modifier
                                .size(92.dp)
                                .padding(horizontal = 8.dp)
                        ) {
                            Image(
                                painter =
                                rememberVectorPainter(
                                    ImageVector.vectorResource(
                                        when (musicState?.state) {
                                            PlaybackStateCompat.STATE_PLAYING ->
                                                R.drawable.ic_pause_filled

                                            else ->
                                                R.drawable.ic_play_filled
                                        }
                                    )
                                ),
                                contentDescription = "Play",
                                modifier = Modifier
                            )
                        }

                        IconButton(onClick = { viewModel.next() }) {
                            Icon(
                                imageVector =
                                ImageVector.vectorResource(R.drawable.ic_skip_next),
                                contentDescription = "Skip to next",
                                tint = MaterialTheme.colors.secondary,
                                modifier = Modifier.fillMaxHeight(0.6f)
                            )
                        }
                    }
                }
            }
        }
    )
}

