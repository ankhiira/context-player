package com.gabchmel.sensorprocessor

import android.Manifest
import android.app.PendingIntent
import android.app.Service
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothProfile
import android.content.*
import android.content.pm.PackageManager
import android.hardware.Sensor
import android.hardware.SensorManager
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.net.wifi.WifiManager
import android.os.Build
import android.os.Bundle
import android.os.IBinder
import android.util.Log
import android.widget.Toast
import androidx.core.app.ActivityCompat
import com.gabchmel.predicitonmodule.PredictionModelBuiltIn
import com.gabchmel.sensorprocessor.InputProcessHelper.inputProcessHelper
import com.gabchmel.sensorprocessor.InputProcessHelper.processInputCSV
import com.gabchmel.sensorprocessor.activityDetection.TransitionList
import com.google.android.gms.location.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import java.io.File
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList
import kotlin.math.abs


class SensorProcessService : Service() {

    private var locationManager: LocationManager? = null

    private val _location = MutableStateFlow<Location?>(null)
    val location: StateFlow<Location?> = _location

    private val _time = MutableStateFlow<Date?>(null)
    val time: StateFlow<Date?> = _time

    private lateinit var csvFile: File

    private lateinit var pendingIntent: PendingIntent
    private lateinit var broadcastReceiver: BroadcastReceiver

    private var currentState = "NONE"

    private var lightSensorValue: Float = 0.0f

    private var orientSensorAzimuthZAxis: Float = 0.0f
    private var orientSensorPitchXAxis: Float = 0.0f
    private var orientSensorRollYAxis: Float = 0.0f

    private var barometerVal: Float = 0.0f

    private var deviceLying = 0.0f

    private var BTdeviceConnected = 0.0f

    var headphonesPluggedIn = 0

    private val predictionModel = PredictionModelBuiltIn(this)

    private var classNames = arrayListOf<String>()

    private val _prediction = MutableStateFlow<String?>(null)
    val prediction: StateFlow<String?> = _prediction

    // binder given to clients
    private val binder = object : LocalBinder<SensorProcessService>() {
        override fun getService() = this@SensorProcessService
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
            Log.e("perm", "permission not granted")
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

        // Check if the BT device is connected
        bluetoothDevicesConnection()

//        val receiverFilter = IntentFilter(Intent.ACTION_BATTERY_CHANGED)
//        registerReceiver(SensorReceiver(), receiverFilter)
    }

    override fun onBind(intent: Intent): IBinder {

        val sensorManager = this.getSystemService(Context.SENSOR_SERVICE) as SensorManager

        // Register listeners to sensor value changes
        val coorList = SensorManagerUtility.sensorReader(
            sensorManager, Sensor.TYPE_ORIENTATION,
            "Orientation"
        ) as MutableList<*>

//        orientSensorAzimuthZAxis = coorList[0] as Float
//        orientSensorPitchXAxis = coorList[1] as Float
//        orientSensorRollYAxis = coorList[2] as Float

        lightSensorValue = SensorManagerUtility.sensorReader(
            sensorManager, Sensor.TYPE_LIGHT,
            "Ambient light"
        ) as Float

        barometerVal = SensorManagerUtility.sensorReader(
            sensorManager, Sensor.TYPE_PRESSURE,
            "Barometer"
        ) as Float

        SensorManagerUtility.sensorReader(
            sensorManager, Sensor.TYPE_AMBIENT_TEMPERATURE,
            "Temperature"
        )

//        SensorManagerUtility.sensorReader(
//            sensorManager, Sensor.TYPE_PROXIMITY,
//            "Proximity"
//        )
//
//        SensorManagerUtility.sensorReader(
//            sensorManager, Sensor.TYPE_RELATIVE_HUMIDITY,
//            "Humidity"
//        )
//
//        SensorManagerUtility.sensorReader(
//            sensorManager, Sensor.TYPE_HEART_BEAT,
//            "Heart beat"
//        )
//
//        SensorManagerUtility.sensorReader(
//            sensorManager, Sensor.TYPE_HEART_RATE,
//            "Heart rate"
//        )
//
//        SensorManagerUtility.sensorReader(
//            sensorManager, Sensor.TYPE_HEART_RATE,
//            "Heart rate"
//        )

//        headphonesPluggedInDetection()
        wifiConnection()
        internetConnectivity(this)

        createModel()
        triggerPrediction()

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

    fun writeToFile(ID: String) {

        // Get current time
        val currentTime = Calendar.getInstance().time
        val dateFormatter = SimpleDateFormat("E MMM dd HH:mm:ss ZZZZ yyyy", Locale.getDefault())
        val date = dateFormatter.format(currentTime)

        Log.d("Sensor", "write")

        processOrientation()

        val latitude: Double
        val longitude: Double

//        if (location.value == null) {
//            locationManager.requestLocationUpdates(getProviderName(), 0, 0, this)
//        }

        // If the location is null, set the value to 0.0
        if (location.value == null) {
            latitude = 0.0
            longitude = 0.0
        } else {
            latitude = location.value?.latitude!!
            longitude = location.value?.longitude!!
        }

        // TODO Make check that we have a value - maybe we don't have to have value idk
        try {
            // Write to csv file
            csvFile.appendText(
                ID + ","
                        + date + ","
                        + longitude.toString() + ","
                        + latitude.toString() + ","
                        + currentState + ","
                        + lightSensorValue + ","
                        + deviceLying + ","
                        + BTdeviceConnected + ","
                        + headphonesPluggedIn.toFloat() + "\n"
            )
        } catch (e: IOException) {
            Log.e("Err", "Couldn't write to file", e)
        }
    }

    fun getSensorData(): SensorData {

        val currentTime = Calendar.getInstance().time

        return SensorData(
            currentTime,
            location.value?.longitude,
            location.value?.latitude,
            currentState,
            lightSensorValue,
            deviceLying,
            BTdeviceConnected,
            headphonesPluggedIn.toFloat()
        )
    }

    fun createModel() {

        // Process input CSV file and save class names into ArrayList<String>
        classNames = processInputCSV(this)

        predictionModel.createModel(classNames)
    }

    fun triggerPrediction() {

        Log.d("prediciton", "trigger prediction")

        // Get the processed input values
        val input = inputProcessHelper(getSensorData())

        _prediction.value = predictionModel.predict(input, classNames)
    }

    private fun activityDetection() {

        val request = ActivityTransitionRequest(TransitionList.getTransitions())

        val intent = Intent(this, ActivityTransitionReceiver::class.java)
        pendingIntent = PendingIntent.getBroadcast(this, 0, intent, 0)

        // myPendingIntent is the instance of PendingIntent where the app receives callbacks.
        val task = ActivityRecognition.getClient(this)
            .requestActivityTransitionUpdates(request, pendingIntent)

        // used: https://heartbeat.fritz.ai/detect-users-activity-in-android-using-activity-transition-api-f718c844efb2
        task.addOnSuccessListener {
            // Handle success
            Log.d("ActivityRecognition", "Transitions Api registered with success")
        }

        task.addOnFailureListener { e: Exception ->
            // Handle error
            Log.d(
                "ActivityRecognition",
                "Transitions Api could NOT be registered ${e.localizedMessage}"
            )
        }
    }

    fun writeActivity(currentActivity: String) {
        currentState = currentActivity
    }

    private fun processOrientation() {
        // Get the range of acceptable values for lying device - -1.58 is exactly lying
        deviceLying = if (abs(orientSensorAzimuthZAxis) in 1.4f..1.7f) {
            Log.d("Orientation", "Device is lying")
            1.0f
        } else {
            Log.d("Orientation", "Device is staying")
            0.0f
        }
    }

    private fun bluetoothDevicesConnection() {

        // Check if the device supports bluetooth
        val pm: PackageManager = this.packageManager
        val hasBluetooth = pm.hasSystemFeature(PackageManager.FEATURE_BLUETOOTH)

        if (hasBluetooth) {

            val bluetoothAdapter = BluetoothAdapter.getDefaultAdapter()
            val pairedDevices = bluetoothAdapter.bondedDevices

            val s: MutableList<String> = ArrayList()
            for (bt in pairedDevices) s.add(bt.name)

            BTdeviceConnected =
                if (bluetoothAdapter != null && BluetoothProfile.STATE_CONNECTED == bluetoothAdapter.getProfileConnectionState(
                        BluetoothProfile.HEADSET
                    )
                ) {
                    Log.d("BT", "mame headset")
                    1.0f
                } else {
                    0.0f
                }
        }
    }

    private fun headphonesPluggedInDetection() {
        broadcastReceiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context?, intent: Intent) {
                val action = intent.action
                if (Intent.ACTION_HEADSET_PLUG == action) {
                    headphonesPluggedIn = intent.getIntExtra("state", -1)
                    if (headphonesPluggedIn == 0) {
                        Toast.makeText(
                            applicationContext,
                            "Headphones not plugged in",
                            Toast.LENGTH_LONG
                        ).show()
                    } else if (headphonesPluggedIn == 1) {
                        Toast.makeText(
                            applicationContext,
                            "Headphones plugged in",
                            Toast.LENGTH_LONG
                        ).show()
                    }
                }
            }
        }

//        val receiverFilter = IntentFilter(Intent.ACTION_HEADSET_PLUG)
//        registerReceiver(broadcastReceiver, receiverFilter)
    }

    private fun wifiConnection(): String {
        val wifiManager = applicationContext.getSystemService(WIFI_SERVICE) as WifiManager
        val wifiInfo = wifiManager.connectionInfo

        Log.d("ssid", "SSID:${wifiInfo.ssid}, hashCode:${wifiInfo.ssid.hashCode()}")

        return wifiInfo.ssid.hashCode().toString()
    }

    private fun internetConnectivity(context: Context): String {
        val connectivityManager =
            context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val capabilities: NetworkCapabilities?
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            capabilities =
                connectivityManager.getNetworkCapabilities(connectivityManager.activeNetwork)
        } else {
            return "NONE"
        }
        if (capabilities != null) {
            when {
                capabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) -> {
                    Log.i("Internet", "NetworkCapabilities.TRANSPORT_CELLULAR")
                    return "TRANSPORT_CELLULAR"
                }
                capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) -> {
                    Log.i("Internet", "NetworkCapabilities.TRANSPORT_WIFI")
                    return "TRANSPORT_WIFI"
                }
                capabilities.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET) -> {
                    Log.i("Internet", "NetworkCapabilities.TRANSPORT_ETHERNET")
                    return "TRANSPORT_ETHERNET"
                }
            }
        }
        return "NONE"
    }
}