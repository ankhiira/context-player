package com.gabchmel.contextmusicplayer

import android.app.Notification
import android.content.ComponentName
import android.graphics.BitmapFactory
import android.net.Uri
import android.support.v4.media.MediaBrowserCompat
import android.support.v4.media.MediaMetadataCompat
import android.support.v4.media.session.MediaControllerCompat
import android.support.v4.media.session.PlaybackStateCompat
import androidx.lifecycle.LifecycleService
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.whenCreated
import com.gabchmel.contextmusicplayer.extensions.getAlbumArt
import com.gabchmel.contextmusicplayer.extensions.getArtist
import com.gabchmel.contextmusicplayer.extensions.getTitle
import com.gabchmel.sensorprocessor.SensorProcessService
import com.gabchmel.sensorprocessor.startService
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class AutoPlaySongService : LifecycleService() {


    private lateinit var mediaBrowser: MediaBrowserCompat
    private var mediaBrowserConnectionCallback = MediaBrowserCompat.ConnectionCallback()

    lateinit var mediaController: MediaControllerCompat

    private val _musicState = MutableStateFlow<PlaybackStateCompat?>(null)
    val musicState: StateFlow<PlaybackStateCompat?> = _musicState

    private val _musicMetadata = MutableStateFlow<MediaMetadataCompat?>(null)
    val musicMetadata: StateFlow<MediaMetadataCompat?> = _musicMetadata

    var prediction = flow {
        val sensorProcessService = sensorProcessService.await()
        emitAll(sensorProcessService.prediction)
    }.filterNotNull()

    var notPlayed = true

    private lateinit var notification: Notification

    val sensorProcessService = lifecycleScope.async {
        whenCreated {
            val service = this@AutoPlaySongService.startService(SensorProcessService::class.java)

            service.createModel()
            service.triggerPrediction()

            service
        }
    }

    val service = CompletableDeferred<MediaPlaybackService>()

    private var songs = flow {
        // Service awaits for complete call
        val service = service.await()
        // Collects values from songs from services
        emitAll(service.songs)
    }.stateIn(lifecycleScope, SharingStarted.Lazily, null)

    private val connectionCallbacks = object : MediaBrowserCompat.ConnectionCallback() {
        override fun onConnected() {

            mediaBrowser.sessionToken.also { token ->

                // Create MediaControllerCompat
                mediaController = MediaControllerCompat(
                    this@AutoPlaySongService,
                    token
                )
            }

            // Display initial state
            val metadata = mediaController.metadata
            val pbstate = mediaController.playbackState

            _musicState.value = pbstate

            // Register a callback to stay in sync
            mediaController.registerCallback(controllerCallback)


            // Check predictions
            lifecycleScope.launch {

                launch {
                    prediction.collectLatest { prediction ->
                        for (song in songs.filterNotNull().first()) {
                            if ("${song.title},${song.author}".hashCode().toUInt().toString() == prediction) {
                                play(song.URI)
                                notPlayed = false
                            }
                        }
                    }
                }

                service.complete(MediaPlaybackService.getInstance(this@AutoPlaySongService))
            }
        }

        override fun onConnectionSuspended() {
            super.onConnectionSuspended()
        }

        override fun onConnectionFailed() {
            super.onConnectionFailed()
        }
    }

    override fun onCreate() {
        super.onCreate()

        // Setting MediaBrowser for connecting to the MediaBrowserService
        mediaBrowser = MediaBrowserCompat(
            this,
            ComponentName(this, MediaPlaybackService::class.java),
            connectionCallbacks,
            null
        )

        // Connects to the MediaBrowseService
        mediaBrowser.connect()

    }

    override fun onDestroy() {
        super.onDestroy()

        mediaController.unregisterCallback(controllerCallback)
        mediaBrowserConnectionCallback.onConnectionSuspended()
        mediaBrowser.disconnect()
    }

    private var controllerCallback = object : MediaControllerCompat.Callback() {

        override fun onMetadataChanged(metadata: MediaMetadataCompat) {
            _musicMetadata.value = metadata
        }

        override fun onPlaybackStateChanged(state: PlaybackStateCompat) {
            _musicState.value = state
        }
    }

    // To play for the first time
    fun play(uri: Uri) {
        mediaController.transportControls.playFromUri(uri, null)
    }

    // To play any other time
    fun play() {
        mediaController.transportControls.play()
    }

    fun pause() {
        mediaController.transportControls.pause()
    }

    fun next() {
        mediaController.transportControls.skipToNext()
    }

    fun prev() {
        mediaController.transportControls.skipToPrevious()
    }

    fun setMusicProgress(progress: Float) {
        mediaController.transportControls.seekTo(progress.toLong())
    }

    // Function to update a notification
    fun updateNotification(isPlaying: Boolean) {
        // Recreate notification
        notification = NotificationManager.createNotification(
            baseContext,
            null,
            _musicMetadata.value?.getTitle() ?: "unknown",
            _musicMetadata.value?.getArtist() ?: "unknown",
            _musicMetadata.value?.getAlbumArt() ?: BitmapFactory.decodeResource(
                resources,
                R.raw.album_cover_clipart
            ),
            isPlaying
        )

        NotificationManager.displayNotification(baseContext, notification)
    }

}