package com.gabchmel.contextmusicplayer.ui.screens.nowPlayingScreen

import android.app.Application
import android.content.ComponentName
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.SavedStateHandle
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import androidx.media3.session.MediaBrowser
import androidx.media3.session.SessionToken
import com.gabchmel.contextmusicplayer.service.MusicService
import com.google.common.util.concurrent.MoreExecutors
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class NowPlayingViewModel(
    val app: Application,
    savedStateHandle: SavedStateHandle
) : AndroidViewModel(app) {

    private val uri: String = checkNotNull(savedStateHandle["uri"])

    private var mediaBrowser: MediaBrowser? = null
    private val sessionToken = SessionToken(app, ComponentName(app, MusicService::class.java))

    private val _songMetadata = MutableStateFlow<MediaMetadata?>(null)
    val songMetadata: StateFlow<MediaMetadata?> = _songMetadata

    val isPlaying = MutableStateFlow(false)
    val playbackPosition = MutableStateFlow(0.0f)
    val songDuration = MutableStateFlow(0.0f)

    init {
        val browserFuture =
            MediaBrowser.Builder(app, sessionToken)
                .buildAsync()

        browserFuture.addListener({
            mediaBrowser = browserFuture.get()
            mediaBrowser?.setMediaItem(MediaItem.fromUri(uri))
            Log.d(
                "MediaItem browserFuture",
                mediaBrowser?.contentDuration.toString()
            )
            mediaBrowser?.prepare()
            mediaBrowser?.play()
        }, MoreExecutors.directExecutor())
    }

    private fun play() {
        mediaBrowser?.play()
        isPlaying.value = true
    }

    private fun pause() {
        mediaBrowser?.pause()
        isPlaying.value = false
    }

    fun playOrPause() {
        if (mediaBrowser?.isPlaying == true) this.pause()
        else this.play()
    }

    fun next() {
        mediaBrowser?.seekToNext()
    }

    fun prev() {
        mediaBrowser?.seekToPrevious()
    }

    fun setMusicProgress(progress: Float) {
        mediaBrowser?.seekTo(progress.toLong())
    }
}