package com.gabchmel.sensorprocessor.data.service

import android.Manifest
import android.app.PendingIntent
import android.app.Service
import android.bluetooth.BluetoothManager
import android.bluetooth.BluetoothProfile
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.hardware.Sensor
import android.location.LocationListener
import android.location.LocationManager
import android.net.ConnectivityManager
import android.net.ConnectivityManager.NetworkCallback
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import android.net.wifi.WifiInfo
import android.net.wifi.WifiManager
import android.os.BatteryManager
import android.os.Build
import android.os.IBinder
import android.util.Log
import androidx.core.app.ActivityCompat
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.floatPreferencesKey
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import com.gabchmel.common.data.ConvertedData
import com.gabchmel.common.data.LocalBinder
import com.gabchmel.common.data.SensorDataPreferences
import com.gabchmel.common.data.dataStore.DataStore.dataStore
import com.gabchmel.predicitonmodule.PredictionModelBuiltIn
import com.gabchmel.sensorprocessor.data.model.MeasuredSensorValues
import com.gabchmel.sensorprocessor.data.model.ProcessedCsvValues
import com.gabchmel.sensorprocessor.data.receiver.ActivityTransitionReceiver
import com.gabchmel.sensorprocessor.data.receiver.TransitionList
import com.gabchmel.sensorprocessor.utils.InputProcessHelper.inputProcessHelper
import com.gabchmel.sensorprocessor.utils.InputProcessHelper.processInputCSV
import com.gabchmel.sensorprocessor.utils.SensorReader
import com.google.android.gms.location.ActivityRecognition
import com.google.android.gms.location.ActivityTransitionRequest
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import java.io.File
import java.io.IOException
import java.util.*
import kotlin.math.acos
import kotlin.math.roundToInt
import kotlin.math.sqrt
import kotlin.reflect.full.memberProperties


class SensorDataProcessingService : Service() {

    // Structure to store sensor values
    private val _measuredSensorValues = MutableStateFlow(MeasuredSensorValues())
    val measuredSensorValues: StateFlow<MeasuredSensorValues> = _measuredSensorValues
    private val coords = mutableListOf<Float>()

    // Input CSV file to save sensor values
    private lateinit var csvFile: File

    // The Random Forest model
    private val predictionModel = PredictionModelBuiltIn(this)

    // Saved prediction result as StateFlow to show notification
    private val _prediction = MutableStateFlow<String?>(null)
    val prediction: StateFlow<String?> = _prediction

    // Service binder given to clients
    private val binder = object : LocalBinder<SensorDataProcessingService>() {
        override fun getService() = this@SensorDataProcessingService
    }

    private var processedCsvValues: ProcessedCsvValues = ProcessedCsvValues()

    private object PreferencesKeys {
        val STATE = stringPreferencesKey("state")
        val IS_DEVICE_LYING = floatPreferencesKey("isDeviceLying")
        val IS_BT_DEVICE_CONNECTED = floatPreferencesKey("isBluetoothDeviceConnected")
        val ARE_HEADPHONES_CONNECTED = floatPreferencesKey("areHeadphonesConnected")
        val CONNECTED_WIFI_SSID = intPreferencesKey("connectedWifiSsid")
        val CURRENT_NETWORK_CONNECTION = stringPreferencesKey("currentNetworkConnection")
        val BATTERY_STATUS = stringPreferencesKey("batteryState")
        val CHARGING_METHOD = stringPreferencesKey("chargingMethod")
    }

    override fun onCreate() {
        super.onCreate()

        // CSV file with sensor measurements and context data
        csvFile = File(this.filesDir, "data.csv")

        registerLocationListener()
        registerActivityDetectionListener()
    }

    override fun onBind(intent: Intent): IBinder {
        CoroutineScope(Dispatchers.Default).launch {
            // Register listeners to sensor value changes
            SensorReader.registerSensorReader(
                baseContext, Sensor.TYPE_ACCELEROMETER,
            ).collect { values: Collection<Float> -> coords.addAll(values) }

            SensorReader.registerSensorReader(
                baseContext, Sensor.TYPE_LIGHT,
            ).collect { value: Collection<Float> ->
                measuredSensorValues.value.lightSensorValue = value.first()
            }

            SensorReader.registerSensorReader(
                baseContext, Sensor.TYPE_PRESSURE,
            ).collect { value: Collection<Float> ->
                measuredSensorValues.value.pressure = value.first()
            }

            SensorReader.registerSensorReader(
                baseContext, Sensor.TYPE_AMBIENT_TEMPERATURE,
            ).collect { value: Collection<Float> ->
                measuredSensorValues.value.temperature = value.first()
            }

            SensorReader.registerSensorReader(
                baseContext, Sensor.TYPE_PROXIMITY,
            ).collect { value: Collection<Float> ->
                measuredSensorValues.value.proximity = value.first()
            }

            SensorReader.registerSensorReader(
                baseContext, Sensor.TYPE_RELATIVE_HUMIDITY,
            ).collect { value: Collection<Float> ->
                measuredSensorValues.value.humidity = value.first()
            }

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                SensorReader.registerSensorReader(
                    baseContext, Sensor.TYPE_HEART_BEAT,
                ).collect { value: Collection<Float> ->
                    measuredSensorValues.value.heartBeat = value.first()
                }
            }

            SensorReader.registerSensorReader(
                baseContext, Sensor.TYPE_HEART_RATE,
            ).collect { value: Collection<Float> ->
                measuredSensorValues.value.heartRate = value.first()
            }
        }

        headphonesPluggedInDetection()

        return binder
    }

    // Function to write sensor values to file
    fun writeToFile(songID: String) {
        // Read current time
        _measuredSensorValues.value.currentTime = Calendar.getInstance().time

        saveConnectedWiFiSSID()
        saveCurrentNetworkConnectionType(this)
        processCurrentOrientation()
        detectPowerSourceConnection()
        detectPowerSourceConnectionType()
        detectBluetoothDevicesConnection(this)

        var measuredSensorValuesCount = 1
        for (prop in MeasuredSensorValues::class.memberProperties) {
            measuredSensorValuesCount++
        }

        if (csvFile.length() == 0L) {
            // If the file is empty, save the current MeasuredSensorValues class size
            val editor = getSharedPreferences("MyPrefsFile", MODE_PRIVATE).edit()
            editor.putInt("csv", measuredSensorValuesCount)
            editor.apply()
        } else {
            // If the file is not empty, then check if the size of SensorData didn't change
            val prefs = getSharedPreferences("MyPrefsFile", MODE_PRIVATE)
            val counterOld = prefs.getInt("csv", 0)
            if (counterOld != measuredSensorValuesCount && counterOld != 0) {
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
                        + measuredSensorValues.value.currentTime + ","
                        + measuredSensorValues.value.longitude + ","
                        + measuredSensorValues.value.latitude + ","
                        + measuredSensorValues.value.currentState + ","
                        + measuredSensorValues.value.lightSensorValue + ","
                        + measuredSensorValues.value.deviceLying + ","
                        + measuredSensorValues.value.BTDeviceConnected + ","
                        + measuredSensorValues.value.headphonesPluggedIn + ","
                        + measuredSensorValues.value.pressure + ","
                        + measuredSensorValues.value.temperature + ","
                        + measuredSensorValues.value.wifi + ","
                        + measuredSensorValues.value.connection + ","
                        + measuredSensorValues.value.batteryStatus + ","
                        + measuredSensorValues.value.chargingType + ","
                        + measuredSensorValues.value.proximity + ","
                        + measuredSensorValues.value.humidity + ","
                        + measuredSensorValues.value.heartBeat + ","
                        + measuredSensorValues.value.heartRate + "\n"
            )
        } catch (e: IOException) {
            Log.e("Err", "Couldn't write to file", e)
        }
    }

    fun createModel(): Boolean {
        processedCsvValues =
            ProcessedCsvValues(
                processInputCSV(this).classNames,
                processInputCSV(this).wifiNames
            )

        // If we don't have enough input data, don't create a model
        return predictionModel.createModel(
            processedCsvValues.classNames,
            processedCsvValues.wifiNames
        )
    }

    fun triggerPrediction(): ConvertedData {
        _measuredSensorValues.value.currentTime = Calendar.getInstance().time

        saveConnectedWiFiSSID()
        saveCurrentNetworkConnectionType(this)
        processCurrentOrientation()
        detectPowerSourceConnection()
        detectBluetoothDevicesConnection(this)

        // Get the processed input values
        val input = inputProcessHelper(measuredSensorValues.value)

        // Check for the case that there is different wifi
        if (processedCsvValues.wifiNames.contains(input.wifi)) {
            _prediction.value = predictionModel.predict(input, processedCsvValues.classNames)
        }

        return input
    }

    suspend fun hasContextChanged(): Boolean {
        val savedGlobalPreferences = dataStore.data
            .map { preferences ->
                SensorDataPreferences(
                    state =
                    preferences[PreferencesKeys.STATE] ?: "",
                    isDeviceLying =
                    preferences[PreferencesKeys.IS_DEVICE_LYING] ?: -1.0f,
                    isBluetoothDeviceConnected =
                    preferences[PreferencesKeys.IS_BT_DEVICE_CONNECTED] ?: -1.0f,
                    areHeadphonesConnected =
                    preferences[PreferencesKeys.ARE_HEADPHONES_CONNECTED] ?: -1.0f,
                    connectedWifiSsid =
                    preferences[PreferencesKeys.CONNECTED_WIFI_SSID] ?: -1,
                    currentNetworkConnection =
                    preferences[PreferencesKeys.CURRENT_NETWORK_CONNECTION] ?: "UNDEFINED",
                    batteryState =
                    preferences[PreferencesKeys.BATTERY_STATUS] ?: "UNDEFINED",
                    chargingMethod =
                    preferences[PreferencesKeys.CHARGING_METHOD] ?: "UNDEFINED"
                )
            }

        var result = false

        savedGlobalPreferences.collect { prefs ->
            result = when {
                (measuredSensorValues.value.currentState != prefs.state
                        && prefs.state != "UNDEFINED") -> true
                (measuredSensorValues.value.deviceLying != prefs.isDeviceLying
                        && prefs.isDeviceLying != -1.0f) -> true
                (measuredSensorValues.value.BTDeviceConnected != prefs.isBluetoothDeviceConnected
                        && prefs.isBluetoothDeviceConnected != -1.0f) -> true
                (measuredSensorValues.value.headphonesPluggedIn != prefs.areHeadphonesConnected
                        && prefs.areHeadphonesConnected != -1.0f) -> true
                (measuredSensorValues.value.wifi != prefs.connectedWifiSsid.toUInt()
                        && prefs.connectedWifiSsid != -1) -> true
                (measuredSensorValues.value.connection != prefs.currentNetworkConnection
                        && prefs.currentNetworkConnection != "UNDEFINED") -> true
                (measuredSensorValues.value.batteryStatus != prefs.batteryState
                        && prefs.batteryState != "UNDEFINED") -> true
                (measuredSensorValues.value.chargingType != prefs.chargingMethod
                        && prefs.chargingMethod != "UNDEFINED") -> true
                else -> false
            }
        }

        return result
    }

    // Function to save current context values to shared preferences for later comparison
    suspend fun saveSensorValuesToSharedPrefs() {
        dataStore.edit { preferences ->
            preferences[PreferencesKeys.STATE] = _measuredSensorValues.value.currentState
            preferences[PreferencesKeys.IS_DEVICE_LYING] = _measuredSensorValues.value.deviceLying
            preferences[PreferencesKeys.IS_BT_DEVICE_CONNECTED] = measuredSensorValues.value.BTDeviceConnected
            preferences[PreferencesKeys.ARE_HEADPHONES_CONNECTED] = measuredSensorValues.value.headphonesPluggedIn
            preferences[PreferencesKeys.CONNECTED_WIFI_SSID] = measuredSensorValues.value.wifi.toInt()
            preferences[PreferencesKeys.CURRENT_NETWORK_CONNECTION] = measuredSensorValues.value.connection
            preferences[PreferencesKeys.BATTERY_STATUS] = measuredSensorValues.value.batteryStatus
            preferences[PreferencesKeys.CHARGING_METHOD] = measuredSensorValues.value.chargingType
        }
    }

    // Function to detect current activity
    private fun registerActivityDetectionListener() {
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACTIVITY_RECOGNITION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            Log.e("permission", "permission not granted")
            return
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val request = ActivityTransitionRequest(TransitionList.getTransitions())
            val intent = Intent(this, ActivityTransitionReceiver::class.java)
            val pendingIntent =
                PendingIntent.getBroadcast(this, 0, intent, PendingIntent.FLAG_IMMUTABLE)

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
    }

    // Function for detection if the device is lying
    private fun processCurrentOrientation() {
        // Inspired by: https://stackoverflow.com/questions/30948131/how-to-know-if-android-device-is-flat-on-table
        when (coords.size) {
            3 -> {
                val norm = sqrt(
                    (coords[0] * coords[0]
                            + coords[1] * coords[1]
                            + coords[2] * coords[2])
                        .toDouble()
                )

                // Normalize the accelerometer vector
                coords[0] = (coords[0] / norm).toFloat()
                coords[1] = (coords[1] / norm).toFloat()
                coords[2] = (coords[2] / norm).toFloat()

                val inclination = Math.toDegrees(acos(coords[2]).toDouble()).roundToInt()

                // Device detected as lying is with inclination in range < 25 or > 155 degrees
                _measuredSensorValues.value.deviceLying =
                    if (inclination < 25 || inclination > 155) 1.0f else 0.0f
            }
            else -> _measuredSensorValues.value.deviceLying = 0.0f
        }
    }

    private fun detectBluetoothDevicesConnection(context: Context) {
        // Check if the device supports bluetooth
        if (packageManager.hasSystemFeature(PackageManager.FEATURE_BLUETOOTH)) {
            val bluetoothManager =
                context.getSystemService(BLUETOOTH_SERVICE) as BluetoothManager
            val bluetoothAdapter = bluetoothManager.adapter

            // TODO check Old API level Redmi
//            val bluetoothAdapter = BluetoothAdapter.getDefaultAdapter()

            // Check if the bluetooth headphones are connected
            if (ActivityCompat.checkSelfPermission(
                    this,
                    Manifest.permission.BLUETOOTH_CONNECT
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                // TODO: Consider calling
                //    ActivityCompat#requestPermissions
                // here to request the missing permissions, and then overriding
                //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                //                                          int[] grantResults)
                // to handle the case where the user grants the permission. See the documentation
                // for ActivityCompat#requestPermissions for more details.
                return
            }
            bluetoothAdapter?.let {
                _measuredSensorValues.value.BTDeviceConnected =
                    if (BluetoothProfile.STATE_CONNECTED
                        == bluetoothAdapter.getProfileConnectionState(BluetoothProfile.HEADSET)
                    ) 1.0f else 0.0f
            }
        }
    }

    // Function to detect if the headphones are plugged in
    private fun headphonesPluggedInDetection() {
        val broadcastReceiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context?, intent: Intent) {
                val action = intent.action
                if (Intent.ACTION_HEADSET_PLUG == action) {
                    _measuredSensorValues.value.headphonesPluggedIn =
                        intent.getIntExtra("state", -1).toFloat()
                }
            }
        }

        val receiverFilter = IntentFilter(Intent.ACTION_HEADSET_PLUG)
        registerReceiver(broadcastReceiver, receiverFilter)
    }

    // Function for connected wifi name detection
    private fun saveConnectedWiFiSSID() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val request = NetworkRequest.Builder()
                .addTransportType(NetworkCapabilities.TRANSPORT_WIFI)
                .build()
            val connectivityManager =
                applicationContext.getSystemService(ConnectivityManager::class.java)

            connectivityManager.registerNetworkCallback(request, object : NetworkCallback() {
                override fun onAvailable(network: Network) {}

                override fun onCapabilitiesChanged(
                    network: Network,
                    networkCapabilities: NetworkCapabilities
                ) {
                    val wifiInfo = networkCapabilities.transportInfo as WifiInfo

                    if (wifiInfo.ssid != "<unknown ssid>") {
                        _measuredSensorValues.value.wifi = wifiInfo.ssid.hashCode().toUInt()
                    }
                }
            })
        } else {
            val wifiManager = applicationContext.getSystemService(WIFI_SERVICE) as WifiManager
            val wifiInfo = wifiManager.connectionInfo

            if (wifiInfo.ssid != "<unknown ssid>") {
                _measuredSensorValues.value.wifi = wifiInfo.ssid.hashCode().toUInt()
            }
        }
    }

    // Function to retrieve current internet connection type
    private fun saveCurrentNetworkConnectionType(context: Context) {
        val connectivityManager =
            context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        var capabilities: NetworkCapabilities? = null
        // Function activeNetwork requires minimal level 23
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            capabilities =
                connectivityManager.getNetworkCapabilities(connectivityManager.activeNetwork)
        } else {
            _measuredSensorValues.value.connection = "NONE"
        }

        capabilities?.let {
            when {
                capabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) -> {
                    _measuredSensorValues.value.connection = "TRANSPORT_CELLULAR"
                }
                capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) -> {
                    _measuredSensorValues.value.connection = "TRANSPORT_WIFI"
                }
                capabilities.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET) -> {
                    _measuredSensorValues.value.connection = "TRANSPORT_ETHERNET"
                }
                else -> _measuredSensorValues.value.connection = "NONE"
            }
        }
    }

    // Function for current location detection
    fun registerLocationListener() {
        // Location change listener
        val locationListener = LocationListener { location ->
            _measuredSensorValues.value.longitude = location.longitude
            _measuredSensorValues.value.latitude = location.latitude
        }

        // Persistent LocationManager reference
        val locationManager = this.getSystemService(LOCATION_SERVICE) as LocationManager?

        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED ||
            ActivityCompat.checkSelfPermission(
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

    private fun detectPowerSourceConnection() {
        val batteryStatus = IntentFilter(Intent.ACTION_BATTERY_CHANGED).let { iFilter ->
            this.registerReceiver(null, iFilter)
        }

        // Detect if the device is charged
        when (batteryStatus?.getIntExtra(BatteryManager.EXTRA_STATUS, -1) ?: "NONE") {
            BatteryManager.BATTERY_STATUS_CHARGING ->
                _measuredSensorValues.value.batteryStatus = "CHARGING"
            BatteryManager.BATTERY_STATUS_FULL ->
                _measuredSensorValues.value.batteryStatus = "CHARGING"
            BatteryManager.BATTERY_STATUS_NOT_CHARGING ->
                _measuredSensorValues.value.batteryStatus = "NOT_CHARGING"
            "NONE" ->
                _measuredSensorValues.value.chargingType = "NONE"
        }
    }

    private fun detectPowerSourceConnectionType() {
        val batteryStatus = IntentFilter(Intent.ACTION_BATTERY_CHANGED).let { iFilter ->
            this.registerReceiver(null, iFilter)
        }

        when (batteryStatus?.getIntExtra(BatteryManager.EXTRA_PLUGGED, -1) ?: "NONE") {
            BatteryManager.BATTERY_PLUGGED_USB ->
                _measuredSensorValues.value.chargingType = "USB"
            BatteryManager.BATTERY_PLUGGED_AC ->
                _measuredSensorValues.value.chargingType = "AC"
            BatteryManager.BATTERY_PLUGGED_WIRELESS ->
                _measuredSensorValues.value.chargingType = "WIRELESS"
            "NONE" ->
                _measuredSensorValues.value.chargingType = "NONE"
        }
    }
}