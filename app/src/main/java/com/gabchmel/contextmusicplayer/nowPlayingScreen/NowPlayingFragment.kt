package com.gabchmel.contextmusicplayer.nowPlayingScreen

import android.annotation.SuppressLint
import android.os.Bundle
import android.support.v4.media.session.PlaybackStateCompat
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.SeekBar
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.gabchmel.contextmusicplayer.*
import com.gabchmel.contextmusicplayer.R
import com.gabchmel.contextmusicplayer.theme.JetnewsTheme
import com.google.accompanist.glide.rememberGlidePainter

class NowPlayingFragment : Fragment() {

    private val viewModel: NowPlayingViewModel by viewModels()

    private val args: NowPlayingFragmentArgs by navArgs()

    @SuppressLint("RestrictedApi")
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        viewModel.args = args

        return ComposeView(requireContext()).apply {
            setContent {
                View()
            }
        }
    }

    private fun playSong() {
        val pbState = viewModel.musicState.value?.state ?: return
        if (pbState == PlaybackStateCompat.STATE_PLAYING) {
            viewModel.pause()

//            // Preemptively set icon
//            binding.btnPlay.setBackgroundResource(R.drawable.ic_play_arrow_black_24dp)
        } else {
            if (viewModel.notPlayed) {
                viewModel.play(args.uri)
            } else {
                viewModel.play()
            }

//            // Preemptively set icon
//            binding.btnPlay.setBackgroundResource(R.drawable.ic_pause_black_24dp)
        }
    }


//    fun onSongCompletion() {
//        btnPlay.setBackgroundResource(R.drawable.ic_play_arrow_black_24dp)
//        seekBar.progress = 0
//    }

    @SuppressLint("RestrictedApi")
    @Composable
    fun View() {
        val viewModel = viewModel<NowPlayingViewModel>()
        val musicState by viewModel.musicState.collectAsState()
        val musicMetadata by viewModel.musicMetadata.collectAsState()


        JetnewsTheme {
            val materialBlue700 = MaterialTheme.colors.primary
            val materialGrey900 = MaterialTheme.colors.onBackground
            val materialGrey400 = MaterialTheme.colors.secondary
            val materialYel400 = MaterialTheme.colors.onPrimary

            val scaffoldState =
                rememberScaffoldState(rememberDrawerState(DrawerValue.Open))

            Scaffold(
                scaffoldState = scaffoldState,
                modifier = Modifier
                    .background(materialGrey900)
                    .fillMaxSize(),
                topBar = {
                    TopAppBar(
                        title = {
                            Text(
                                "Playing from library",
                                color = materialYel400,
                                fontSize = 18.sp,
                            )
                        },
                        navigationIcon = {
                            IconButton(onClick = { findNavController().navigate(R.id.song_list_Fragment) }) {
                                Icon(
                                    imageVector = ImageVector.vectorResource(R.drawable.ic_back),
                                    contentDescription = "Back",
                                    modifier = Modifier.fillMaxHeight(0.4f),
                                    tint = materialYel400
                                )
                            }
                        },
                        // TODO shadow pryc
                        backgroundColor = Color.Transparent
                    )
                },
                content = {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(8.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp),
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
                                .clip(RoundedCornerShape(16.dp))
                                .fillMaxWidth()
                                .padding(24.dp)
                                .height(210.dp)
                        )

                        // Title
                        Text(
                            text = musicMetadata?.getTitle() ?: "Loading",
                            fontSize = 24.sp,
                            color = materialYel400,
                            fontWeight = FontWeight.Bold,
                        )

                        // Author
                        Text(
                            text = musicMetadata?.getArtist() ?: "Loading",
                            fontSize = 18.sp,
                            color = materialGrey400,
                            modifier = Modifier
                                .absolutePadding(bottom = 16.dp)
                        )

                        // SeekBar
                        AndroidView(
                            { context ->
                                SeekBar(context).apply {
                                    progress = 0

                                    setOnSeekBarChangeListener(object :
                                        SeekBar.OnSeekBarChangeListener {
                                        override fun onProgressChanged(
                                            seekBar: SeekBar?,
                                            progress: Int,
                                            fromUser: Boolean
                                        ) {
                                            // Display the current progress of the SeekBar when the progress changes
                                            if (fromUser) {
                                                viewModel.setMusicProgress(progress)
                                            }
                                        }

                                        override fun onStartTrackingTouch(seekBar: SeekBar?) {
                                        }

                                        override fun onStopTrackingTouch(seekBar: SeekBar?) {
                                        }
                                    })
                                }
                            },
                            modifier = Modifier.fillMaxWidth()
                        ) { seekBar ->
                            // Max length of the seekBar (length of the song)
                            musicMetadata?.let { musicMetadata ->
                                seekBar.max = musicMetadata.getDuration().toInt()
                            }

                            // Set seekBar Progress according to the current state
                            musicState?.let { musicState ->

                                seekBar.progress = musicState.getCurrentPosition(null).toInt()
                            }
                        }

                        Row(
                            modifier = Modifier
                                .padding(vertical = 16.dp, horizontal = 8.dp)
                                .fillMaxWidth()
                                .height(120.dp),
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
                                    tint = materialGrey400,
                                    modifier = Modifier.fillMaxHeight(0.6f)
                                )
                            }

                            IconButton(
                                onClick = {
                                    playSong()
                                },
                                modifier = Modifier
                                    .size(92.dp)
                                    .shadow(elevation = 8.dp, shape = CircleShape)
                                    .padding(horizontal = 8.dp)
                            ) {
                                Image(
                                    painter =
                                    rememberVectorPainter(
                                        ImageVector.vectorResource(
                                            if (musicState?.state == PlaybackStateCompat.STATE_PLAYING)
                                                R.drawable.ic_pause_filled
                                            else
                                                R.drawable.ic_play_filled
                                        )
                                    ),
                                    contentDescription = "Play",
                                    modifier = Modifier
//                                        .shadow(elevation = 8.dp, shape = CircleShape)
                                )

                            }

                            IconButton(onClick = { viewModel.next() }) {
                                Icon(
                                    imageVector =
                                    ImageVector.vectorResource(R.drawable.ic_skip_next),
                                    contentDescription = "Skip to next",
                                    tint = materialGrey400,
                                    modifier = Modifier.fillMaxHeight(0.6f)
                                )

                            }
                        }
                    }
                }
            )
        }
    }

    @Preview
    @Composable
    fun DefPrev() {
//        View()
        Row(
            modifier = Modifier
                .padding(vertical = 16.dp, horizontal = 8.dp)
                .fillMaxWidth()
                .height(120.dp),
//                            verticalAlignment = Alignment.Bottom,
            horizontalArrangement = Arrangement.Center
        ) {
//            IconButton(
//                onClick = {
//                    viewModel.prev()
//                },
//            ) {
//                Icon(
//                    imageVector =
//                    ImageVector.vectorResource(R.drawable.ic_skip_prev),
//                    contentDescription = "Skip to previous",
////                    tint = materialGrey400,
//                    modifier = Modifier.fillMaxHeight(0.6f)
//                )
//            }

            IconButton(
                onClick = {
                    playSong()
                },
                modifier = Modifier
                    .fillMaxSize()
            ) {
                Image(
                    painter =
                    rememberVectorPainter(
                        ImageVector.vectorResource(
//                        if (musicState?.state == PlaybackStateCompat.STATE_PLAYING)
//                            R.drawable.ic_pause_filled
//                        else
                            R.drawable.ic_play_filled
                        )
                    ),
                    contentDescription = "Play",
                    modifier = Modifier
//                        .fillMaxHeight()
                        .shadow(elevation = 8.dp, shape = CircleShape)
                )

            }

//            IconButton(onClick = { viewModel.next() }) {
//                Icon(
//                    imageVector =
//                    ImageVector.vectorResource(R.drawable.ic_skip_next),
//                    contentDescription = "Skip to next",
////                    tint = materialGrey400,
//                    modifier = Modifier.fillMaxHeight(0.6f)
//                )
//
//            }
        }
    }
}

