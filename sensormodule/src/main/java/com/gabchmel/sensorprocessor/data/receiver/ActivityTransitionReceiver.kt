package com.gabchmel.sensorprocessor.data.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.gabchmel.common.utils.bindService
import com.gabchmel.sensorprocessor.data.service.SensorDataProcessingService
import com.google.android.gms.location.ActivityTransition
import com.google.android.gms.location.ActivityTransitionResult
import com.google.android.gms.location.DetectedActivity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async


class ActivityTransitionReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        if (ActivityTransitionResult.hasResult(intent)) {
            val activityResult = ActivityTransitionResult.extractResult(intent)

            activityResult?.let { result ->
                for (event in result.transitionEvents) {
                    val activity = detectActivityType(event.activityType)
                    val transition = detectTransitionType(event.transitionType)

                    context.sendBroadcast(Intent("MyAction"))

                    val sensorProcessService = CoroutineScope(Dispatchers.Default).async {
                        val service =
                            context.bindService(SensorDataProcessingService::class.java)

                        if (service.createModel()) {
                                val input = service.triggerPrediction()
//                                CollectedSensorDataFragment.updateUI(input)
                        }

                        service.measuredSensorValues.value.userActivity = activity

                        service
                    }
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