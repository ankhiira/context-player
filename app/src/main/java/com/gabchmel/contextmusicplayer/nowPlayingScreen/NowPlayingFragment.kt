package com.gabchmel.contextmusicplayer.nowPlayingScreen

import android.annotation.SuppressLint
import android.os.Build
import android.os.Bundle
import android.support.v4.media.session.PlaybackStateCompat
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.SeekBar
import androidx.annotation.RequiresApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.navArgs
import com.gabchmel.contextmusicplayer.R
import com.gabchmel.contextmusicplayer.databinding.NowPlayingFragmentBinding
import com.gabchmel.contextmusicplayer.getArtist
import com.gabchmel.contextmusicplayer.getDuration
import com.gabchmel.contextmusicplayer.getTitle
import com.gabchmel.contextmusicplayer.theme.JetnewsTheme

class NowPlayingFragment : Fragment() {

    private lateinit var binding: NowPlayingFragmentBinding

    private val viewModel: NowPlayingViewModel by viewModels()

    private lateinit var seekBar: SeekBar
    private lateinit var btnPlay: Button
    private lateinit var btnNext: Button
    private lateinit var btnPrev: Button

    private val args: NowPlayingFragmentArgs by navArgs()

    @SuppressLint("RestrictedApi")
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        viewModel.args = args

        // Create ViewBinding for the HomeFragment
        binding = NowPlayingFragmentBinding.inflate(inflater, container, false)

//        return ComposeView(requireContext()).apply {
//            setContent {
//                View()
//            }
//        }
        val view = binding.root

        seekBar = binding.seekBar
        btnPlay = binding.btnPlay
        btnNext = binding.btnNext
        btnPrev = binding.btnPrev

        // Set onClickListener
        btnPlay.setOnClickListener {
            playSong()
        }

        btnNext.setOnClickListener {
            skipToNext()
        }

        btnPrev.setOnClickListener {
            skipToPrev()
        }

        seekBar.progress = 0

        subscribeSeekBar()

        // Observe change of musicState from viewModel
        viewModel.musicState.observe(viewLifecycleOwner) { state ->

            // Set seekBar Progress according to the current state
            seekBar.progress = state.getCurrentPosition(null).toInt()

            binding.btnPlay.setBackgroundResource(
                if (state.state == PlaybackStateCompat.STATE_PLAYING)
                    R.drawable.ic_pause_black_24dp
                else
                    R.drawable.ic_play_arrow_black_24dp
            )
        }

        viewModel.musicMetadata.observe(viewLifecycleOwner) { metadata ->
            // Max length of the seekBar (length of the song)
            seekBar.max = metadata.getDuration().toInt()

            binding.tvSongTitle.text = metadata.getTitle()
            binding.tvSongAuthor.text = metadata.getArtist()
        }

        return view
    }

    private fun playSong() {
        val pbState = viewModel.musicState.value?.state ?: return
        if (pbState == PlaybackStateCompat.STATE_PLAYING) {
            viewModel.pause()

            // Preemptively set icon
            binding.btnPlay.setBackgroundResource(R.drawable.ic_play_arrow_black_24dp)
        } else {
            if (viewModel.notPlayed) {
                viewModel.play(args.uri)
            } else {
                viewModel.play()
            }

            // Preemptively set icon
            binding.btnPlay.setBackgroundResource(R.drawable.ic_pause_black_24dp)
        }
    }

    private fun skipToNext() {
        viewModel.next()
    }

    private fun skipToPrev() {
        viewModel.prev()
    }

    private fun subscribeSeekBar() {

        // When the SeekBar changes, update the data in the ViewModel
        seekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
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

    fun onSongCompletion() {
        btnPlay.setBackgroundResource(R.drawable.ic_play_arrow_black_24dp)
        seekBar.progress = 0
    }
}

@Composable
fun View() {
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
                        )
                    },
                    navigationIcon = {
//                        AppBarIcon(
//                            icon = imageResource(
//                                id = R.drawable.ic_menu_black_24dp
//                            )
//                        ) {
//                            // Open nav drawer
//                        }
//                    modifier = Modifier.
                    }
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
                        imageVector =
                        ImageVector.vectorResource(R.drawable.ic_album_cover_vector),
                        contentDescription = "Album Art",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .clip(RoundedCornerShape(16.dp))
                            .fillMaxWidth()
                            .padding(32.dp)
                            .height(210.dp)
                    )

                    // Song name
                    Text(
                        text = "Song name",
                        fontSize = 24.sp,
                        color = materialYel400,
//                        modifier = Modifier.absolutePadding(top = 2.dp)
                    )

                    // Author
                    Text(
                        text = "Author",
                        fontSize = 24.sp,
                        color = materialYel400,
                        modifier = Modifier.absolutePadding(bottom = 16.dp)
                    )

                    // SeekBar
                    AndroidView(
                        { context ->
                            SeekBar(context).apply {
                                progress = 0
                            }
                        },
                        modifier = Modifier.fillMaxWidth()
                    )

                    Row(
                        modifier = Modifier
                            .padding(vertical = 8.dp, horizontal = 8.dp)
                            .fillMaxWidth(),
                        verticalAlignment = Alignment.Bottom,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        IconButton(
                            onClick = { /*TODO*/ },
                        ) {
                            Icon(
                                imageVector =
                                ImageVector.vectorResource(R.drawable.ic_skip_prev),
                                contentDescription = "Skip to previous",
                                tint = materialGrey400,
                                modifier = Modifier.size(25.dp)
                            )
                        }

                        IconButton(onClick = {
                            // playSong()
                        }) {
                            Icon(
                                imageVector =
                                ImageVector.vectorResource(R.drawable.ic_play),
                                contentDescription = "Play",
                                tint = materialBlue700,
                                modifier = Modifier
                                    .size(120.dp)
                            )

                        }

                        IconButton(onClick = { /*TODO*/ }) {
                            Icon(
                                imageVector =
                                ImageVector.vectorResource(R.drawable.ic_skip_next),
                                contentDescription = "Skip to next",
                                tint = materialGrey400,
                                modifier = Modifier.size(25.dp)
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
    View()
}