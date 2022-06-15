package com.gabchmel.contextmusicplayer.data.service

import android.app.NotificationChannel
import android.app.PendingIntent
import android.content.*
import android.net.Uri
import android.os.Build
import android.os.IBinder
import android.support.v4.media.MediaBrowserCompat
import android.support.v4.media.session.MediaControllerCompat
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.os.bundleOf
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.whenCreated
import com.gabchmel.common.data.ConvertedData
import com.gabchmel.common.data.LocalBinder
import com.gabchmel.common.utils.bindService
import com.gabchmel.contextmusicplayer.ui.MainActivity
import com.gabchmel.contextmusicplayer.ui.NotificationManager
import com.gabchmel.contextmusicplayer.R
import com.gabchmel.contextmusicplayer.data.model.Song
import com.gabchmel.contextmusicplayer.ui.settingsScreen.CollectedSensorDataFragment
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

    companion object {
        lateinit var lifecycleOwnerNew: LifecycleOwner
        lateinit var predictedSong: Song
        val mediaControllerNew = CompletableDeferred<MediaControllerCompat>()
    }

    inner class BoundService(
        private val context: Context,
        val name: ComponentName?,
        val service: MediaPlaybackService,
        private val connection: ServiceConnection,
        private var isBinded: Boolean
    ) {
        fun unbind() {
            if (isBinded) {
                try {
                    context.unbindService(connection)
                    isBinded = false
                } catch (e: Exception) {
                    Log.e("Exception", e.toString())
                }
            }
        }
    }

    // Binds to service and waits for onServiceConnected
    // Bind to service function inspired by: https://stackoverflow.com/questions/48381902/wait-for-service-to-be-bound-using-coroutines
    suspend fun bindServiceAndWait(context: Context, intent: Intent, flags: Int) =
        suspendCoroutine<BoundService> { continuation ->

            val conn = object : ServiceConnection {
                override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
                    val binder = service as LocalBinder<MediaPlaybackService>
                    val serviceVal = binder.getService()
                    continuation.resume(
                        BoundService(
                            context, name,
                            serviceVal, this, true
                        )
                    )

                }

                override fun onServiceDisconnected(name: ComponentName?) {
                }
            }
            context.bindService(intent, conn, flags)
        }

    private lateinit var mediaBrowser: MediaBrowserCompat
    val bs = CompletableDeferred<BoundService>()
    private var input = ConvertedData()

    private val sensorProcessService = lifecycleOwner.lifecycleScope.async {
        lifecycleOwner.whenCreated {
            val service = context.bindService(SensorProcessService::class.java)
            if (service.createModel()) {
                input = service.triggerPrediction()
                CollectedSensorDataFragment.updateUI(input)
            }
            service
        }
    }

    private var prediction = flow {
        val sensorProcessService = sensorProcessService.await()
        emitAll(sensorProcessService.prediction)
    }.filterNotNull()

    private var songs = flow {
        // Service awaits for complete call
        val service = bs.await().service
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

            mediaControllerNew.complete(mediaController)

            // Register a callback to stay in sync
            mediaController.registerCallback(controllerCallback)

            lifecycleOwner.lifecycleScope.launch {
                val intent = Intent(context, MediaPlaybackService::class.java)
                intent.putExtra("is_binding", true)
                bs.complete(
                    bindServiceAndWait(
                        context,
                        intent, Context.BIND_AUTO_CREATE
                    )
                )
            }
        }
    }

    private var controllerCallback = object : MediaControllerCompat.Callback() {}

    init {
        lifecycleOwner.lifecycleScope.launch {
            lifecycleOwner.whenCreated {

                lifecycleOwnerNew = lifecycleOwner

                // Register receiver of the notification button action
                context.registerReceiver(ActionReceiver(), IntentFilter("action"))

                // Detect if the context changed so we should predict song
                val sensorProcessService = sensorProcessService.await()
                val hasContextChanged = sensorProcessService.detectContextChange()

//                if (hasContextChanged) {
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

    // Function identifying predicted song and sending custom action to create notification
    private fun setNotification() {
        // Check predictions
        lifecycleOwner.lifecycleScope.launch {

            launch {
                prediction.collectLatest { prediction ->
                    // Save predictions with their input to CSV file
                    val predictionFile = File(context.filesDir, "predictions.csv")
                    var predictionString = "$prediction,"
                    for (property in ConvertedData::class.primaryConstructor?.parameters!!) {
                        val propertyNew = input::class.members
                            .first { it.name == property.name } as KProperty1<Any, *>
                        predictionString += if (property.name == "wifi") {
                            "${input.wifi},"
                        } else {
                            "${propertyNew.get(input)},"
                        }
                    }
                    predictionString = predictionString.dropLast(1)
                    predictionString += "\n"
                    predictionFile.appendText(predictionString)

                    // Find song that matches the prediction hash
                    for (song in songs.filterNotNull().first()) {
                        if ("${song.title},${song.author}".hashCode().toUInt()
                                .toString() == prediction
                        ) {
                            predictedSong = song
                            createNotification(song)

//                            mediaBrowser.disconnect()
//                            bs.await().unbind()
                        }
                    }
                }
            }
        }
    }

    // Broadcast Receiver listening to action performed by click on prediction notification buttons
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

    // Function to create prediction notification
    private fun createNotification(song: Song) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            var description = "Test notification"
            val descriptionText = "description"
            val importance = android.app.NotificationManager.IMPORTANCE_DEFAULT
            val notificationChannel =
                NotificationChannel(NotificationManager.CHANNEL_ID, description, importance).apply {
                    description = descriptionText
                }

            val notificationManager: android.app.NotificationManager =
                context.getSystemService(Context.NOTIFICATION_SERVICE) as android.app.NotificationManager

            notificationManager.createNotificationChannel(notificationChannel)
        }

        // Specification of activity that will be executed after click on the notification will be performed
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }

        // Definition of the intent execution that execute the according activity
        val pendingIntent = PendingIntent.getActivity(
            context, 0, intent,
            PendingIntent.FLAG_UPDATE_CURRENT
        )

        val intentPlay = Intent(context, ActionReceiver::class.java).apply {
            this.putExtra("action", "actionPlay")
        }

        val pendingIntentPlay =
            PendingIntent.getBroadcast(
                context,
                0,
                intentPlay,
                PendingIntent.FLAG_IMMUTABLE
            )

        val intentSkip = Intent(context, ActionReceiver::class.java).apply {
            this.putExtra("action", "actionSkip")
        }

        val pendingIntentSkip =
            PendingIntent.getBroadcast(
                context,
                0,
                intentSkip,
                PendingIntent.FLAG_IMMUTABLE
            )

        // Definition of notification layout
        val builder = NotificationCompat.Builder(context, NotificationManager.CHANNEL_ID)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .setSilent(true)
            .setSmallIcon(R.drawable.ic_baseline_headset_24)
            .setContentTitle("Play this song?")
            .setContentText(song.title + " - " + song.author)
            .addAction(
                R.drawable.ic_play_arrow_black_24dp,
                "Play",
                pendingIntentPlay
            )
            .addAction(
                R.drawable.ic_skip_next_black_24dp,
                "Skip for now",
                pendingIntentSkip
            )
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)

        val notification = builder.build()

        with(NotificationManagerCompat.from(context)) {
            notify(678, notification)
        }
    }
}