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
import androidx.lifecycle.whenCreated
import androidx.media3.common.MediaItem
import androidx.media3.session.MediaBrowser
import androidx.media3.session.SessionToken
import com.gabchmel.common.data.ConvertedData
import com.gabchmel.contextmusicplayer.BuildConfig
import com.gabchmel.contextmusicplayer.data.local.model.Song
import kotlinx.coroutines.guava.asDeferred
import kotlinx.coroutines.launch

class MediaBrowserConnector(
    private val lifecycleOwner: LifecycleOwner,
    val context: Context
) : LifecycleObserver {


    private var input = ConvertedData()

//    private val sensorProcessService = lifecycleOwner.lifecycleScope.async {
//        lifecycleOwner.whenCreated {
//            val service = context.bindService(SensorDataProcessingService::class.java)
//            if (service.createModel()) {
//                input = service.triggerPrediction()
//                val viewModel: CollectedSensorDataViewModel = view
//                viewModel.updateUI(input)
//            }
//            service
//        }
//    }

//    private var prediction = flow<String?> {
//        val sensorProcessService = sensorProcessService.await()
//        emitAll(sensorProcessService.prediction)
//    }.filterNotNull()

    // Broadcast Receiver listening to action performed by click on prediction notification buttons
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
//            lifecycleOwnerNew.lifecycleScope.launch {
//                mediaControllerNew.await().transportControls.sendCustomAction(
//                    "skip",
//                    bundleOf("songUri" to songUri)
//                )
//            }.join()
        }
    }

    init {
        val songPredictor = SongPredictor(
            context,
            lifecycleOwner
        )

        lifecycleOwner.lifecycleScope.launch {
            lifecycleOwner.whenCreated {
                lifecycleOwnerNew = lifecycleOwner

                // Register receiver of the notification button action
                context.registerReceiver(ActionReceiver(), IntentFilter("action"))

//                val sensorProcessService = sensorProcessService.await()

                when {
                    !BuildConfig.IS_DEBUG -> {
//                        if (sensorProcessService.hasContextChanged()) {
                            createMediaBrowser()
                            songPredictor.identifyPredictedSong()
//                        }

                        // Save current sensor values to later detect if the context changed
//                        sensorProcessService.saveSensorValuesToSharedPrefs()
                    }

                    else -> {
                        createMediaBrowser()
                        songPredictor.identifyPredictedSong()
                    }
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