package com.gabchmel.contextmusicplayer.ui

import android.Manifest
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.media.AudioManager
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.lifecycle.lifecycleScope
import androidx.work.Constraints
import androidx.work.Data
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import com.gabchmel.common.data.dataStore.DataStore
import com.gabchmel.common.data.dataStore.DataStore.dataStore
import com.gabchmel.common.data.dataStore.GlobalPreferences
import com.gabchmel.common.utils.bindService
import com.gabchmel.contextmusicplayer.utils.isPermissionNotGranted
import com.gabchmel.contextmusicplayer.worker.PredictionWorker
import com.gabchmel.sensorprocessor.data.service.SensorDataProcessingService
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch


class MainActivity : ComponentActivity() {

    private object PreferencesKeys {
        val LOCATION_GRANTED = booleanPreferencesKey("location_permitted")
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.auto(
                darkScrim = Color.TRANSPARENT,
                lightScrim = Color.TRANSPARENT
            )
        )
        super.onCreate(savedInstanceState)

        setContent {
            ContextPlayerApp()
        }

        // Start SensorProcessService to collect sensor values
        Intent(this, SensorDataProcessingService::class.java).also { intent ->
            startService(intent)
        }

        lifecycleScope.launch {
            this@MainActivity.bindService(SensorDataProcessingService::class.java)
        }

        enqueueNewWork(this@MainActivity)
    }

    override fun onResume() {
        super.onResume()

        // To adjust the right music stream volume (e.g. not ring volume)
        volumeControlStream = AudioManager.STREAM_MUSIC

        try {
            // When the activity is opened again, check if the permissions state didn't change
            if (isPermissionNotGranted(this, Manifest.permission.ACCESS_FINE_LOCATION)
                || isPermissionNotGranted(this, Manifest.permission.ACCESS_COARSE_LOCATION)
                || isPermissionNotGranted(this, Manifest.permission.ACTIVITY_RECOGNITION)
            ) {
                CoroutineScope(Dispatchers.IO).launch {
                    dataStore.edit { preferences ->
                        preferences[PreferencesKeys.LOCATION_GRANTED] = false
                    }
                }
            } else {
                // If the permission is granted, register location listener
                val savedGlobalPreferences = dataStore.data
                    .map { preferences ->
                        val isLocationGranted =
                            preferences[PreferencesKeys.LOCATION_GRANTED] ?: false
                        GlobalPreferences(isLocationGranted)
                    }

                CoroutineScope(Dispatchers.IO).launch {
                    savedGlobalPreferences.collect { preferences ->
                        if (!preferences.isLocationGranted) {
                            lifecycleScope.launch {
                                val service =
                                    this@MainActivity.bindService(SensorDataProcessingService::class.java)
                                service.registerLocationListener()
                            }

                            dataStore.edit { preferencesOther ->
                                preferencesOther[PreferencesKeys.LOCATION_GRANTED] = true
                            }
                        }
                    }
                }
            }
        } catch (e: Exception) {
            Log.e("Error", e.toString())
        }
    }

    private fun enqueueNewWork(context: Context) {
        val workRequest = OneTimeWorkRequestBuilder<PredictionWorker>()
            // Set Work to start on device charging
            .setConstraints(
                Constraints.Builder()
                    .build()
            )
            .setInputData(
                Data.Builder()
                    .build()
            )
            .addTag("WIFI_JOB2")
            .build()

        // Create on-demand initialization of WorkManager
        val workManager = WorkManager.getInstance(context)

        // To monitor the Work state
        val status = workManager.getWorkInfoByIdLiveData(workRequest.id)
        status.observe(this) { workInfo ->
            workInfo?.let {
                Log.d("Progress", "State: state:${workInfo.state}")
            }
        }

        // Enqueue the first Work
        workManager.enqueueUniqueWork(
            "work",
            ExistingWorkPolicy.REPLACE,
            workRequest
        )
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