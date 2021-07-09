package com.gabchmel.contextmusicplayer

import android.content.ComponentName
import android.content.Context
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
import com.gabchmel.sensorprocessor.SensorProcessService
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*

class MediaBrowserConnector(val lifecycleOwner: LifecycleOwner, val context: Context) :
    LifecycleObserver {

    private lateinit var mediaBrowser: MediaBrowserCompat
    val mediaController = CompletableDeferred<MediaControllerCompat>()

    val service = CompletableDeferred<MediaPlaybackService>()

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

    private val connectionCallbacks = object : MediaBrowserCompat.ConnectionCallback() {
        override fun onConnected() {

            // Create MediaControllerCompat
            val mediaController = MediaControllerCompat(
                context,
                mediaBrowser.sessionToken
            )

            this@MediaBrowserConnector.mediaController.complete(mediaController)

            // Register a callback to stay in sync
            mediaController.registerCallback(controllerCallback)

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
        }

        override fun onPlaybackStateChanged(state: PlaybackStateCompat) {
        }
    }

    init {
        // pridat v pripade vyuziti druhe moznosti - @OnLifecycleEvent
//        lifecycleOwner.lifecycle.addObserver(this)
        lifecycleOwner.lifecycleScope.launch {
            lifecycleOwner.whenCreated {

                // Detect if the context changed so we should predict song
                val sensorProcessService = sensorProcessService.await()
                val isContextChanged = sensorProcessService.detectContextChange()

//                if(isContextChanged) {
                    // Setting MediaBrowser for connecting to the MediaBrowserService
                    mediaBrowser = MediaBrowserCompat(
                        context,
                        ComponentName(context, MediaPlaybackService::class.java),
                        connectionCallbacks,
                        null
                    )

                    // Connects to the MediaBrowseService
                    mediaBrowser.connect()
                    setNotification()
//                }

                // Save current sensor values to later detect if the context changed
                sensorProcessService.saveSensorData()
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

    // Function identifying predicted song and sending custom action to create notification
    private fun setNotification() {
        // Check predictions
        lifecycleOwner.lifecycleScope.launch {

            launch {
                prediction.collectLatest { prediction ->
                    for (song in songs.filterNotNull().first()) {
                        if ("${song.title},${song.author}".hashCode().toUInt()
                                .toString() == prediction
                        ) {
                            getMetadata(song.URI)
                        }
                    }
                }
            }
        }
    }

    // Send action to MediaPlaybackService to set predicted song for play
    private suspend fun getMetadata(songUri: Uri) {
        lifecycleOwner.lifecycleScope.launch {
            mediaController.await().transportControls.sendCustomAction(
                "getMetadata",
                bundleOf("songUri" to songUri)
            )
        }.join()
    }

//    @OnLifecycleEvent(Lifecycle.Event.ON_CREATE)
//    fun connect() {
//
//    }
}