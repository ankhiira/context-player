package com.gabchmel.contextmusicplayer.ui.screens.playlistScreen

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.support.v4.media.session.PlaybackStateCompat
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.addCallback
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
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
import androidx.compose.material.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Alignment.Companion.CenterHorizontally
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.layout.ContentScale
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
import com.gabchmel.contextmusicplayer.data.model.Song
import com.gabchmel.contextmusicplayer.ui.theme.JetnewsTheme
import com.gabchmel.contextmusicplayer.ui.theme.appFontFamily
import com.gabchmel.contextmusicplayer.ui.theme.spacing
import com.gabchmel.contextmusicplayer.utils.getAlbumArt
import com.gabchmel.contextmusicplayer.utils.getArtist
import com.gabchmel.contextmusicplayer.utils.getTitle
import com.google.accompanist.glide.rememberGlidePainter
import com.google.accompanist.swiperefresh.SwipeRefresh
import com.google.accompanist.swiperefresh.rememberSwipeRefreshState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow


class SongListFragment : Fragment() {
    private val viewModel: SongListViewModel by viewModels()
    private val _isPermGranted = MutableStateFlow(false)
    private val isPermGranted: StateFlow<Boolean> = _isPermGranted

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
        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner) {
            // This part handles the back button event
            val action = Intent(Intent.ACTION_MAIN)
            action.addCategory(Intent.CATEGORY_HOME)
            action.flags = Intent.FLAG_ACTIVITY_NEW_TASK
            startActivity(action)
        }

        // Compose view for the Song list fragment
        return ComposeView(requireContext()).apply {
            setContent {
                JetnewsTheme {
                    val songs by viewModel.songs.collectAsState()
                    Scaffold(
                        topBar = {
                            TopAppBar(
                                title = {
                                    Text(
                                        "Song List",
                                        modifier = Modifier.fillMaxWidth(),
                                        textAlign = TextAlign.Center,
                                        fontFamily = appFontFamily
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
                                            tint = MaterialTheme.colors.onPrimary
                                        )
                                    }
                                },
                                elevation = 0.dp,
                            )
                        },
                        content = { padding ->
                            // Component to grant the permissions
                            val granted by isPermGranted.collectAsState()

                            val permission =
                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU)
                                    Manifest.permission.READ_MEDIA_AUDIO
                                else
                                    Manifest.permission.READ_EXTERNAL_STORAGE

                            when {
                                (ActivityCompat.checkSelfPermission(
                                    requireContext(),
                                    permission
                                ) != PackageManager.PERMISSION_GRANTED) && !granted -> {
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
                                                requestPermissionLauncher.launch(permission)
                                            }) {
                                                Text("Grant permission")
                                            }
                                        }
                                    }
                                }

                                granted || (ActivityCompat.checkSelfPermission(
                                    requireContext(),
                                    permission
                                ) == PackageManager.PERMISSION_GRANTED) -> {
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
                                                        SongRow(song)
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
                                                playSong()
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
            }
        }
    }

    // Function for creating one song compose object row
    @Composable
    fun SongRow(song: Song) {
        val fontColor = MaterialTheme.colors.onPrimary
        Row(
            Modifier
                .clickable(onClick = {
                    findNavController().navigate(
                        SongListFragmentDirections.actionSongListFragmentToHomeFragment(
                            uri = song.URI,
                            play = true
                        )
                    )
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
                    text = "${song.title}",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.W400,
                    color = fontColor
                )
                Text(
                    text = "${song.author}",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.W400,
                    color = fontColor,
                    modifier = Modifier.alpha(0.54f)
                )
            }
        }
    }

    // Function to play song from a bottom bar
    private fun playSong() {
        val playbackState = viewModel.musicState.value?.state ?: return
        when (playbackState) {
            PlaybackStateCompat.STATE_PLAYING -> {
                viewModel.pause()
                // Preemptively set icon
                // binding.btnPlay.setBackgroundResource(R.drawable.ic_play_arrow_black_24dp)
            }

            else -> {
                viewModel.play()
                // Preemptively set icon
                // binding.btnPlay.setBackgroundResource(R.drawable.ic_pause_black_24dp)
            }
        }
    }

    // Function for preview
    @Preview
    @Composable
    fun ExampleSongRow() {
        SongRow(Song("Title", "author", null, Uri.EMPTY))
    }
}



