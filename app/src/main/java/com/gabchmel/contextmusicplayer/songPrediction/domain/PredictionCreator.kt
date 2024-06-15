package com.gabchmel.contextmusicplayer.songPrediction.domain

import android.annotation.SuppressLint
import android.content.BroadcastReceiver
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.Uri
import android.os.Build
import androidx.core.app.NotificationManagerCompat
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.lifecycleScope
import androidx.media3.common.MediaItem
import androidx.media3.session.MediaBrowser
import androidx.media3.session.SessionToken
import com.gabchmel.common.utils.bindService
import com.gabchmel.contextmusicplayer.BuildConfig
import com.gabchmel.contextmusicplayer.core.data.song.Song
import com.gabchmel.contextmusicplayer.core.domain.service.MusicService
import com.gabchmel.contextmusicplayer.songPrediction.presentation.notification.PredictionNotificationCreator.NOTIFICATION_ID
import com.gabchmel.sensorprocessor.data.service.SensorDataProcessingService
import kotlinx.coroutines.async
import kotlinx.coroutines.guava.asDeferred
import kotlinx.coroutines.launch

@SuppressLint("UnspecifiedRegisterReceiverFlag")
class PredictionCreator(
    private val lifecycleOwner: LifecycleOwner,
    val context: Context
) : LifecycleObserver {

    private val sensorProcessService = lifecycleOwner.lifecycleScope.async {
        val service = context.bindService(SensorDataProcessingService::class.java)
        if (service.createModel()) {
            service.triggerPrediction()
        }
        service
    }

    /**
     * Listening to an action performed by click on prediction notification buttons.
     *
     */
    class ActionReceiver(
        private val lifecycleOwner: LifecycleOwner,
        private val mediaBrowser: MediaBrowser,
        private val predictedSong: Song?
    ) : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            val action = intent.getStringExtra("action")
            lifecycleOwner.lifecycleScope.launch {
                when {
                    action.equals("actionPlay") -> predictedSong?.let { play(it.uri) }
                    action.equals("actionSkip") -> predictedSong?.let { skip(it.uri) }
                }
            }

            // After clicking on the notification button, dismiss the notification
            NotificationManagerCompat.from(context).cancel(NOTIFICATION_ID)

            // Close the notification
            context.sendBroadcast(Intent(Intent.ACTION_CLOSE_SYSTEM_DIALOGS))
        }

        private suspend fun play(songUri: Uri) {
            lifecycleOwner.lifecycleScope.launch {
                val mediaItem = MediaItem.Builder()
                    .setUri(songUri)
                    .build()

                mediaBrowser.setMediaItem(mediaItem)
                mediaBrowser.play()
            }.join()
        }

        private suspend fun skip(songUri: Uri) {
            lifecycleOwner.lifecycleScope.launch {
                val mediaItem = MediaItem.Builder()
                    .setUri(songUri)
                    .build()

                mediaBrowser.setMediaItem(mediaItem)
                //TODO skip
            }.join()
        }
    }

    init {
        lifecycleOwner.lifecycleScope.launch {
            val mediaBrowser = createMediaBrowser()
            val songPredictor = SongPredictor(context, lifecycleOwner)
            val predictedSong = if (BuildConfig.FLAVOR == "full") {
                if (sensorProcessService.await().hasContextChanged()) {
                    songPredictor.identifyPredictedSong()
                } else {
                    null
                }
            } else {
                songPredictor.identifyPredictedSong()
            }

            // Register receiver of the notification button action
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                context.registerReceiver(
                    ActionReceiver(lifecycleOwner, mediaBrowser, predictedSong),
                    IntentFilter("action"),
                    Context.RECEIVER_NOT_EXPORTED,
                )
            } else {
                context.registerReceiver(
                    ActionReceiver(lifecycleOwner, mediaBrowser, predictedSong),
                    IntentFilter("action")
                )
            }
        }
    }

    private suspend fun createMediaBrowser(): MediaBrowser {
        val sessionToken = SessionToken(context, ComponentName(context, MusicService::class.java))

        return MediaBrowser.Builder(context, sessionToken)
            .buildAsync().asDeferred().await()
    }
}