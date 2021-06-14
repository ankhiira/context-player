package com.gabchmel.contextmusicplayer

import android.content.Intent
import android.media.AudioManager
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import com.gabchmel.sensorprocessor.SensorProcessService


class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

//        setContent {
//            Greeting("Android")
//        }

        setContentView(R.layout.activity_main)

        // Adjust music volume with volume controls
        volumeControlStream = AudioManager.STREAM_MUSIC

        // Bottom navigation
//        val navHostFragment =
//            supportFragmentManager.findFragmentById(R.id.nav_host_fragment) as NavHostFragment
//        val navController = navHostFragment.navController

//        val bottomNavigationView: BottomNavigationView = findViewById(R.id.bottom_nav_view)
//
//        NavigationUI.setupWithNavController(bottomNavigationView, navController)

        Intent(this, SensorProcessService::class.java).also { intent ->
            startService(intent)
        }

        Intent(this@MainActivity, AutoPlaySongService::class.java).also { intent ->
            startService(intent)
        }

        val input = floatArrayOf(0.781831f,0.265953f,0.410050f,-0.872427f,-0.522189f,0.62349f,0.85283f)

//        val predictionModel = PredictionModelBuiltIn(this)
//        val songToPlay = predictionModel.predict(input)

//        // Recreate notification
//        val notification: Notification = NotificationManager.createNotification(
//            baseContext,
//            mediaSession.sessionToken,
//            metadataRetriever.getTitle() ?: "unknown",
//            metadataRetriever.getArtist() ?: "unknown",
//            metadataRetriever.getAlbumArt() ?: BitmapFactory.decodeResource(
//                resources,
//                R.raw.album_cover_clipart
//            ),
//            isPlaying
//        )

//        NotificationManager.displayNotification(baseContext, notification)
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