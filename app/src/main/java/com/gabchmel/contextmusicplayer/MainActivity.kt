package com.gabchmel.contextmusicplayer

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.media.AudioManager
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import androidx.core.app.ActivityCompat
import androidx.lifecycle.lifecycleScope
import androidx.work.*
import com.gabchmel.common.utilities.bindService
import com.gabchmel.sensorprocessor.SensorProcessService
import kotlinx.coroutines.launch
import java.util.*


class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_main)

        // Adjust music volume with volume controls
        volumeControlStream = AudioManager.STREAM_MUSIC

        // Start SensorProcessService to collect sensor values
        Intent(this, SensorProcessService::class.java).also { intent ->
            startService(intent)
        }

        // Save the current sensor values to shared preferences
        lifecycleScope.launch {
            val service = this@MainActivity.bindService(SensorProcessService::class.java)
            service.saveSensorData()
        }

        // Create Work
        val request = OneTimeWorkRequestBuilder<PredictionWorker>()
            // Set Work to start on device charging
            .setConstraints(
                Constraints.Builder()
                    .build()
            )
            .setInputData(
                Data.Builder()
                    .build()
            )
            .addTag("WIFIJOB2")
            .build()

        // Create on-demand initialization of WorkManager
        val workManager = WorkManager
            .getInstance(this@MainActivity)

        // To monitor the Work state
        val status = workManager.getWorkInfoByIdLiveData(request.id)
        status.observe(this, { workInfo ->
            if (workInfo != null) {
                Log.d("Progress", "State: state:${workInfo.state}")
            }
        })

        // Enqueue the first Work
        workManager.enqueueUniqueWork(
            "work",
            ExistingWorkPolicy.REPLACE,
            request
        )
    }

    override fun onResume() {
        super.onResume()

        // When the activity is opened again, check if the permission didn't change
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED || ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED || ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACTIVITY_RECOGNITION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            // If the permission is not enabled, save that option to shared preferences file
            val editor = getSharedPreferences("MyPrefsFile", MODE_PRIVATE).edit()
            editor.putBoolean("locationPermission", false)
            editor.apply()
        } else {
            // If the permission is now enabled, register location listener
            val prefs = getSharedPreferences("MyPrefsFile", MODE_PRIVATE)
            val wasGranted = prefs.getBoolean("locationPermission", true)

            // Save the current permission state
            val editor = getSharedPreferences("MyPrefsFile", MODE_PRIVATE).edit()
            editor.putBoolean("locationPermission", true)
            editor.apply()

            if (!wasGranted) {
                lifecycleScope.launch {
                    val service = this@MainActivity.bindService(SensorProcessService::class.java)
                    service.registerLocationListener()
                }
            }
        }
    }
}

@Composable
fun Greeting(name: String) {
    Text(text = "Hello $name!")
}

@Preview
@Composable
fun PreviewGreeting() {
    Greeting("Android dlhoo")
}