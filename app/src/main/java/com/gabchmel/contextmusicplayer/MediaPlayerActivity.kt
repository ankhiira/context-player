package com.gabchmel.contextmusicplayer

import android.content.ComponentName
import android.media.AudioManager
import android.os.Bundle
import android.support.v4.media.MediaBrowserCompat
import android.support.v4.media.MediaMetadataCompat
import android.support.v4.media.session.MediaControllerCompat
import android.support.v4.media.session.PlaybackStateCompat
import android.widget.Button
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.gabchmel.contextmusicplayer.databinding.FragmentHomeBinding
import com.gabchmel.contextmusicplayer.homeScreen.NowPlayingViewModel

class MediaPlayerActivity : AppCompatActivity() {

    private lateinit var mediaBrowser: MediaBrowserCompat

//    private lateinit var binding: FragmentHomeBinding
//    private val viewModel: NowPlayingViewModel by viewModels()


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
//
//        binding = FragmentHomeBinding.inflate(inflater, container, false)
//        val view = binding.root

//        binding.tvSongTitle.text = viewModel.musicMetadata.title
//        binding.tvSongAuthor.text = viewModel.musicMetadata.artist

        mediaBrowser = MediaBrowserCompat(
            this,
            ComponentName(this, MediaPlaybackService::class.java),
            connectionCallbacks,
            null // optional
        )
    }

    override fun onStart() {
        super.onStart()
        mediaBrowser.connect()
    }

    override fun onResume() {
        super.onResume()
        volumeControlStream = AudioManager.STREAM_MUSIC
    }

    override fun onStop() {
        super.onStop()
        MediaControllerCompat.getMediaController(this)?.unregisterCallback(controllerCallback)
        mediaBrowser.disconnect()
    }

    private val connectionCallbacks = object : MediaBrowserCompat.ConnectionCallback() {
        override fun onConnected() {

            mediaBrowser.sessionToken.also { token ->

                // Create MediaControllerCompat
                val mediaController = MediaControllerCompat(
                    this@MediaPlayerActivity,
                    token
                )

                MediaControllerCompat.setMediaController(this@MediaPlayerActivity, mediaController)
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

        val mediaController = MediaControllerCompat.getMediaController(this@MediaPlayerActivity)

        runOnUiThread {
            var playPause = findViewById<Button>(R.id.btn_play).apply {
                setOnClickListener {
                    val pbState = mediaController.playbackState.state
                    if (pbState == PlaybackStateCompat.STATE_PLAYING) {
                        mediaController.transportControls.pause()
                    } else {
                        mediaController.transportControls.play()
                    }
                }
            }
        }

        // Display initial state
        val metadata = mediaController.metadata
        val pbstate = mediaController.playbackState

        // Register a callback to stay in sync
        mediaController.registerCallback(controllerCallback)
    }

    private var controllerCallback = object : MediaControllerCompat.Callback() {

        override fun onMetadataChanged(metadata: MediaMetadataCompat?) {
            super.onMetadataChanged(metadata)
        }

        override fun onPlaybackStateChanged(state: PlaybackStateCompat?) {
            super.onPlaybackStateChanged(state)
        }
    }
}