package com.gabchmel.contextmusicplayer.homeScreen

import android.content.ComponentName
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.support.v4.media.MediaBrowserCompat
import android.support.v4.media.MediaMetadataCompat
import android.support.v4.media.session.MediaControllerCompat
import android.support.v4.media.session.PlaybackStateCompat
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.SeekBar
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.MutableLiveData
import com.gabchmel.contextmusicplayer.MediaPlaybackService
import com.gabchmel.contextmusicplayer.R
import com.gabchmel.contextmusicplayer.databinding.FragmentHomeBinding
import kotlinx.android.synthetic.main.fragment_home.*


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

    private lateinit var runnable: Runnable
    private var handler = Handler(Looper.getMainLooper())

    private lateinit var mediaBrowser: MediaBrowserCompat

    val playbackState = MutableLiveData<PlaybackStateCompat>()

    private lateinit var binding: FragmentHomeBinding

    private val viewModel: NowPlayingViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            param1 = it.getString(ARG_PARAM1)
            param2 = it.getString(ARG_PARAM2)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        binding = FragmentHomeBinding.inflate(inflater, container, false)
        val view = binding.root

        binding.tvSongTitle.text = viewModel.musicMetadata.title
        binding.tvSongAuthor.text = viewModel.musicMetadata.artist

        val seekBar = binding.seekBar

        mediaBrowser = MediaBrowserCompat(
            activity,
            ComponentName(requireActivity(), MediaPlaybackService::class.java),
            connectionCallbacks,
            null // optional
        )

        mediaBrowser.connect()

        // Inflate the layout for this fragment
//        val view = inflater.inflate(R.layout.fragment_home, container, false)

        seekBar.progress = 0

        // Max length of the seekBar (length of the song)
//        seekBar.max = mediaPlayer.duration

        seekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {

                if (fromUser) {
//                    mediaPlayer.seekTo(progress)
                }
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {

            }

            override fun onStopTrackingTouch(seekBar: SeekBar?) {

            }
        })

        // To handle the moving of the seekBar while the song is playing
        runnable = Runnable {
//            seekBar.progress = mediaPlayer.currentPosition
            handler.postDelayed(runnable, 1000)
        }

        handler.postDelayed(runnable, 1000)

        // After the song finishes go to the initial design
//        mediaPlayer.setOnCompletionListener {
//            btnPlay.setBackgroundResource(R.drawable.ic_play_arrow_black_24dp)
//            seekBar.progress = 0
//        }

        return view
    }

    override fun onDestroy() {
        super.onDestroy()

        MediaControllerCompat.getMediaController(requireActivity())?.unregisterCallback(controllerCallback)
        mediaBrowser.disconnect()
    }

//    Create connection callback
    private val connectionCallbacks = object : MediaBrowserCompat.ConnectionCallback() {
        override fun onConnected() {

            mediaBrowser.sessionToken.also { token ->

                // Create MediaControllerCompat
                val mediaController = MediaControllerCompat(
                    requireContext(),
                    token
                )

                MediaControllerCompat.setMediaController(requireActivity(), mediaController)
            }

            buildTransportControls()
        }

        override fun onConnectionSuspended() {
            super.onConnectionSuspended()
        }

        override fun onConnectionFailed() {
            super.onConnectionFailed()
        }
    }

    fun buildTransportControls() {

        val mediaController = MediaControllerCompat.getMediaController(requireActivity())

        var playPause = btn_play.setOnClickListener {
            val pbState = mediaController.playbackState.state
            if (pbState == PlaybackStateCompat.STATE_PLAYING) {
                mediaController.transportControls.pause()
                binding.btnPlay.setBackgroundResource(R.drawable.ic_play_arrow_black_24dp)
            } else {
                mediaController.transportControls.play()
                binding.btnPlay.setBackgroundResource(R.drawable.ic_pause_black_24dp)
            }

        }

        // Display initial state
        val metadata = mediaController.metadata
        val pbState = mediaController.playbackState

        // Register a callback to stay in sync
        mediaController.registerCallback(controllerCallback)
    }

    private var controllerCallback = object : MediaControllerCompat.Callback() {

        override fun onMetadataChanged(metadata: MediaMetadataCompat?) {
            super.onMetadataChanged(metadata)
        }

        override fun onPlaybackStateChanged(state: PlaybackStateCompat?) {
            Log.d("TAG", "playback state changed")

//            super.onPlaybackStateChanged(state)

//            playbackState.postValue((state ?: STATE_PLAYING) as PlaybackStateCompat?)

        }
    }

    companion object {

        const val CHANNEL_ID = "channel"
        const val notificationID = 1234

        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment BlankFragment.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            HomeFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }
}