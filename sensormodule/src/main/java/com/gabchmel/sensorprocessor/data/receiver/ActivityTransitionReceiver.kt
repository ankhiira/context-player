package com.gabchmel.sensorprocessor.data.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.gabchmel.sensorprocessor.data.service.SensorProcessService
import com.google.android.gms.location.ActivityTransition
import com.google.android.gms.location.ActivityTransitionResult
import com.google.android.gms.location.DetectedActivity

class ActivityTransitionReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        if (ActivityTransitionResult.hasResult(intent)) {

            val result = ActivityTransitionResult.extractResult(intent)
            for (event in result!!.transitionEvents) {
                val activity = activityType(event.activityType)
                val transition = transitionType(event.transitionType)

                context.sendBroadcast(Intent("MyAction"))
                SensorProcessService._sensorData.value.currentState = activity
            }
        }
    }

    // Detect the type of transition
    private fun transitionType(transitionType: Int): String {
        return when (transitionType) {
            ActivityTransition.ACTIVITY_TRANSITION_ENTER -> "ENTER"
            ActivityTransition.ACTIVITY_TRANSITION_EXIT -> "EXIT"
            else -> "UNKNOWN"
        }
    }

    // Detect the type of activity
    private fun activityType(activity: Int): String {
        return when (activity) {
            DetectedActivity.IN_VEHICLE -> "IN_VEHICLE"
            DetectedActivity.STILL -> "STILL"
            DetectedActivity.WALKING -> "WALKING"
            DetectedActivity.RUNNING -> "RUNNING"
            else -> "UNKNOWN"
        }
    }
}