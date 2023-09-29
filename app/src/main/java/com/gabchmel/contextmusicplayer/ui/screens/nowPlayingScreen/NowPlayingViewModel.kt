package com.gabchmel.contextmusicplayer.ui.screens.nowPlayingScreen

import android.app.Application
import android.content.ComponentName
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.SavedStateHandle
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import androidx.media3.common.Player
import androidx.media3.session.MediaBrowser
import androidx.media3.session.MediaController
import androidx.media3.session.SessionToken
import com.gabchmel.contextmusicplayer.service.MusicService
import com.google.common.util.concurrent.ListenableFuture
import com.google.common.util.concurrent.MoreExecutors
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class NowPlayingViewModel(
    val app: Application,
    savedStateHandle: SavedStateHandle
) : AndroidViewModel(app) {

    private val uri: String = checkNotNull(savedStateHandle["uri"])

    private var mediaController: MediaController? = null
    private lateinit var browserFuture: ListenableFuture<MediaBrowser>
    private lateinit var mediaBrowser: MediaBrowser

    private val sessionToken = SessionToken(app, ComponentName(app, MusicService::class.java))
    private val mediaControllerFuture = MediaController.Builder(
        app,
        sessionToken
    ).buildAsync()


    private val playerListener = object : Player.Listener {
        override fun onPlayWhenReadyChanged(playWhenReady: Boolean, reason: Int) {
            println("event in MediaBrowser: reason=$reason")
            super.onPlayWhenReadyChanged(playWhenReady, reason)
        }
    }

    private val _songMetadata = MutableStateFlow<MediaMetadata?>(null)
    val songMetadata: StateFlow<MediaMetadata?> = _songMetadata

    val isPlaying = MutableStateFlow(false)
    val playbackPosition = MutableStateFlow(0.0f)
    val songDuration = MutableStateFlow(0.0f)

    init {
        val browserFuture = MediaBrowser.Builder(app, sessionToken).buildAsync()
        browserFuture.addListener({
            mediaBrowser = browserFuture.get()
            mediaBrowser.setMediaItem(MediaItem.fromUri(uri))
            Log.d(
                "MediaItem browserFuture",
                mediaBrowser.contentDuration.toString()
            )
            mediaBrowser.prepare()
            mediaBrowser.play()
        }, MoreExecutors.directExecutor())

//        viewModelScope.launch {
//            MediaBrowser.Builder(app, sessionToken)
//                .buildAsync()
//                .await()
//                .addListener(playerListener)
//        }

//        mediaControllerFuture.addListener(
//            {
//                mediaController = mediaControllerFuture.get()
//                isPlaying.value = mediaController?.isPlaying == true
//                playbackPosition.value = mediaController?.currentPosition?.toFloat() ?: 0.0f
//                mediaController?.setMediaItem(MediaItem.fromUri(uri))
//                Log.d(
//                    "MediaItem",
//                    mediaController?.currentMediaItem?.requestMetadata?.mediaUri.toString()
//                )
//                mediaController?.prepare()
//                songDuration.value = if (mediaController?.duration != C.TIME_UNSET)
//                    mediaController?.duration?.toFloat() ?: 0.0f else 0.0f
//                _songMetadata.value = mediaController?.mediaMetadata
//                Log.d("metadata", _songMetadata.value?.mediaType.toString())
//            },
//            MoreExecutors.directExecutor()
//        )
    }

    private fun play() {
        mediaController?.play()
    }

    private fun pause() {
        mediaController?.pause()
    }

    fun playOrPause() {
        if (mediaController?.isPlaying == true) this.pause()
        else this.play()
    }

    fun next() {
        mediaController?.seekToNext()
    }

    fun prev() {
        mediaController?.seekToPrevious()
    }

    fun setMusicProgress(progress: Float) {
        mediaController?.seekTo(progress.toLong())
    }
}