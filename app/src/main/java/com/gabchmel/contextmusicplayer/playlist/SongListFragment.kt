package com.gabchmel.contextmusicplayer.playlist

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


class SongListFragment : Fragment() {

    private val viewModel: SongListViewModel by viewModels()

    // Register the permissions callback, which handles the user's response to the
    // system permissions dialog. Save the return value, an instance of
    // ActivityResultLauncher. You can use either a val, as shown in this snippet,
    // or a lateinit var in your onAttach() or onCreate() method.
    @RequiresApi(Build.VERSION_CODES.Q)
    private val requestPermissionLauncher =
        registerForActivityResult(
            ActivityResultContracts.RequestPermission()
        ) { isGranted: Boolean ->
            if (isGranted) {
                // Permission is granted. Continue the action or workflow in your
                // app.
                viewModel.loadSongs()
            } else {
                // Explain to the user that the feature is unavailable because the
                // features requires a permission that the user has denied. At the
                // same time, respect the user's decision. Don't link to system
                // settings in an effort to convince the user to change their
                // decision.
            }
        }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        return ComposeView(requireContext()).apply {
            setContent {
                JetnewsTheme {

//                    fun ScaffoldDemo() {
                    val materialBlue700 = MaterialTheme.colors.primary
                    val scaffoldState =
                        rememberScaffoldState(rememberDrawerState(DrawerValue.Open))

//                    var isRefreshing by remember { mutableStateOf(false) }

                    Scaffold(
                        scaffoldState = scaffoldState,
                        topBar = {
                            TopAppBar(
                                title = {
                                    Text(
                                        "Song List Name",
                                        color = materialBlue700
                                    )
                                },
                                backgroundColor = Color.White
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
//                                    PullToRefresh(
//                                        isRefreshing = isRefreshing,
//                                        onRefresh = {
//                                            isRefreshing = true
//                                            // update items and set isRefreshing = false
//                                        }
//                                    ) {
                                    Text(
                                        "Song list name",
                                        color = materialBlue700,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 18.sp,
                                        modifier = Modifier.align(alignment = Alignment.CenterHorizontally)
                                    )
                                    LazyColumn {
                                        items(viewModel.songs) { song ->
                                            SongRow(song)
                                        }
                                    }
//                                    }
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
    @Composable
    fun SongRow(song: Song) {
        Column(
            Modifier
                .clickable(onClick = {
                    findNavController().navigate(SongListFragmentDirections.actionSongListFragmentToHomeFragment(song.URI))
                })
                .padding(8.dp)
                .fillMaxSize()
        ) {
            Text(
                text = "${song.title}",
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "${song.author}",
                fontSize = 14.sp
            )
        }
    }

    // Function for preview
    @Preview
    @Composable
    fun ExampleSongRow() {
        SongRow(Song("Blala", "auti", Uri.EMPTY))
    }
}



