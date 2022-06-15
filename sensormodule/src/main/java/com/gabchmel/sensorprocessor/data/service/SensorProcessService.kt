package com.gabchmel.sensorprocessor.data.service

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
import androidx.core.app.ActivityCompat
import com.gabchmel.common.data.ConvertedData
import com.gabchmel.common.data.LocalBinder
import com.gabchmel.predicitonmodule.PredictionModelBuiltIn
import com.gabchmel.sensorprocessor.data.receiver.ActivityTransitionReceiver
import com.gabchmel.sensorprocessor.data.receiver.TransitionList
import com.gabchmel.sensorprocessor.data.model.SensorData
import com.gabchmel.sensorprocessor.utils.InputProcessHelper.inputProcessHelper
import com.gabchmel.sensorprocessor.utils.InputProcessHelper.processInputCSV
import com.gabchmel.sensorprocessor.utils.SensorManagerUtility
import com.google.android.gms.location.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import java.io.File
import java.io.IOException
import java.util.*
import kotlin.math.acos
import kotlin.math.roundToInt
import kotlin.math.sqrt
import kotlin.reflect.full.memberProperties


class SensorProcessService : Service() {

    companion object {
        // Structure to store sensor values
        var _sensorData = MutableStateFlow(SensorData())
    }

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

    private var wifiNamesList = arrayListOf<UInt>()

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

    // Function to write sensor values to file
    fun writeToFile(songID: String) {
        // Read current time
        _sensorData.value.currentTime = Calendar.getInstance().time

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

        var counter = 1
        for (prop in SensorData::class.memberProperties) {
            counter++
        }

        // Check the size of SensorData structure if it didn't change
        if (csvFile.length() == 0L) {
            // If the file is empty, save the current SensorData size
            val editor = getSharedPreferences("MyPrefsFile", MODE_PRIVATE).edit()
            editor.putInt("csv", counter)
            editor.apply()
        } else {
            // If the file is not empty, then check if the size of SensorData didn't change
            val prefs = getSharedPreferences("MyPrefsFile", MODE_PRIVATE)
            val counterOld = prefs.getInt("csv", 0)
            if (counterOld != counter && counterOld != 0) {
                // If the size changed, delete the CSV file
                val inputFile = File(this.filesDir, "data.csv")
                if (inputFile.exists()) {
                    this.deleteFile("data.csv")
                }
                // Create new CSV file
                csvFile = File(this.filesDir, "data.csv")
            }
        }

        try {
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

    suspend fun createModel(): Boolean {
        // Process input CSV file and save class names and wifi list into ArrayList<String>
        val (classNamesNew, wifiList) = processInputCSV(this)

        classNames = classNamesNew
        wifiNamesList = wifiList

        // If we don't have enough input data, don't create a model
        if (!predictionModel.createModel(classNames, wifiList)) {
            return false
        }
        return true
    }

    fun triggerPrediction(): ConvertedData {

        // Read current time
        _sensorData.value.currentTime = Calendar.getInstance().time

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

        // Get the processed input values
        val input = inputProcessHelper(sensorData.value)

        // Check for the case that there is different wifi
        if (wifiNamesList.contains(input.wifi)) {
            _prediction.value = predictionModel.predict(input, classNames)
        }

        return input
    }

    // Function to compare measured values with saved to determine change in context
    fun detectContextChange(): Boolean {
        val prefs = getSharedPreferences("MyPrefsFile", MODE_PRIVATE)
        val state = prefs.getString("state", "UNDEFINED")
        val deviceLying = prefs.getFloat("deviceLying", -1.0f)
        val bluetooth = prefs.getFloat("bluetooth", -1.0f)
        val headphones = prefs.getFloat("headphones", -1.0f)
        val wifi = prefs.getInt("wifi", -1)
        val connection = prefs.getString("connection", "UNDEFINED")
        val batteryStat = prefs.getString("batteryStat", "UNDEFINED")
        val chargingType = prefs.getString("chargingType", "UNDEFINED")
        when {
            (sensorData.value.currentState != state && state != "UNDEFINED") ->
                return true
            (sensorData.value.deviceLying != deviceLying && deviceLying != -1.0f) ->
                return true
            (sensorData.value.BTdeviceConnected != bluetooth && bluetooth != -1.0f) ->
                return true
            (sensorData.value.headphonesPluggedIn != headphones && headphones != -1.0f) ->
                return true
            (sensorData.value.wifi != wifi.toUInt() && wifi != -1) ->
                return true
            (sensorData.value.connection != connection && connection != "UNDEFINED") ->
                return true
            (sensorData.value.batteryStatus != batteryStat && batteryStat != "UNDEFINED") ->
                return true
            (sensorData.value.chargingType != chargingType && chargingType != "UNDEFINED") ->
                return true
        }
        return false
    }

    // Function to save current context values to shared preferences for later comparison
    fun saveSensorData() {
        val editor = getSharedPreferences("MyPrefsFile", MODE_PRIVATE).edit()
        editor.putString("state", sensorData.value.currentState)
        sensorData.value.deviceLying?.let { editor.putFloat("deviceLying", it) }
        sensorData.value.BTdeviceConnected?.let { editor.putFloat("bluetooth", it) }
        sensorData.value.headphonesPluggedIn?.let { editor.putFloat("headphones", it) }
        sensorData.value.wifi.let { editor.putInt("wifi", it.toInt()) }
        editor.putString("connection", sensorData.value.connection)
        editor.putString("batteryStat", sensorData.value.batteryStatus)
        editor.putString("chargingType", sensorData.value.chargingType)
        editor.apply()
    }

    // Function to detect current activity
    private fun activityDetection() {

        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACTIVITY_RECOGNITION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            Log.e("permission", "permission not granted")
            return
        }

        val request = ActivityTransitionRequest(TransitionList.getTransitions())

        val intent = Intent(this, ActivityTransitionReceiver::class.java)
        val pendingIntent = PendingIntent.getBroadcast(this, 0, intent, 0)

        // pendingIntent is the instance of PendingIntent where the app receives callbacks.
        val task = ActivityRecognition.getClient(this)
            .requestActivityTransitionUpdates(request, pendingIntent)

        // implemented from: https://heartbeat.fritz.ai/detect-users-activity-in-android-using-activity-transition-api-f718c844efb2
        task.addOnSuccessListener {
        }

        task.addOnFailureListener { e: Exception ->
            // Handle error
            Log.e(
                "ActivityRecognition",
                "Transitions Api could NOT be registered ${e.localizedMessage}"
            )
        }
    }

    // Function for detection if the device is lying
    private fun processOrientation() {

        // Inspired by: https://stackoverflow.com/questions/30948131/how-to-know-if-android-device-is-flat-on-table
        if (coordList.size == 3) {
            val norm = sqrt(
                (coordList[0] * coordList[0]
                        + coordList[1] * coordList[1] + coordList[2] * coordList[2]).toDouble()
            )

            // Normalize the accelerometer vector
            coordList[0] = (coordList[0] / norm).toFloat()
            coordList[1] = (coordList[1] / norm).toFloat()
            coordList[2] = (coordList[2] / norm).toFloat()

            val inclination = Math.toDegrees(acos(coordList[2]).toDouble()).roundToInt()

            // Device detected as lying is with inclination in range < 25 or > 155 degrees
            _sensorData.value.deviceLying = if (inclination < 25 || inclination > 155) {
                1.0f
            } else {
                0.0f
            }
        } else {
            _sensorData.value.deviceLying = 0.0f
        }
    }

    // Function to detect connection of the bluetooth headset
    private fun bluetoothDevicesConnection() {
        // Check if the device supports bluetooth
        if (packageManager.hasSystemFeature(PackageManager.FEATURE_BLUETOOTH)) {
            val bluetoothAdapter = BluetoothAdapter.getDefaultAdapter()

            // Check if the bluetooth headphones are connected
            _sensorData.value.BTdeviceConnected =
                if (bluetoothAdapter != null && BluetoothProfile.STATE_CONNECTED
                    == bluetoothAdapter.getProfileConnectionState(
                        BluetoothProfile.HEADSET
                    )
                ) {
                    1.0f
                } else {
                    0.0f
                }
        }
    }

    // Function to detect if the headphones are plugged in
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

    // Function for connected wifi name detection
    private fun wifiConnection() {
        val wifiManager = applicationContext.getSystemService(WIFI_SERVICE) as WifiManager
        val wifiInfo = wifiManager.connectionInfo

        if (wifiInfo.ssid != "<unknown ssid>") {
            _sensorData.value.wifi = wifiInfo.ssid.hashCode().toUInt()
        }
    }

    // Function to retrieve current internet connection type
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
                    _sensorData.value.connection = "TRANSPORT_CELLULAR"
                }
                capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) -> {
                    _sensorData.value.connection = "TRANSPORT_WIFI"
                }
                capabilities.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET) -> {
                    _sensorData.value.connection = "TRANSPORT_ETHERNET"
                }
            }
        } else {
            _sensorData.value.connection = "NONE"
        }
    }

    // Function for current location detection
    fun registerLocationListener() {
        // Location change listener
        val locationListener = LocationListener { location ->
            _sensorData.value.longitude = location.longitude
            _sensorData.value.latitude = location.latitude
        }

        // Persistent LocationManager reference
        val locationManager = this.getSystemService(LOCATION_SERVICE) as LocationManager?

        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED || ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_COARSE_LOCATION
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
}