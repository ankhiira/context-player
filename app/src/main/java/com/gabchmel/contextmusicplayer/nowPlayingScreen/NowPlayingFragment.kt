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
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.navArgs
import com.gabchmel.contextmusicplayer.R
import com.gabchmel.contextmusicplayer.databinding.FragmentHomeBinding
import com.gabchmel.contextmusicplayer.theme.JetnewsTheme

class NowPlayingFragment : Fragment() {

    private lateinit var binding: FragmentHomeBinding

    private val viewModel: NowPlayingViewModel by viewModels()

    private lateinit var seekBar: SeekBar
    private lateinit var btnPlay: Button

    private val args: NowPlayingFragmentArgs by navArgs()

    @RequiresApi(Build.VERSION_CODES.O)
    @SuppressLint("RestrictedApi")
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        viewModel.args = args

        // Create ViewBinding for the HomeFragment
        binding = FragmentHomeBinding.inflate(inflater, container, false)
        return ComposeView(requireContext()).apply {
            setContent {
                JetnewsTheme {
                    Column {
                        val materialBlue700 = MaterialTheme.colors.primary
                        val scaffoldState =
                            rememberScaffoldState(rememberDrawerState(DrawerValue.Open))

                        Scaffold(
                            scaffoldState = scaffoldState,
                            topBar = {
                                TopAppBar(
                                    title = {
                                        Text(
                                            "Playing from library",
                                            color = Color.White,
                                        )
                                    },
                                    backgroundColor = MaterialTheme.colors.primary
                                )
                            },
                            content = {
                                Column(
                                    modifier = Modifier
                                        .padding(16.dp)
                                        .fillMaxWidth()
                                        .fillMaxHeight(),
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                ) {
                                    // Album art
                                    Image(
                                        imageVector =
                                        ImageVector.vectorResource(R.drawable.ic_album_cover_vector),
                                        contentDescription = "Album Art",
                                        modifier = Modifier.fillMaxWidth()
                                    )

                                    // Song name
                                    Text(
                                        text = "Song name",
                                        fontSize = 24.sp
                                    )

                                    // Author
                                    Text(text = "Author",
                                        fontSize = 16.sp)

                                    // SeekBar
                                    AndroidView({ context ->
                                        SeekBar(context).apply {
                                            progress = 50
                                        }
                                    })

                                    Row {
                                        IconButton(
                                            onClick = { /*TODO*/ },
                                            modifier = Modifier.then(
                                                Modifier.size(72.dp)
                                            )
                                        ) {
                                            Icon(
                                                painter =
                                                painterResource(id = R.drawable.ic_skip_previous_black_24dp),
                                                contentDescription = "Skip to previous",
                                                tint = Color.Red
                                            )
                                        }

                                        IconButton(onClick = { /*TODO*/ }) {
                                            Icon(
                                                painter =
                                                painterResource(id = R.drawable.ic_play_arrow_black_24dp),
                                                contentDescription = "Play"
                                            )

                                        }

                                        IconButton(onClick = { /*TODO*/ }) {
                                            Icon(
                                                painter =
                                                painterResource(id = R.drawable.ic_skip_next_black_24dp),
                                                contentDescription = "Skip to next"
                                            )

                                        }
                                    }
                                }
                            }
                        )
                    }
                }
            }
        }
//        val view = binding.root
//
//        seekBar = binding.seekBar
//        btnPlay = binding.btnPlay
//
//        // Set onClickListener
//        btnPlay.setOnClickListener {
//            playSong()
//        }
//
//        seekBar.progress = 0
//
//        subscribeSeekBar()
//
//        // Observe change of musicState from viewModel
//        viewModel.musicState.observe(viewLifecycleOwner) { state ->
//
//            // Set seekBar Progress according to the current state
//            seekBar.progress = state.getCurrentPosition(null).toInt()
//
//            binding.btnPlay.setBackgroundResource(
//                if (state.state == PlaybackStateCompat.STATE_PLAYING)
//                    R.drawable.ic_pause_black_24dp
//                else
//                    R.drawable.ic_play_arrow_black_24dp
//            )
//        }
//
//        viewModel.musicMetadata.observe(viewLifecycleOwner) { metadata ->
//            // Max length of the seekBar (length of the song)
//            seekBar.max = metadata.getDuration().toInt()
//
//            binding.tvSongTitle.text = metadata.getTitle()
//            binding.tvSongAuthor.text = metadata.getArtist()
//        }
//
//        return view
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