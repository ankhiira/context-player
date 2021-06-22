package com.gabchmel.sensorprocessor

import android.app.IntentService
import android.content.Intent
import android.util.Log
import android.widget.Toast

class SensorIntentService : IntentService(SensorIntentService::class.simpleName) {

    override fun onHandleIntent(intent: Intent?) {
        Log.d("blabla", "Intent service")
        Toast.makeText(this, "text", Toast.LENGTH_SHORT).show()
    }
}