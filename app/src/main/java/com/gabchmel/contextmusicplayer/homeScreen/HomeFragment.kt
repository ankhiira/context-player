package com.gabchmel.contextmusicplayer.homeScreen

import android.annotation.SuppressLint
import android.os.Bundle
import android.support.v4.media.session.PlaybackStateCompat
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.SeekBar
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.navArgs
import com.gabchmel.contextmusicplayer.*
import com.gabchmel.contextmusicplayer.databinding.FragmentHomeBinding
import kotlinx.android.synthetic.main.fragment_home.*


class HomeFragment : Fragment() {

    private lateinit var binding: FragmentHomeBinding

    private val viewModel: NowPlayingViewModel by viewModels()

    private lateinit var seekBar: SeekBar
    private lateinit var btnPlay: Button

    val args: HomeFragmentArgs by navArgs()

    @SuppressLint("RestrictedApi")
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Create ViewBinding for the HomeFragment
        binding = FragmentHomeBinding.inflate(inflater, container, false)
        val view = binding.root

        seekBar = binding.seekBar
        btnPlay = binding.btnPlay

//        args.uri

        btnPlay.setOnClickListener {
            val pbState = viewModel.musicState.value?.state ?: return@setOnClickListener
            if (pbState == PlaybackStateCompat.STATE_PLAYING) {
                viewModel.pause()

                // Preemptively set icon
                binding.btnPlay.setBackgroundResource(R.drawable.ic_play_arrow_black_24dp)
            } else {
                viewModel.play()

                // Preemptively set icon
                binding.btnPlay.setBackgroundResource(R.drawable.ic_pause_black_24dp)
            }
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