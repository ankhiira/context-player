package com.gabchmel.contextmusicplayer.playlistScreen

import android.Manifest
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.app.ActivityCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.gabchmel.contextmusicplayer.theme.JetnewsTheme
import com.google.accompanist.swiperefresh.SwipeRefresh
import com.google.accompanist.swiperefresh.rememberSwipeRefreshState


class SongListFragment : Fragment() {

    private val viewModel: SongListViewModel by viewModels()

    // Register the permissions callback
    private val requestPermissionLauncher =
        registerForActivityResult(
            ActivityResultContracts.RequestPermission()
        ) { isGranted: Boolean ->
            if (isGranted) {
                // Permission is granted. Continue the action or workflow in your app.
                viewModel.loadSongs()
            } else {
                // TODO permission denied
                // Explain to the user that the feature is unavailable because the
                // features requires a permission that the user has denied. At the
                // same time, respect the user's decision.
            }
        }

    @RequiresApi(Build.VERSION_CODES.Q)
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Create compose layout for this fragment
        return ComposeView(requireContext()).apply {
            setContent {
                val songs by viewModel.songs.collectAsState()

                JetnewsTheme {
//                    fun ScaffoldDemo() {
                    val materialBlue700 = MaterialTheme.colors.primary
                    val scaffoldState =
                        rememberScaffoldState(rememberDrawerState(DrawerValue.Open))

                    Scaffold(
                        scaffoldState = scaffoldState,
                        topBar = {
                            TopAppBar(
                                title = {
                                    Text(
                                        "Song List Name",
                                        color = materialBlue700,
                                    )
                                },
                                elevation = 0.dp,
                                backgroundColor = Color.Transparent
                            )
                        },
                        content = {
                            if (ActivityCompat.checkSelfPermission(
                                    requireContext(),
                                    Manifest.permission.READ_EXTERNAL_STORAGE
                                ) != PackageManager.PERMISSION_GRANTED
                            ) {
                                Column {
                                    Text("The permission to access external storage needed.")
                                    Button({

                                        requestPermissionLauncher.launch(Manifest.permission.READ_EXTERNAL_STORAGE)

                                    }) {
                                        Text("Give permission")
                                    }
                                }
                            } else {
                                Column {

                                    val isRefreshing by viewModel.isRefreshing.collectAsState()

                                    SwipeRefresh(
                                        state = rememberSwipeRefreshState(isRefreshing),
                                        onRefresh = { viewModel.refresh() },
                                    ) {
                                        Column {
                                            Text(
                                                "Song list name",
                                                color = materialBlue700,
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 18.sp,
                                                modifier = Modifier
                                                    .align(alignment = Alignment.CenterHorizontally)
                                                    .padding(vertical = 16.dp)
                                            )

                                            if (songs!=null)
                                                LazyColumn {
                                                var id = 0
                                                items(songs!!) { song ->
                                                    SongRow(song, id)
                                                    id++
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    )
//                    }
                }
            }
        }
    }

    // Function for creating one song compose object row
    @RequiresApi(Build.VERSION_CODES.Q)
    @Composable
    fun SongRow(song: Song, id: Int) {

        val fontColor = MaterialTheme.colors.onPrimary
        Column(
            Modifier
                .clickable(onClick = {
                    val play = true
                    findNavController().navigate(
                        SongListFragmentDirections.actionSongListFragmentToHomeFragment(
                            song.URI,
                            play,
                            id
                        )
                    )
                })
                .padding(8.dp)
                .fillMaxSize()
        ) {
            Text(
                text = "${song.title}",
                fontWeight = FontWeight.Bold,
                color = fontColor
            )
            Text(
                text = "${song.author}",
                fontSize = 14.sp,
                color = fontColor
            )
        }
    }

    // Function for preview
    @RequiresApi(Build.VERSION_CODES.Q)
    @Preview
    @Composable
    fun ExampleSongRow() {
        SongRow(Song("Title", "author", Uri.EMPTY),0)
    }
}



