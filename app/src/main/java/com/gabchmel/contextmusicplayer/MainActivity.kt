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
import com.gabchmel.contextmusicplayer.PredictionWorker.Companion.Progress
import com.gabchmel.sensorprocessor.SensorProcessService
import kotlinx.coroutines.launch
import java.util.*


class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

//        setContent {
//            Greeting("Android")
//        }

        setContentView(R.layout.activity_main)

        // Adjust music volume with volume controls
        volumeControlStream = AudioManager.STREAM_MUSIC

        Intent(this, SensorProcessService::class.java).also { intent ->
            startService(intent)
        }

        lifecycleScope.launch {
            val service = this@MainActivity.bindService(SensorProcessService::class.java)
            service.saveSensorData()
        }

        val request = OneTimeWorkRequestBuilder<PredictionWorker>()
            // Set Work to start on device charging
            .setConstraints(
                Constraints.Builder()
//                    .setRequiresCharging(true)
//                    .setMinimumLoggingLevel(android.util.Log.DEBUG)
//                    .setRequiredNetworkType(NetworkType.METERED)
                    .build()
            )
            .setInputData(
                Data.Builder()
//                    .putString("lifecycle", lifecycle.toString())
                    .build()
            )
            .addTag("WIFIJOB2")
            .build()

//        WorkManager.getInstance().cancelAllWorkByTag("com.gabchmel.contextmusicplayer.PredictionWorker")

        // Create on-demand initialization of WorkManager
        val workManager = WorkManager
            .getInstance(this@MainActivity)
//        workManager.enqueueUniqueWork(
//            "work",
//            ExistingWorkPolicy.REPLACE,
//            request
//        )

        val status = workManager.getWorkInfoByIdLiveData(request.id)
        status.observe(this, { workInfo ->
            if (workInfo != null) {
                val progress = workInfo.progress
                val state = workInfo.state
                val value = progress.getInt(Progress, -1)
                Log.d("Progress", "Progress:$value, state:$state")
            }
        })
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

    override fun onBackPressed() {
        val a = Intent(Intent.ACTION_MAIN)
        a.addCategory(Intent.CATEGORY_HOME)
        a.flags = Intent.FLAG_ACTIVITY_NEW_TASK
        startActivity(a)
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