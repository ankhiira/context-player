package com.gabchmel.contextmusicplayer

import android.content.Intent
import android.media.AudioManager
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import androidx.work.*
import com.gabchmel.sensorprocessor.SensorProcessService
import java.util.concurrent.TimeUnit


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

        val uploadWorkRequest: WorkRequest =
            PeriodicWorkRequestBuilder<PredictionWorker>(
                20, TimeUnit.SECONDS, // repeatInterval (the period cycle)
                10, TimeUnit.SECONDS // flexInterval
            )
                // Set Work to start on device charging
                .setConstraints(Constraints.Builder()
//                    .setRequiresCharging(true)
                    .setRequiredNetworkType(NetworkType.METERED)
                    .build())
                .build()

        // Create on-demand initialization of WorkManager
        WorkManager
            .getInstance(this@MainActivity)
            .enqueue(uploadWorkRequest)
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