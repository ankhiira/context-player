package com.gabchmel.sensorprocessor.data.receiver

import android.content.BroadcastReceiver
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.IBinder
import com.gabchmel.sensorprocessor.data.service.SensorDataProcessingService
import com.google.android.gms.location.ActivityTransition
import com.google.android.gms.location.ActivityTransitionResult
import com.google.android.gms.location.DetectedActivity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async


class ActivityTransitionReceiver : BroadcastReceiver() {

    private lateinit var sensorService: SensorDataProcessingService
    private var bound = false

    private val connection = object : ServiceConnection {

        override fun onServiceConnected(className: ComponentName, service: IBinder) {
            // We've bound to LocalService, cast the IBinder and get LocalService instance.
            val binder = service as SensorDataProcessingService.LocalBinder
            sensorService = binder.getService()
            bound = true
        }

        override fun onServiceDisconnected(arg0: ComponentName) {
            bound = false
        }
    }

    override fun onReceive(context: Context, intent: Intent) {
        if (ActivityTransitionResult.hasResult(intent)) {
            val activityResult = ActivityTransitionResult.extractResult(intent)

            activityResult?.let { result ->
                for (event in result.transitionEvents) {
                    val activity = detectActivityType(event.activityType)
                    val transition = detectTransitionType(event.transitionType)

                    context.sendBroadcast(Intent("MyAction"))

                    val sensorProcessService = CoroutineScope(Dispatchers.Default).async {
                        val intent = Intent(context, SensorDataProcessingService::class.java)
                            .putExtra("is_binding", true)
                        val service =
                            context.bindService(intent, connection, Context.BIND_AUTO_CREATE)
                        if (sensorService.createModel()) {
                                val input = sensorService.triggerPrediction()
//                                CollectedSensorDataFragment.updateUI(input)
                        }
                        sensorService
                    }

//                    sensorService.sensor.value.currentState = activity
                }
            }
        }
    }

    private fun detectTransitionType(transitionType: Int): String {
        return when (transitionType) {
            ActivityTransition.ACTIVITY_TRANSITION_ENTER -> "ENTER"
            ActivityTransition.ACTIVITY_TRANSITION_EXIT -> "EXIT"
            else -> "UNKNOWN"
        }
    }

    private fun detectActivityType(activity: Int): String {
        return when (activity) {
            DetectedActivity.IN_VEHICLE -> "IN_VEHICLE"
            DetectedActivity.STILL -> "STILL"
            DetectedActivity.WALKING -> "WALKING"
            DetectedActivity.RUNNING -> "RUNNING"
            else -> "UNKNOWN"
        }
    }
}