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

        val request = OneTimeWorkRequestBuilder<PredictionWorker>()
            // Set Work to start on device charging
            .setConstraints(
                Constraints.Builder()
//                    .setRequiresCharging(true)
//                    .setMinimumLoggingLevel(android.util.Log.DEBUG)
                    .setRequiredNetworkType(NetworkType.METERED)
                    .build()
            )
            .addTag("WIFIJOB2")
            .build()

//        WorkManager.getInstance().cancelAllWorkByTag("com.gabchmel.contextmusicplayer.PredictionWorker")

        // Create on-demand initialization of WorkManager
        val workManager= WorkManager
            .getInstance(this@MainActivity)
//        workManager.enqueueUniqueWork(
//            "work",
//            ExistingWorkPolicy.REPLACE,
//            request
//        )

        val status = workManager.getWorkInfoByIdLiveData(request.id)
        status.observe(this, { workInfo->
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

//        WorkManager
//            .getInstance(this@MainActivity)
//            .getWorkInfoByIdLiveData(request.id)
//            .observe(this, { workInfo->
//                if (workInfo != null) {
//                    val progress = workInfo.progress
//                    val state = workInfo.state
//                    val value = progress.getInt(Progress, 6)
//                    Log.d("Progress", "Progress:$value, state:$state")
//                }
//            })
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