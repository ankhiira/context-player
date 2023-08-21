package com.gabchmel.contextmusicplayer.ui

import android.Manifest
import android.content.Context
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
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.lifecycle.lifecycleScope
import androidx.work.Constraints
import androidx.work.Data
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import com.gabchmel.common.data.GlobalPreferences
import com.gabchmel.common.data.dataStore.DataStore.dataStore
import com.gabchmel.common.utils.bindService
import com.gabchmel.contextmusicplayer.databinding.ActivityMainBinding
import com.gabchmel.contextmusicplayer.ui.utils.PredictionWorker
import com.gabchmel.sensorprocessor.data.service.SensorProcessService
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch


class MainActivity : AppCompatActivity() {

    // View binding for activity_main.xml file
    private lateinit var binding: ActivityMainBinding

    private object PreferencesKeys {
        val LOCATION_GRANTED = booleanPreferencesKey("location_permitted")
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Bind the layout with object
        binding = ActivityMainBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)

        // Adjust music volume with volume controls - volume controls adjust right stream
        volumeControlStream = AudioManager.STREAM_MUSIC

        // Start SensorProcessService to collect sensor values
        Intent(this, SensorProcessService::class.java).also { intent ->
            startService(intent)
        }

        // Save the current sensor values to shared preferences
        lifecycleScope.launch {
            val service = this@MainActivity.bindService(SensorProcessService::class.java)
            service.saveSensorValuesToSharedPrefs()
        }

        enqueueNewWork(this@MainActivity)
    }

    override fun onResume() {
        super.onResume()

        try {
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
                                    this@MainActivity.bindService(SensorProcessService::class.java)
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