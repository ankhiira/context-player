package com.gabchmel.sensorprocessor

import android.Manifest
import android.app.PendingIntent
import android.app.Service
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Binder
import android.os.Bundle
import android.os.IBinder
import android.util.Log
import androidx.core.app.ActivityCompat
import com.google.android.gms.location.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import java.io.File
import java.io.IOException
import java.util.*
import kotlin.math.abs


class SensorProcessService : Service() {

    private var locationManager: LocationManager? = null

    private val _location = MutableStateFlow<Location?>(null)
    val location: StateFlow<Location?> = _location

    private val _time = MutableStateFlow<Date?>(null)
    val time: StateFlow<Date?> = _time

    private lateinit var csvFile : File

    private lateinit var pendingIntent: PendingIntent
    private lateinit var broadcastReceiver: BroadcastReceiver

    private var currentState = "NONE"

    private var lightSensorValue : Float = 0.0f

    private var orientSensorAzimuth_z_axis : Float = 0.0f
    private var orientSensorPitch_x_axis : Float = 0.0f
    private var orientSensorRoll_y_axis : Float = 0.0f

    // binder given to clients
    private val binder = LocalBinder()

    // class used for the client binder
    inner class LocalBinder : Binder() {
        // Returns instance of SensorProcessService so clients can call public methods
        fun getService()= this@SensorProcessService
    }

    // Sensor event listener for light sensor
    private var sensorEventListenerLight = object: SensorEventListener {
        override fun onSensorChanged(event: SensorEvent?) {
            if (event != null) {
                // Get the value in Lux
                lightSensorValue = event.values[0]
                Log.d("LightVal", "light:$lightSensorValue")
            }
        }

        override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
        }
    }

    private var sensorEventListenerOrientation = object : SensorEventListener {
        override fun onSensorChanged(event: SensorEvent?) {
            if (event != null) {
                orientSensorAzimuth_z_axis = event.values[0]
                orientSensorPitch_x_axis = event.values[1]
                orientSensorRoll_y_axis = event.values[2]

                Log.d("Orientation", "orientation:$orientSensorAzimuth_z_axis," +
                        "$orientSensorPitch_x_axis, $orientSensorRoll_y_axis")
            }
        }

        override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
        }

    }

    override fun onCreate() {
        super.onCreate()

        // CSV file with sensor measurements and context data
        csvFile = File(this.filesDir, "data.csv")

        // Persistent LocationManager reference
        locationManager = this.getSystemService(LOCATION_SERVICE) as LocationManager?

        // TODO if not granted
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACTIVITY_RECOGNITION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            Log.e("perm","permission not granted")
        }

        locationManager?.requestLocationUpdates(
            LocationManager.NETWORK_PROVIDER,
            0L,
            0f,
            locationListener
        )

        // Get current time
        _time.value = Calendar.getInstance().time

        // Set ut callbacks for activity detection
        activityDetection()
    }

    override fun onBind(intent: Intent): IBinder {

        val sensorManager= this.getSystemService(Context.SENSOR_SERVICE) as SensorManager
        val sensorLight = sensorManager.getDefaultSensor(Sensor.TYPE_LIGHT)

        sensorManager.registerListener(sensorEventListenerLight, sensorLight,
            SensorManager.SENSOR_DELAY_NORMAL)

        val mySensors: List<Sensor> = sensorManager.getSensorList(Sensor.TYPE_ORIENTATION)

        sensorManager.registerListener(sensorEventListenerOrientation, mySensors[0],
        SensorManager.SENSOR_DELAY_NORMAL)

        return binder
    }

    // Location change listener
    private val locationListener: LocationListener = object : LocationListener {
        override fun onLocationChanged(location: Location) {
            _location.value = location
        }

        override fun onStatusChanged(provider: String, status: Int, extras: Bundle) {}
        override fun onProviderEnabled(provider: String) {}
        override fun onProviderDisabled(provider: String) {}
    }

    fun writeToFile(ID : String) {

        // Get current time
        val currentTime = Calendar.getInstance().time
        Log.d("Orientation", "write")

        processOrientation()

        // TODO Make check that we have a value - maybe we don't have to have value idk
        try {
            // Write to csv file
            csvFile.appendText(
                ID + "," +
                currentTime.toString() + ","
                + location.value?.longitude.toString() + ","
                + location.value?.latitude.toString() + "\n"
            )
        } catch (e: IOException) {
            Log.e("Err", "Couldn't write to file", e)
        }
    }

    private fun activityDetection() {
        //        broadcastReceiver = object : BroadcastReceiver() {
//            override fun onReceive(context: Context, intent: Intent) {
//                if (ActivityTransitionResult.hasResult(intent)) {
//                    val result = ActivityTransitionResult.extractResult(intent)!!
//                    for (event in result.transitionEvents) {
//                        // chronological sequence of events....
//                        Log.d("Action", "logging")
//                    }
//                }
//            }
//        }

        val transitions = mutableListOf<ActivityTransition>()

        transitions +=
            ActivityTransition.Builder()
                .setActivityType(DetectedActivity.IN_VEHICLE)
                .setActivityTransition(ActivityTransition.ACTIVITY_TRANSITION_ENTER)
                .build()

        transitions +=
            ActivityTransition.Builder()
                .setActivityType(DetectedActivity.IN_VEHICLE)
                .setActivityTransition(ActivityTransition.ACTIVITY_TRANSITION_EXIT)
                .build()

        transitions +=
            ActivityTransition.Builder()
                .setActivityType(DetectedActivity.ON_BICYCLE)
                .setActivityTransition(ActivityTransition.ACTIVITY_TRANSITION_ENTER)
                .build()

        transitions +=
            ActivityTransition.Builder()
                .setActivityType(DetectedActivity.ON_FOOT)
                .setActivityTransition(ActivityTransition.ACTIVITY_TRANSITION_ENTER)
                .build()

        transitions +=
            ActivityTransition.Builder()
                .setActivityType(DetectedActivity.STILL)
                .setActivityTransition(ActivityTransition.ACTIVITY_TRANSITION_ENTER)
                .build()

        transitions +=
            ActivityTransition.Builder()
                .setActivityType(DetectedActivity.WALKING)
                .setActivityTransition(ActivityTransition.ACTIVITY_TRANSITION_ENTER)
                .build()

        transitions +=
            ActivityTransition.Builder()
                .setActivityType(DetectedActivity.WALKING)
                .setActivityTransition(ActivityTransition.ACTIVITY_TRANSITION_EXIT)
                .build()

        val request = ActivityTransitionRequest(transitions)

        val intent = Intent(this, ActivityTransitionReceiver::class.java)
        pendingIntent = PendingIntent.getBroadcast(this, 0, intent, PendingIntent.FLAG_IMMUTABLE)

        // myPendingIntent is the instance of PendingIntent where the app receives callbacks.
        val task= ActivityRecognition.getClient(this)
            .requestActivityTransitionUpdates(request, pendingIntent)

        // used: https://heartbeat.fritz.ai/detect-users-activity-in-android-using-activity-transition-api-f718c844efb2
        task.addOnSuccessListener {
            // Handle success
            Log.d("ActivityRecognition", "Transitions Api registered with success")
        }

        task.addOnFailureListener { e: Exception ->
            // Handle error
            Log.d("ActivityRecognition", "Transitions Api could NOT be registered ${e.localizedMessage}")
        }
    }

    fun writeActivity(currentActivity : String) {
        currentState = currentActivity
    }

    private fun processOrientation() {
        // Get the range of acceptable values for lying device - -1.58 is exactly lying
        if (abs(orientSensorAzimuth_z_axis) in 1.4f..1.7f) {
            Log.d("Orientation", "Device is lying")
        } else {
            Log.d("Orientation", "Device is staying")
        }
    }
}