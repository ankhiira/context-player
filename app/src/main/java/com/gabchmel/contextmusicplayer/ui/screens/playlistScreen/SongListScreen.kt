package com.gabchmel.contextmusicplayer.ui.screens.playlistScreen

import android.Manifest
import android.net.Uri
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.pullrefresh.PullRefreshIndicator
import androidx.compose.material.pullrefresh.pullRefresh
import androidx.compose.material.pullrefresh.rememberPullRefreshState
import androidx.compose.material3.Button
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Alignment.Companion.CenterHorizontally
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import com.gabchmel.contextmusicplayer.R
import com.gabchmel.contextmusicplayer.data.local.model.Song
import com.gabchmel.contextmusicplayer.ui.theme.spacing
import com.gabchmel.contextmusicplayer.utils.isPermissionNotGranted

@OptIn(
    ExperimentalMaterial3Api::class,
    ExperimentalMaterialApi::class
)
@Composable
fun SongListScreen(
    navController: NavHostController
) {
    val viewModel: SongListViewModel = viewModel()
    val songs by viewModel.songs.collectAsState()
    val context = LocalContext.current
    var isPermissionGranted by remember { mutableStateOf(false) }

    val requestPermissionLauncher =
        rememberLauncherForActivityResult(
            contract = ActivityResultContracts.RequestPermission()
        ) { isGranted ->
            if (isGranted) {
                isPermissionGranted = true
            }
        }

    Scaffold(
        modifier = Modifier.background(
            brush = Brush.verticalGradient(
                colors = listOf(
                    MaterialTheme.colorScheme.primary,
                    MaterialTheme.colorScheme.background
                )
            )
        ),
        containerColor = Color.Transparent,
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        text = stringResource(id = R.string.playlist_topbar_title),
                        color = MaterialTheme.colorScheme.onPrimary,
                        style = MaterialTheme.typography.titleLarge
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
                            tint = MaterialTheme.colorScheme.onPrimary
                        )
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = Color.Transparent
                )
            )
        },
        content = { padding ->
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
                                requestPermissionLauncher.launch(permission)
                            }) {
                                Text("Grant permission")
                            }
                        }
                    }
                }

                isPermissionGranted || !isPermissionNotGranted(context, permission) -> {
                    val refreshing by viewModel.isRefreshing.collectAsStateWithLifecycle()
                    val pullRefreshState =
                        rememberPullRefreshState(refreshing, { viewModel.refresh() })

                    LaunchedEffect(Unit) {
                        viewModel.loadSongs()
                    }

                    Box(
                        modifier = Modifier
                            .pullRefresh(pullRefreshState)
                            .padding(padding)
                    ) {
                        songs?.let { songs ->
                            LazyColumn(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(top = 16.dp)
                            ) {
                                items(songs, key = { it.uri }) { song ->
                                    val songUri = Uri.encode(song.uri.toString())
                                    SongItem(
                                        song,
                                        onItemSelected = {
                                            navController.navigate("now_playing/${songUri}")
                                        }
                                    )
                                }
                            }

                            PullRefreshIndicator(
                                refreshing,
                                pullRefreshState,
                                Modifier.align(Alignment.TopCenter)
                            )
                        }
                    }
                }
            }
        },
        bottomBar = {
            val musicState by viewModel.musicState.collectAsStateWithLifecycle()
            val musicMetadata by viewModel.musicMetadata.collectAsStateWithLifecycle()
            val connected by viewModel.connected.collectAsStateWithLifecycle()

            if (connected) {
                BottomPlayingIndicator(
                    songMetadata = musicMetadata,
                    playbackState = musicState,
                    onPlayClicked = {
                        viewModel.playSong()
                    }
                )
            }
        }
    )
}

@Preview
@Composable
fun ExampleSongRow() {
    SongItem(
        song = Song(
            title = "Title",
            artist = "author",
            metaData = null,
            uri = Uri.EMPTY
        ),
        onItemSelected = {}
    )
}