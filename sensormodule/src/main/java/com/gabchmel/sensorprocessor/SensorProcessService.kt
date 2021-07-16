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
import android.location.LocationListener
import android.location.LocationManager
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.net.wifi.WifiManager
import android.os.BatteryManager
import android.os.Build
import android.os.IBinder
import android.util.Log
import android.widget.Toast
import androidx.core.app.ActivityCompat
import com.gabchmel.common.LocalBinder
import com.gabchmel.predicitonmodule.PredictionModelBuiltIn
import com.gabchmel.sensorprocessor.activityDetection.TransitionList
import com.gabchmel.sensorprocessor.utility.InputProcessHelper.inputProcessHelper
import com.gabchmel.sensorprocessor.utility.InputProcessHelper.processInputCSV
import com.gabchmel.sensorprocessor.utility.SensorManagerUtility
import com.google.android.gms.location.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import java.io.File
import java.io.IOException
import java.util.*
import kotlin.math.acos
import kotlin.math.roundToInt
import kotlin.math.sqrt


class SensorProcessService : Service() {

    // Structure to store sensor values
    var _sensorData = MutableStateFlow(SensorData())
    val sensorData: StateFlow<SensorData> = _sensorData

    val data = sensorData.value

    // List of orientation coordinates
    var coordList = mutableListOf<Float>()

    // Input CSV file to save sensor values
    private lateinit var csvFile: File

    // Random Forest model
    private val predictionModel = PredictionModelBuiltIn(this)

    // Song IDs
    private var classNames = arrayListOf<String>()

    // Saved prediction result as StateFlow to show notification
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

        registerLocationListener()

        // Set ut callbacks for activity detection
        activityDetection()
    }

    override fun onBind(intent: Intent): IBinder {

        val sensorManager = this.getSystemService(Context.SENSOR_SERVICE) as SensorManager

        // Register listeners to sensor value changes
        SensorManagerUtility.sensorReader(
            sensorManager, Sensor.TYPE_ACCELEROMETER,
            "Orientation",
            this
        )

        SensorManagerUtility.sensorReader(
            sensorManager, Sensor.TYPE_LIGHT,
            "Ambient light",
            this
        )

        SensorManagerUtility.sensorReader(
            sensorManager, Sensor.TYPE_PRESSURE,
            "Barometer",
            this
        )

        SensorManagerUtility.sensorReader(
            sensorManager, Sensor.TYPE_AMBIENT_TEMPERATURE,
            "Temperature",
            this
        )

        SensorManagerUtility.sensorReader(
            sensorManager, Sensor.TYPE_PROXIMITY,
            "Proximity",
            this
        )

        SensorManagerUtility.sensorReader(
            sensorManager, Sensor.TYPE_RELATIVE_HUMIDITY,
            "Humidity",
            this
        )

        SensorManagerUtility.sensorReader(
            sensorManager, Sensor.TYPE_HEART_BEAT,
            "Heart beat",
            this
        )

        SensorManagerUtility.sensorReader(
            sensorManager, Sensor.TYPE_HEART_RATE,
            "Heart rate",
            this
        )

        headphonesPluggedInDetection()

        return binder
    }

    fun writeToFile(songID: String) {
        // Read current time
        _sensorData.value.currentTime = Calendar.getInstance().time

//        Log.d("Sensor", "write")

        // Check to which Wi-Fi is the device connected
        wifiConnection()
        // Check the internet connection type
        internetConnectivity(this)
        // Get the orientation of the device
        processOrientation()
        // Detect if the device is charging
        batteryStatusDetection()
        // Check if the BT device is connected
        bluetoothDevicesConnection()

        try {
            // TODO redo do for each to optimize
            // Write to csv file
            csvFile.appendText(
                songID + ","
                        + sensorData.value.currentTime + ","
                        + sensorData.value.longitude + ","
                        + sensorData.value.latitude + ","
                        + sensorData.value.currentState + ","
                        + sensorData.value.lightSensorValue + ","
                        + sensorData.value.deviceLying + ","
                        + sensorData.value.BTdeviceConnected + ","
                        + sensorData.value.headphonesPluggedIn + ","
                        + sensorData.value.pressure + ","
                        + sensorData.value.temperature + ","
                        + sensorData.value.wifi + ","
                        + sensorData.value.connection + ","
                        + sensorData.value.batteryStatus + ","
                        + sensorData.value.chargingType + ","
                        + sensorData.value.proximity + ","
                        + sensorData.value.humidity + ","
                        + sensorData.value.heartBeat + ","
                        + sensorData.value.heartRate + "\n"
            )
        } catch (e: IOException) {
            Log.e("Err", "Couldn't write to file", e)
        }
    }

    fun createModel(): Boolean {
        // Process input CSV file and save class names and wifi list into ArrayList<String>
        val (classNamesNew, wifiList) = processInputCSV(this)

        classNames = classNamesNew

        // If we don't have enough input data, don't create a model
        if (!predictionModel.createModel(classNames, wifiList)) {
            return false
        }

        Log.d("model", "model created")
        return true
    }

    fun triggerPrediction() {
        Log.d("prediction", "trigger prediction")

        // Get the processed input values
        val input = inputProcessHelper(sensorData.value)

        _prediction.value = predictionModel.predict(input, classNames)
    }

    fun detectContextChange(): Boolean {
        val prefs = getSharedPreferences("MyPrefsFile", MODE_PRIVATE)
        val time = prefs.getString("time", "No name defined")
        val headphones = prefs.getFloat("headphones", -1.0f)
        val bluetooth = prefs.getFloat("bluetooth", -1.0f)
        val light = prefs.getFloat("light", -1.0f)
        if (sensorData.value.BTdeviceConnected != bluetooth) {
            return true
        }
        if (sensorData.value.lightSensorValue != light) {
            return true
        }
        return false
    }

    // Function to save current context values to shared preferences for later comparison
    fun saveSensorData() {
        val editor = getSharedPreferences("MyPrefsFile", MODE_PRIVATE).edit()
        editor.putString("time", sensorData.value.currentTime.toString())
        sensorData.value.longitude?.let { editor.putFloat("longitude", it.toFloat()) }
        sensorData.value.latitude?.let { editor.putFloat("latitude", it.toFloat()) }
        editor.putString("state", sensorData.value.currentState)
        sensorData.value.lightSensorValue?.let { editor.putFloat("light", it) }
        sensorData.value.deviceLying?.let { editor.putFloat("lying", it) }
        sensorData.value.BTdeviceConnected?.let { editor.putFloat("bluetooth", it) }
        sensorData.value.headphonesPluggedIn?.let { editor.putFloat("headphones", it) }
        editor.apply()
    }

    private fun activityDetection() {
        val request = ActivityTransitionRequest(TransitionList.getTransitions())

        val intent = Intent(this, ActivityTransitionReceiver::class.java)
        val pendingIntent = PendingIntent.getBroadcast(this, 0, intent, 0)

        // pendingIntent is the instance of PendingIntent where the app receives callbacks.
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

    private fun processOrientation() {

        val normOfg = sqrt(
            (coordList[0] * coordList[0]
                    + coordList[1] * coordList[1] + coordList[2] * coordList[2]).toDouble()
        )

        // Normalize the accelerometer vector
        coordList[0] = (coordList[0] / normOfg).toFloat()
        coordList[1] = (coordList[1] / normOfg).toFloat()
        coordList[2] = (coordList[2] / normOfg).toFloat()

        val inclination = Math.toDegrees(acos(coordList[2]).toDouble()).roundToInt()

        // Device detected as lying is with inclination in range < 25 or > 155 degrees
        _sensorData.value.deviceLying = if (inclination < 25 || inclination > 155)
        {
            // device is flat
            Log.d("Orientation", "Device is lying")
            1.0f
        }
        else
        {
            // device is not flat
            Log.d("Orientation", "Device is staying")
            0.0f
        }
    }

    private fun bluetoothDevicesConnection() {

        // Check if the device supports bluetooth
        if (packageManager.hasSystemFeature(PackageManager.FEATURE_BLUETOOTH)) {
            val bluetoothAdapter = BluetoothAdapter.getDefaultAdapter()
//            val pairedDevices = bluetoothAdapter.bondedDevices

//            val s: MutableList<String> = ArrayList()
//            for (bt in pairedDevices) s.add(bt.name)

            // Check if the bluetooth headphones are connected
            _sensorData.value.BTdeviceConnected =
                if (bluetoothAdapter != null && BluetoothProfile.STATE_CONNECTED
                    == bluetoothAdapter.getProfileConnectionState(
                        BluetoothProfile.HEADSET
                    )
                ) {
//                    Log.d("BT", "mame headset")
                    1.0f
                } else {
                    0.0f
                }
        }
    }

    private fun headphonesPluggedInDetection() {
        val broadcastReceiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context?, intent: Intent) {
                val action = intent.action
                if (Intent.ACTION_HEADSET_PLUG == action) {
                    _sensorData.value.headphonesPluggedIn =
                        intent.getIntExtra("state", -1).toFloat()
                }
            }
        }

        val receiverFilter = IntentFilter(Intent.ACTION_HEADSET_PLUG)
        registerReceiver(broadcastReceiver, receiverFilter)
    }

    private fun wifiConnection() {
        val wifiManager = applicationContext.getSystemService(WIFI_SERVICE) as WifiManager
        val wifiInfo = wifiManager.connectionInfo

//        Log.d("ssid", "SSID:${wifiInfo.ssid}, hashCode:${wifiInfo.ssid.hashCode()}")

        _sensorData.value.wifi = wifiInfo.ssid.hashCode().toUInt()
    }

    // Function to retrieve current internet connection state
    private fun internetConnectivity(context: Context) {
        val connectivityManager =
            context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        var capabilities: NetworkCapabilities? = null
        // Function activeNetwork requires minimal level 23
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            capabilities =
                connectivityManager.getNetworkCapabilities(connectivityManager.activeNetwork)
        } else {
            _sensorData.value.connection = "NONE"
        }
        if (capabilities != null) {
            when {
                capabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) -> {
//                    Log.i("Internet", "NetworkCapabilities.TRANSPORT_CELLULAR")
                    _sensorData.value.connection = "TRANSPORT_CELLULAR"
                }
                capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) -> {
//                    Log.i("Internet", "NetworkCapabilities.TRANSPORT_WIFI")
                    _sensorData.value.connection = "TRANSPORT_WIFI"
                }
                capabilities.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET) -> {
//                    Log.i("Internet", "NetworkCapabilities.TRANSPORT_ETHERNET")
                    _sensorData.value.connection = "TRANSPORT_ETHERNET"
                }
            }
        } else {
            _sensorData.value.connection = "NONE"
        }
    }

    private fun registerLocationListener() {
        // Location change listener
        val locationListener = LocationListener { location ->
            _sensorData.value.longitude = location.longitude
            _sensorData.value.latitude = location.latitude

            Log.d("location", "${location.latitude}, ${location.longitude}")
        }

        // Persistent LocationManager reference
        val locationManager = this.getSystemService(LOCATION_SERVICE) as LocationManager?

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
            Log.e("permission", "permission not granted")
            return
        }

        locationManager?.requestLocationUpdates(
            LocationManager.NETWORK_PROVIDER,
            0L,
            0f,
            locationListener
        )

        //        if (location.value == null) {
//            locationManager.requestLocationUpdates(getProviderName(), 0, 0, this)
//        }
    }

    private fun batteryStatusDetection() {
        val batteryStatus = IntentFilter(Intent.ACTION_BATTERY_CHANGED).let { iFilter ->
            this.registerReceiver(null, iFilter)
        }

        // Detect if the device is charged
        when (batteryStatus?.getIntExtra(BatteryManager.EXTRA_STATUS, -1) ?: "NONE") {
            BatteryManager.BATTERY_STATUS_CHARGING ->
                _sensorData.value.batteryStatus = "CHARGING"
            BatteryManager.BATTERY_STATUS_FULL ->
                _sensorData.value.batteryStatus = "CHARGING"
            BatteryManager.BATTERY_STATUS_NOT_CHARGING ->
                _sensorData.value.batteryStatus = "NOT_CHARGING"
            "NONE" ->
                _sensorData.value.chargingType = "NONE"
        }

        // Detect how the device is charged
        when (batteryStatus?.getIntExtra(BatteryManager.EXTRA_PLUGGED, -1) ?: "NONE") {
            BatteryManager.BATTERY_PLUGGED_USB ->
                _sensorData.value.chargingType = "USB"
            BatteryManager.BATTERY_PLUGGED_AC ->
                _sensorData.value.chargingType = "AC"
            BatteryManager.BATTERY_PLUGGED_WIRELESS ->
                _sensorData.value.chargingType = "WIRELESS"
            "NONE" ->
                _sensorData.value.chargingType = "NONE"
        }
    }

    inner class ActivityTransitionReceiver : BroadcastReceiver() {

        override fun onReceive(context: Context, intent: Intent) {
            if (ActivityTransitionResult.hasResult(intent)) {

                val result = ActivityTransitionResult.extractResult(intent)
                for (event in result!!.transitionEvents) {
                    val activity = activityType(event.activityType)
                    val transition = transitionType(event.transitionType)
                    val message = "Transition: $activity ($transition)"

                    Log.d("DetectedActReceiver", message)

                    Toast.makeText(context, message, Toast.LENGTH_LONG).show()

                    context.sendBroadcast(Intent("MyAction"))

                    _sensorData.value.currentState = activity
                }
            }
        }

        private fun transitionType(transitionType: Int): String {
            return when (transitionType) {
                ActivityTransition.ACTIVITY_TRANSITION_ENTER -> "ENTER"
                ActivityTransition.ACTIVITY_TRANSITION_EXIT -> "EXIT"
                else -> "UNKNOWN"
            }
        }

        private fun activityType(activity: Int): String {
            return when (activity) {
                DetectedActivity.IN_VEHICLE -> "IN_VEHICLE"
                DetectedActivity.STILL -> "STILL"
                DetectedActivity.WALKING -> "WALKING"
                DetectedActivity.RUNNING -> "RUNNING"
                else -> "UNKNOWN"
            }
        }
    }
}