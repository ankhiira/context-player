package com.gabchmel.contextmusicplayer.service

import android.content.BroadcastReceiver
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.Uri
import androidx.core.app.NotificationManagerCompat
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.lifecycleScope
import androidx.media3.common.MediaItem
import androidx.media3.session.MediaBrowser
import androidx.media3.session.SessionToken
import com.gabchmel.common.data.ConvertedData
import com.gabchmel.common.utils.bindService
import com.gabchmel.contextmusicplayer.BuildConfig
import com.gabchmel.contextmusicplayer.data.local.model.Song
import com.gabchmel.sensorprocessor.data.service.SensorDataProcessingService
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.guava.asDeferred
import kotlinx.coroutines.launch

class PredictionCreator(
    private val lifecycleOwner: LifecycleOwner,
    val context: Context
) : LifecycleObserver {

    private var input = ConvertedData()

    private val sensorProcessService = lifecycleOwner.lifecycleScope.async {
        val service = context.bindService(SensorDataProcessingService::class.java)
        if (service.createModel()) {
            input = service.triggerPrediction()
//            val viewModel: CollectedSensorDataViewModel = viewModel()
//            viewModel.updateUI(input)
        }
        service
    }

    private var prediction = flow {
        val sensorProcessService = sensorProcessService.await()
        emitAll(sensorProcessService.prediction)
    }.filterNotNull()

    /**
     * Listening to action performed by click on prediction notification buttons.
     *
     */
    class ActionReceiver : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            val action = intent.getStringExtra("action")
            lifecycleOwnerNew.lifecycleScope.launch {
                when {
                    action.equals("actionPlay") -> play(predictedSong.uri)
                    action.equals("actionSkip") -> skip(predictedSong.uri)
                }
            }

            // After clicking on the notification button, dismiss the notification
            NotificationManagerCompat.from(context).cancel(678)

            // Close the notification tray
            context.sendBroadcast(Intent(Intent.ACTION_CLOSE_SYSTEM_DIALOGS))
        }

        private suspend fun play(songUri: Uri) {
            lifecycleOwnerNew.lifecycleScope.launch {
                val mediaItem = MediaItem.Builder()
                    .setUri(songUri)
                    .build()

                mediaBrowser.setMediaItem(mediaItem)
            }.join()
        }

        // Send action to MediaPlaybackService to set predicted song for play
        private suspend fun skip(songUri: Uri) {
            lifecycleOwnerNew.lifecycleScope.launch {
//                mediaBrowser.transportControls.sendCustomAction(
//                    "skip",
//                    bundleOf("songUri" to songUri)
//                )
            }.join()
        }
    }

    init {
        val songPredictor = SongPredictor(
            context,
            lifecycleOwner
        )

        lifecycleOwner.lifecycleScope.launch {
            lifecycleOwnerNew = lifecycleOwner

            // Register receiver of the notification button action
            context.registerReceiver(ActionReceiver(), IntentFilter("action"))

//                val sensorProcessService = sensorProcessService.await()

            when {
                BuildConfig.FLAVOR != "debugVersion" -> {
                    if (sensorProcessService.await().hasContextChanged()) {
                        createMediaBrowser()
                        songPredictor.identifyPredictedSong()
                    }
                }

                else -> {
                    createMediaBrowser()
                    songPredictor.identifyPredictedSong()
                }
            }
        }
    }

    private suspend fun createMediaBrowser() {
        val sessionToken = SessionToken(context, ComponentName(context, MusicService::class.java))

        mediaBrowser = MediaBrowser.Builder(context, sessionToken)
            .buildAsync().asDeferred().await()
    }

    companion object {
        lateinit var lifecycleOwnerNew: LifecycleOwner
        lateinit var predictedSong: Song

        private lateinit var mediaBrowser: MediaBrowser
    }
}