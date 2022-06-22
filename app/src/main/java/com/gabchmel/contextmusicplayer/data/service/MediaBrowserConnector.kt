package com.gabchmel.contextmusicplayer.data.service

import android.content.*
import android.net.Uri
import android.os.IBinder
import android.support.v4.media.MediaBrowserCompat
import android.support.v4.media.session.MediaControllerCompat
import android.util.Log
import androidx.core.app.NotificationManagerCompat
import androidx.core.os.bundleOf
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.whenCreated
import com.gabchmel.common.data.ConvertedData
import com.gabchmel.common.data.LocalBinder
import com.gabchmel.common.utils.bindService
import com.gabchmel.contextmusicplayer.BuildConfig
import com.gabchmel.contextmusicplayer.data.model.Song
import com.gabchmel.contextmusicplayer.ui.screens.settingsScreen.CollectedSensorDataFragment
import com.gabchmel.contextmusicplayer.ui.utils.notifications.PredictionNotificationCreator
import com.gabchmel.sensorprocessor.data.service.SensorProcessService
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.io.File
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine
import kotlin.reflect.KProperty1
import kotlin.reflect.full.primaryConstructor

class MediaBrowserConnector(val lifecycleOwner: LifecycleOwner, val context: Context) :
    LifecycleObserver {

    inner class BoundService(
        private val context: Context,
        val name: ComponentName?,
        val service: MediaPlaybackService,
        private val connection: ServiceConnection,
        private var isBound: Boolean
    ) {
        fun unbind() {
            if (isBound) {
                try {
                    context.unbindService(connection)
                    isBound = false
                } catch (e: Exception) {
                    Log.e("Exception", e.toString())
                }
            }
        }
    }

    // Broadcast Receiver listening to action performed by click on prediction notification buttons
    @Suppress("DEPRECATION")
    class ActionReceiver : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            val action = intent.getStringExtra("action")
            lifecycleOwnerNew.lifecycleScope.launch {
                when {
                    action.equals("actionPlay") -> play(predictedSong.URI)
                    action.equals("actionSkip") -> skip(predictedSong.URI)
                }
            }

            // After clicking on the notification button, dismiss the notification
            NotificationManagerCompat.from(context).cancel(678)

            // Close the notification tray
            context.sendBroadcast(Intent(Intent.ACTION_CLOSE_SYSTEM_DIALOGS))
        }

        // Send action to MediaPlaybackService to set predicted song for play
        private suspend fun play(songUri: Uri) {
            lifecycleOwnerNew.lifecycleScope.launch {
                mediaControllerNew.await().transportControls.playFromUri(songUri, null)
            }.join()
        }

        // Send action to MediaPlaybackService to set predicted song for play
        private suspend fun skip(songUri: Uri) {
            lifecycleOwnerNew.lifecycleScope.launch {
                mediaControllerNew.await().transportControls.sendCustomAction(
                    "skip",
                    bundleOf("songUri" to songUri)
                )
            }.join()
        }
    }

    private lateinit var mediaBrowser: MediaBrowserCompat
    private val boundService = CompletableDeferred<BoundService>()
    private var contextData = ConvertedData()

    private val sensorProcessService = lifecycleOwner.lifecycleScope.async {
        lifecycleOwner.whenCreated {
            val service = context.bindService(SensorProcessService::class.java)
            if (service.createModel()) {
                contextData = service.triggerPrediction()
                CollectedSensorDataFragment.updateUI(contextData)
            }
            service
        }
    }

    private val prediction = flow {
        emitAll(sensorProcessService.await().prediction)
    }.filterNotNull()

    private val songs = flow {
        val service = boundService.await().service
        emitAll(service.songs)
    }.stateIn(lifecycleOwner.lifecycleScope, SharingStarted.Lazily, null)

    init {
        lifecycleOwner.lifecycleScope.launch {
            lifecycleOwner.whenCreated {
                lifecycleOwnerNew = lifecycleOwner

                // Register receiver of the notification button action
                context.registerReceiver(ActionReceiver(), IntentFilter("action"))

                val sensorProcessService = sensorProcessService.await()

                when {
                    !BuildConfig.IS_DEBUG -> {
                        if (sensorProcessService.hasContextChanged()) {
                            createMediaBrowser()
                            mediaBrowser.connect()
                            identifyPredictedSong()
                        }
                        // Save current sensor values to later detect if the context changed
                        sensorProcessService.saveSensorValuesToSharedPrefs()
                    }
                    else -> {
                        createMediaBrowser()
                        mediaBrowser.connect()
                        identifyPredictedSong()
                    }
                }
            }
        }
    }

    // Bind to service function inspired by: https://stackoverflow.com/questions/48381902/wait-for-service-to-be-bound-using-coroutines
    @Suppress("UNCHECKED_CAST")
    suspend fun bindServiceAndWait(context: Context, intent: Intent, flags: Int) =
        suspendCoroutine<BoundService> { continuation ->
            val conn = object : ServiceConnection {
                override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
                    val binder = service as LocalBinder<MediaPlaybackService>
                    val serviceVal = binder.getService()
                    continuation.resume(
                        BoundService(context, name, serviceVal, this, true)
                    )
                }

                override fun onServiceDisconnected(name: ComponentName?) {}
            }
            context.bindService(intent, conn, flags)
        }

    private fun identifyPredictedSong() {
        // Check predictions
        lifecycleOwner.lifecycleScope.launch {
            @Suppress("UNCHECKED_CAST")
            launch {
                prediction.collectLatest { prediction ->
                    // Save predictions with their input to CSV file
                    val predictionFile = File(context.filesDir, "predictions.csv")
                    var predictionString = "$prediction,"
                    ConvertedData::class.primaryConstructor?.parameters?.let { parameters ->
                        for (property in parameters) {
                            val propertyNew = contextData::class.members
                                .first { it.name == property.name } as KProperty1<Any, *>
                            predictionString += when (property.name) {
                                "wifi" -> "${contextData.wifi},"
                                else -> "${propertyNew.get(contextData)},"
                            }
                        }
                    }

                    predictionString = predictionString.dropLast(1).apply { this + "\n" }
                    predictionFile.appendText(predictionString)

                    // Find song that matches the prediction hash
                    for (song in songs.filterNotNull().first()) {
                        if ("${song.title},${song.author}"
                                .hashCode().toUInt().toString() == prediction
                        ) {
                            predictedSong = song
                            PredictionNotificationCreator.createNotification(context, predictedSong)

                            mediaBrowser.disconnect()
                        }
                    }
                }
            }
        }
    }

    private fun createMediaBrowser() {
        val connectionCallbacks = object : MediaBrowserCompat.ConnectionCallback() {
            override fun onConnected() {
                val mediaController = MediaControllerCompat(
                    context,
                    mediaBrowser.sessionToken
                )

                mediaControllerNew.complete(mediaController)

                mediaController.registerCallback(object : MediaControllerCompat.Callback() {})

                lifecycleOwner.lifecycleScope.launch {
                    val intent = Intent(context, MediaPlaybackService::class.java)
                    intent.putExtra("is_binding", true)
                    boundService.complete(
                        bindServiceAndWait(
                            context,
                            intent,
                            Context.BIND_AUTO_CREATE
                        )
                    )
                }
            }
        }

        // Setting MediaBrowser for connecting to the MediaBrowserService
        mediaBrowser = MediaBrowserCompat(
            context,
            ComponentName(context, MediaPlaybackService::class.java),
            connectionCallbacks,
            null
        )
    }

    companion object {
        lateinit var lifecycleOwnerNew: LifecycleOwner
        lateinit var predictedSong: Song
        val mediaControllerNew = CompletableDeferred<MediaControllerCompat>()
    }
}