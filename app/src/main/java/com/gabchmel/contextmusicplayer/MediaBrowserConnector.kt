package com.gabchmel.contextmusicplayer

import android.content.ComponentName
import android.content.Context
import android.graphics.BitmapFactory
import android.media.MediaMetadataRetriever
import android.net.Uri
import android.support.v4.media.MediaBrowserCompat
import android.support.v4.media.MediaMetadataCompat
import android.support.v4.media.session.MediaControllerCompat
import android.support.v4.media.session.PlaybackStateCompat
import androidx.core.os.bundleOf
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.whenCreated
import com.gabchmel.common.utilities.bindService
import com.gabchmel.contextmusicplayer.extensions.getAlbumArt
import com.gabchmel.contextmusicplayer.playlistScreen.Song
import com.gabchmel.sensorprocessor.SensorProcessService
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*

class MediaBrowserConnector(val lifecycleOwner: LifecycleOwner, val context: Context) :
    LifecycleObserver {

    private val servicePlayback =
        lifecycleOwner.lifecycleScope.async { MediaPlaybackService.getInstance(context) }

    private lateinit var mediaBrowser: MediaBrowserCompat
    val mediaController = CompletableDeferred<MediaControllerCompat>()

    val service = CompletableDeferred<MediaPlaybackService>()

//    lateinit var args: NowPlayingFragmentArgs

    var notPlayed = true

    private val sensorProcessService = lifecycleOwner.lifecycleScope.async {
        lifecycleOwner.whenCreated {
            val service = context.bindService(SensorProcessService::class.java)
            service.createModel()
            service.triggerPrediction()
            service
        }
    }

    var prediction = flow {
        val sensorProcessService = sensorProcessService.await()
        emitAll(sensorProcessService.prediction)
    }.filterNotNull()

    private var songs = flow {
        // Service awaits for complete call
        val service = service.await()
        // Collects values from songs from services
        emitAll(service.songs)
    }.stateIn(lifecycleOwner.lifecycleScope, SharingStarted.Lazily, null)

    // Update metadata
    private val metadataRetriever = MediaMetadataRetriever()

    private val _musicState = MutableStateFlow<PlaybackStateCompat?>(null)
    val musicState: StateFlow<PlaybackStateCompat?> = _musicState

    private val _musicMetadata = MutableStateFlow<MediaMetadataCompat?>(null)
    val musicMetadata: StateFlow<MediaMetadataCompat?> = _musicMetadata

    private val connectionCallbacks = object : MediaBrowserCompat.ConnectionCallback() {
        override fun onConnected() {

            // Create MediaControllerCompat
            val mediaController = MediaControllerCompat(
                context,
                mediaBrowser.sessionToken
            )

            this@MediaBrowserConnector.mediaController.complete(mediaController)

            // Display initial state
            val metadata = mediaController.metadata
            val pbstate = mediaController.playbackState

            _musicState.value = pbstate

            // Register a callback to stay in sync
            mediaController.registerCallback(controllerCallback)

//            // Play after fragment is open
//            if (args.play && notPlayed) {
//                play(args.uri)
//                notPlayed = false
//            }

            lifecycleOwner.lifecycleScope.launch {
                service.complete(MediaPlaybackService.getInstance(context))
            }
        }

        override fun onConnectionSuspended() {
            super.onConnectionSuspended()
        }

        override fun onConnectionFailed() {
            super.onConnectionFailed()
        }
    }

    private var controllerCallback = object : MediaControllerCompat.Callback() {

        override fun onMetadataChanged(metadata: MediaMetadataCompat) {
            _musicMetadata.value = metadata
        }

        override fun onPlaybackStateChanged(state: PlaybackStateCompat) {
            _musicState.value = state
        }
    }

    init {
        // pridat v pripade vyuziti druhe moznosti - @OnLifecycleEvent
//        lifecycleOwner.lifecycle.addObserver(this)
        lifecycleOwner.lifecycleScope.launch {
            lifecycleOwner.whenCreated {
                // Setting MediaBrowser for connecting to the MediaBrowserService
                mediaBrowser = MediaBrowserCompat(
                    context,
                    ComponentName(context, MediaPlaybackService::class.java),
                    connectionCallbacks,
                    null
                )

                // Connects to the MediaBrowseService
                mediaBrowser.connect()
                playSong()
            }
        }
    }

    // To play for the first time
    suspend fun play(uri: Uri) {
        // Wait for the playFromUri function to finish
        lifecycleOwner.lifecycleScope.launch {
            mediaController.await().transportControls.playFromUri(uri, null)
        }.join()
    }

    // To play any other time
    suspend fun play() {
        lifecycleOwner.lifecycleScope.launch {
            mediaController.await().transportControls.play()
        }.join()
    }

    suspend fun pause() {
        lifecycleOwner.lifecycleScope.launch {
            mediaController.await().transportControls.pause()
        }.join()
    }

    suspend fun next() {
        lifecycleOwner.lifecycleScope.launch {
            mediaController.await().transportControls.skipToNext()
        }.join()
    }

    suspend fun prev() {
        lifecycleOwner.lifecycleScope.launch {
            mediaController.await().transportControls.skipToPrevious()
        }.join()
    }

    suspend fun setMusicProgress(progress: Float) {
        lifecycleOwner.lifecycleScope.launch {
            mediaController.await().transportControls.seekTo(progress.toLong())
        }.join()
    }

    fun playSong() {
        // Check predictions
        lifecycleOwner.lifecycleScope.launch {

            launch {
                prediction.collectLatest { prediction ->
                    for (song in songs.filterNotNull().first()) {
                        if ("${song.title},${song.author}".hashCode().toUInt()
                                .toString() == prediction
                        ) {
//                            updateNotification(false, song)
                            getMetadata(song.URI)
//                            play(song.URI)
                            notPlayed = false
                        }
                    }
                }
            }
        }
    }

    suspend fun getMetadata(songUri: Uri) {
        lifecycleOwner.lifecycleScope.launch {
            mediaController.await().transportControls.sendCustomAction(
                "getMetadata",
                bundleOf("songUri" to songUri)
            )
        }.join()
    }

    // Function to update a notification
    private fun updateNotification(isPlaying: Boolean, song: Song) {

        // Recreate notification
        val notification = NotificationManager.createNotification(
            context,
            mediaBrowser.sessionToken,
            song.title ?: "unknown",
            song.author ?: "unknown",
            _musicMetadata.value?.getAlbumArt() ?: BitmapFactory.decodeResource(
                context.resources,
                R.raw.album_cover_clipart
            ),
            isPlaying
        )

        NotificationManager.displayNotification(context, notification)
    }

//    @OnLifecycleEvent(Lifecycle.Event.ON_CREATE)
//    fun connect() {
//
//    }
}