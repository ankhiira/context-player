package com.gabchmel.contextmusicplayer.playback.nowPlaying.presentation

import android.app.Application
import android.content.ComponentName
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.media3.common.C
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import androidx.media3.common.Player
import androidx.media3.session.MediaBrowser
import androidx.media3.session.SessionToken
import com.gabchmel.common.utils.bindService
import com.gabchmel.contextmusicplayer.playback.musicService.MusicService
import com.gabchmel.contextmusicplayer.ui.NowPlaying
import com.gabchmel.sensorprocessor.data.service.SensorDataProcessingService
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.guava.asDeferred
import kotlinx.coroutines.launch

class NowPlayingViewModel(
    val key: NowPlaying,
    val app: Application
) : ViewModel() {

    private var mediaBrowserLocal: MediaBrowser? = null
    private val sessionToken = SessionToken(app, ComponentName(app, MusicService::class.java))

    private val _songMetadata = MutableStateFlow<MediaMetadata?>(null)
    val songMetadata: StateFlow<MediaMetadata?> = _songMetadata

    val songProgress = MutableStateFlow(0.0f)
    val songDuration = MutableStateFlow(0.0f)

    val isPlaying = MutableStateFlow(false)

    init {
        viewModelScope.launch {
            val mediaBrowser =
                MediaBrowser.Builder(app, sessionToken)
                    .buildAsync().asDeferred().await()

            val mediaItem = MediaItem.Builder()
                .setUri(key.songUri)
                .build()

            mediaBrowserLocal = mediaBrowser

            // Bind to SensorProcessService to later write to the file
            val sensorDataProcessingService = app.bindService(SensorDataProcessingService::class.java)

            mediaBrowser.setMediaItem(mediaItem)
            mediaBrowser.prepare()
            mediaBrowser.play()
            mediaBrowser.addListener(object : Player.Listener {
                override fun onMediaMetadataChanged(mediaMetadata: MediaMetadata) {
                    _songMetadata.value = mediaMetadata
                }

                override fun onEvents(player: Player, events: Player.Events) {
                    super.onEvents(player, events)

                    songDuration.value =
                        if (player.duration != C.TIME_UNSET) player.duration.toFloat()
                        else 0.0f
                    songProgress.value =
                        if (player.duration != C.TIME_UNSET) player.currentPosition.toFloat()
                        else 0.0f

                    isPlaying.value = player.isPlaying

                    if (player.isPlaying) {
                        val songId =
                            "${player.mediaMetadata.title},${player.mediaMetadata.artist}"
                                .hashCode()
                                .toString()
                        viewModelScope.launch {
                            sensorDataProcessingService.writeToFile(songId)
                        }
                    }
                }
            })
        }
    }

    private fun play() {
        mediaBrowserLocal?.play()
        isPlaying.value = true
    }

    private fun pause() {
        mediaBrowserLocal?.pause()
        isPlaying.value = false
    }

    fun playOrPause() {
        if (mediaBrowserLocal?.isPlaying == true) this.pause()
        else this.play()
    }

    fun next() {
        mediaBrowserLocal?.seekToNext()
    }

    fun prev() {
        mediaBrowserLocal?.seekToPrevious()
    }

    fun setMusicProgress(progress: Float) {
        mediaBrowserLocal?.seekTo(progress.toLong())
    }

    class Factory(
        private val key: NowPlaying,
        private val app: Application
    ) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return NowPlayingViewModel(key, app) as T
        }
    }
}