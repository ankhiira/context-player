package com.gabchmel.contextmusicplayer.ui.screens.playlistScreen

import android.Manifest
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.support.v4.media.session.PlaybackStateCompat
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.BottomAppBar
import androidx.compose.material.Button
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Scaffold
import androidx.compose.material.Text
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Alignment.Companion.CenterHorizontally
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import com.gabchmel.contextmusicplayer.R
import com.gabchmel.contextmusicplayer.data.model.Song
import com.gabchmel.contextmusicplayer.isPermissionNotGranted
import com.gabchmel.contextmusicplayer.ui.theme.appFontFamily
import com.gabchmel.contextmusicplayer.ui.theme.spacing
import com.gabchmel.contextmusicplayer.utils.getAlbumArt
import com.gabchmel.contextmusicplayer.utils.getArtist
import com.gabchmel.contextmusicplayer.utils.getTitle
import com.google.accompanist.glide.rememberGlidePainter
import com.google.accompanist.swiperefresh.SwipeRefresh
import com.google.accompanist.swiperefresh.rememberSwipeRefreshState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SongListScreen(navController: NavHostController) {
    val viewModel: SongListViewModel = viewModel()
    val isPermissionGranted by remember { mutableStateOf(false) }
    val songs by viewModel.songs.collectAsState()
    val context = LocalContext.current

    // permissions callback
//    val requestPermissionLauncher =
//        registerForActivityResult(
//            ActivityResultContracts.RequestPermission()
//        ) { isGranted: Boolean ->
//            if (isGranted) {
//                // permission is granted
//                _isPermGranted.value = true
//            }
//        }

    BackHandler {
        // This part handles the back button event
        val action = Intent(Intent.ACTION_MAIN)
        action.addCategory(Intent.CATEGORY_HOME)
        action.flags = Intent.FLAG_ACTIVITY_NEW_TASK
//        startActivity(action)
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        text = stringResource(id = R.string.playlist_topbar_title),
                        fontSize = 20.sp,
                        fontFamily = appFontFamily
                    )
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
                            modifier = Modifier.size(24.dp),
                            tint = MaterialTheme.colors.onPrimary
                        )
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = Color.Black
                )
            )
        },
        content = { padding ->
            // Component to grant the permissions
            val permission =
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU)
                    Manifest.permission.READ_MEDIA_AUDIO
                else
                    Manifest.permission.READ_EXTERNAL_STORAGE

            when {
                isPermissionNotGranted(context, permission) && !isPermissionGranted -> {
                    Column(
                        modifier = Modifier
                            .padding(padding)
                            .padding(vertical = 16.dp)
                            .fillMaxSize()
                    ) {
                        Text(
                            text = "The permission to access external storage needed.",
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(MaterialTheme.spacing.small)
                        )
                        Column(
                            horizontalAlignment = CenterHorizontally,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Button({
//                                            openDirectory()
//                                requestPermissionLauncher.launch(permission)
                            }) {
                                Text("Grant permission")
                            }
                        }
                    }
                }

                isPermissionGranted || !isPermissionNotGranted(context, permission) -> {
                    Column {
                        val isRefreshing by viewModel.isRefreshing.collectAsState()
                        viewModel.loadSongs()

                        SwipeRefresh(
                            state = rememberSwipeRefreshState(isRefreshing),
                            onRefresh = { viewModel.refresh() },
                        ) {
                            songs?.let { songs ->
                                LazyColumn(
                                    modifier = Modifier.padding(
                                        MaterialTheme.spacing.medium
                                    )
                                ) {
                                    items(songs, key = { it.URI }) { song ->
                                        SongItem(
                                            song,
                                            onItemSelected = {
                                                navController.navigate("now_playing")
//                                                uri = song.URI,
//                                                                        play = true
                                            }
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        },
        bottomBar = {
            val musicState by viewModel.musicState.collectAsState()
            val musicMetadata by viewModel.musicMetadata.collectAsState()
            val connected by viewModel.connected.collectAsState()

            if (connected) {
                BottomAppBar {
                    Row(
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(end = 8.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 8.dp)
                        ) {
                            // Album art
                            Image(
                                painter = musicMetadata?.getAlbumArt()?.let {
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
                                    text = musicMetadata?.getTitle() ?: "Loading",
                                    color = MaterialTheme.colors.onPrimary
                                )
                                Text(
                                    text = musicMetadata?.getArtist() ?: "Loading",
                                    color = MaterialTheme.colors.onPrimary,
                                    modifier = Modifier.alpha(0.54f)
                                )
                            }
                        }

                        IconButton(
                            onClick = {
                                viewModel.playSong()
                            },
                            modifier = Modifier
                                .size(34.dp)
                                .padding(horizontal = 8.dp)
                        ) {
                            Icon(
                                painter = rememberVectorPainter(
                                    ImageVector.vectorResource(
                                        when (musicState?.state) {
                                            PlaybackStateCompat.STATE_PLAYING ->
                                                R.drawable.ic_pause_new

                                            else ->
                                                R.drawable.ic_play_button_arrowhead
                                        }
                                    )
                                ),
                                contentDescription = "Play",
                                tint = Color(0xFFB1B1B1)
                            )
                        }
                    }
                }
            }
        }
    )
}

// Function for preview
@Preview
@Composable
fun ExampleSongRow() {
    SongItem(
        song = Song(title = "Title", author = "author", albumArt = null, URI = Uri.EMPTY),
        onItemSelected = {}
    )
}