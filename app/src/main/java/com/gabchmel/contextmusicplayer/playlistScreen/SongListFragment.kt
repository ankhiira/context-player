package com.gabchmel.contextmusicplayer.playlistScreen

import android.Manifest
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.app.ActivityCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.gabchmel.contextmusicplayer.R
import com.gabchmel.contextmusicplayer.theme.JetnewsTheme
import com.gabchmel.predicitonmodule.PredictionModel
import com.google.accompanist.swiperefresh.SwipeRefresh
import com.google.accompanist.swiperefresh.rememberSwipeRefreshState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow


class SongListFragment : Fragment() {

    private val viewModel: SongListViewModel by viewModels()

    private var _isPermGranted = MutableStateFlow(false)
    private var isPermGranted: StateFlow<Boolean> = _isPermGranted

    // permissions callback
    private val requestPermissionLauncher =
        registerForActivityResult(
            ActivityResultContracts.RequestPermission()
        ) { isGranted: Boolean ->
            if (isGranted) {
                // permission is granted
                _isPermGranted.value = true
            }
        }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // compose layout for this fragment
        return ComposeView(requireContext()).apply {
            setContent {
                val songs by viewModel.songs.collectAsState()

                val input = floatArrayOf(
                    0.781831f,
                    0.265953f,
                    0.410050f,
                    -0.872427f,
                    -0.522189f,
                    0.62349f,
                    0.85283f
                )

                val predictionModel = PredictionModel(requireContext())
                val songToPlay = predictionModel.predict(input)

//                // TODO bude vzdy not null?
//                lateinit var songURI: Uri
//
//                for (song in songs!!) {
//                    if ("$song.title,$song.author".hashCode().toUInt() == songToPlay) {
//                        songURI = song.URI
//                    }
//                }
//
//                val play = true
//                findNavController().navigate(
//                    SongListFragmentDirections.actionSongListFragmentToHomeFragment(
//                        songURI,
//                        play
//                    )
//                )

                JetnewsTheme {
//                    fun ScaffoldDemo() {
                    val onPrimary = MaterialTheme.colors.onPrimary

                    Scaffold(
                        topBar = {
                            TopAppBar(
                                title = {
                                    Text(
                                        "Song List",
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.fillMaxWidth(),
                                        textAlign = TextAlign.Center
                                    )
                                },
                                actions = {
                                    IconButton(
                                        onClick = { findNavController().navigate(R.id.settingsFragment) },
                                    ) {
                                        Icon(
                                            imageVector = ImageVector.vectorResource(R.drawable.ic_settings),
                                            contentDescription = "Settings",
                                            modifier = Modifier.fillMaxHeight(0.4f),
                                            tint = onPrimary
                                        )
                                    }
                                },
                                elevation = 0.dp,
//                                backgroundColor = Color.Transparent
                            )
                        },
                        content = {
                            val granted by isPermGranted.collectAsState()

                            if ((ActivityCompat.checkSelfPermission(
                                    requireContext(),
                                    Manifest.permission.READ_EXTERNAL_STORAGE
                                ) != PackageManager.PERMISSION_GRANTED) && !granted
                            ) {
                                Column {
                                    Text("The permission to access external storage needed.")
                                    Button({

                                        requestPermissionLauncher.launch(Manifest.permission.READ_EXTERNAL_STORAGE)

                                    }) {
                                        Text("Grant permission")
                                    }
                                }
                            } else if (granted || (ActivityCompat.checkSelfPermission(
                                    requireContext(),
                                    Manifest.permission.READ_EXTERNAL_STORAGE
                                ) == PackageManager.PERMISSION_GRANTED)
                            ) {
                                Column {

                                    val isRefreshing by viewModel.isRefreshing.collectAsState()

                                    viewModel.loadSongs()

                                    SwipeRefresh(
                                        state = rememberSwipeRefreshState(isRefreshing),
                                        onRefresh = { viewModel.refresh() },
                                    ) {
                                        Column {
//                                            Text(
//                                                "Song list name",
//                                                color = primary,
//                                                fontWeight = FontWeight.Bold,
//                                                fontSize = 18.sp,
//                                                modifier = Modifier
//                                                    .align(alignment = Alignment.CenterHorizontally)
//                                                    .padding(vertical = 16.dp)
//                                            )

                                            if (songs != null)
                                                LazyColumn {
                                                    items(songs!!) { song ->
                                                        SongRow(song)
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
    @Composable
    fun SongRow(song: Song) {

        val fontColor = MaterialTheme.colors.onPrimary
        Column(
            Modifier
                .clickable(onClick = {
                    val play = true
                    findNavController().navigate(
                        SongListFragmentDirections.actionSongListFragmentToHomeFragment(
                            song.URI,
                            play
                        )
                    )
                })
//                .padding(horizontal = 8.dp)
                .padding(8.dp)
                .fillMaxWidth()
        ) {
            Text(
                text = "${song.title}",
                fontWeight = FontWeight.W300,
                color = fontColor

            )
            Text(
                text = "${song.author}",
                fontSize = 12.sp,
                fontWeight = FontWeight.W300,
                color = fontColor,
                modifier = Modifier.alpha(0.54f)
            )
        }
    }

    // Function for preview
    @Preview
    @Composable
    fun ExampleSongRow() {
        SongRow(Song("Title", "author", Uri.EMPTY))
    }
}



