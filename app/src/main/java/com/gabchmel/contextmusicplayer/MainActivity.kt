package com.gabchmel.contextmusicplayer

import android.content.Intent
import android.media.AudioManager
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import androidx.work.*
import com.gabchmel.contextmusicplayer.PredictionWorker.Companion.Progress
import com.gabchmel.sensorprocessor.SensorProcessService
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

        val uploadWorkRequest=
            OneTimeWorkRequestBuilder<PredictionWorker>()
//                .setInitialDelay(10, TimeUnit.MINUTES)
//            PeriodicWorkRequestBuilder<PredictionWorker>(
//                20, TimeUnit.SECONDS, // repeatInterval (the period cycle)
//                10, TimeUnit.SECONDS // flexInterval
//            )
                // Set Work to start on device charging
                .setConstraints(Constraints.Builder()
//                    .setRequiresCharging(true)
//                    .setMinimumLoggingLevel(android.util.Log.DEBUG)
                    .setRequiredNetworkType(NetworkType.METERED)
                    .build())
                .addTag("WIFIJOB2")
                .build()

        WorkManager.getInstance().cancelAllWorkByTag("com.gabchmel.contextmusicplayer.PredictionWorker")

        // Create on-demand initialization of WorkManager
        WorkManager
            .getInstance(this@MainActivity)
            .getWorkInfoByIdLiveData(UUID.randomUUID())
            .observe(this, { workInfo: WorkInfo? ->
                if (workInfo != null) {
                    val progress = workInfo.progress
                    val value = progress.getInt(Progress, 0)
                    Log.d("WorkManager", "Progress:$value")
                }
            })
//            .enqueue(uploadWorkRequest)
//            .enqueueUniquePeriodicWork(
//                "predictSong",
//                ExistingPeriodicWorkPolicy.REPLACE,
//                uploadWorkRequest)
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