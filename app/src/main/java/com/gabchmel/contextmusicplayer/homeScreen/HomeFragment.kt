package com.gabchmel.contextmusicplayer.homeScreen

import android.annotation.SuppressLint
import android.content.ComponentName
import android.os.Bundle
import android.support.v4.media.MediaBrowserCompat
import android.support.v4.media.MediaMetadataCompat
import android.support.v4.media.session.MediaControllerCompat
import android.support.v4.media.session.PlaybackStateCompat
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.SeekBar
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.MutableLiveData
import com.gabchmel.contextmusicplayer.*
import com.gabchmel.contextmusicplayer.databinding.FragmentHomeBinding
import kotlinx.android.synthetic.main.fragment_home.*
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map


// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [HomeFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class HomeFragment : Fragment() {
    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null

    private lateinit var binding: FragmentHomeBinding

    private val viewModel: NowPlayingViewModel by viewModels()

    private lateinit var seekBar: SeekBar
    private lateinit var btnPlay: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            param1 = it.getString(ARG_PARAM1)
            param2 = it.getString(ARG_PARAM2)
        }
    }

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

        btnPlay.setOnClickListener {
            val pbState = viewModel.musicState.value?.state ?: return@setOnClickListener
            if (pbState == PlaybackStateCompat.STATE_PLAYING) {
                viewModel.pause()

                //                Preemptively set icon
                binding.btnPlay.setBackgroundResource(R.drawable.ic_play_arrow_black_24dp)
            } else {
                viewModel.play()

                //                Preemptively set icon
                binding.btnPlay.setBackgroundResource(R.drawable.ic_pause_black_24dp)
            }

        }

        seekBar.progress = 0

        subscribeSeekBar()

        viewModel.musicState.observe(viewLifecycleOwner) { state ->

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

    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param CHANNEL_ID Parameter 1.
         * @param notificationID Parameter 2.
         * @return A new instance of fragment BlankFragment.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(CHANNEL_ID: String, notificationID: String) =
            HomeFragment().apply {
                arguments = Bundle().apply {
                    putString("CHANNEL_ID", "channel")
                    putInt("notificationID", 1234)
                }
            }
    }
}