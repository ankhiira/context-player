package com.gabchmel.sensorprocessor.data.service

import android.Manifest
import android.app.PendingIntent
import android.app.Service
import android.bluetooth.BluetoothAdapter
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
import com.gabchmel.common.data.ChargingMethod
import com.gabchmel.common.data.LocalBinder
import com.gabchmel.common.data.NetworkType
import com.gabchmel.common.data.SensorValues
import com.gabchmel.common.data.UserActivity
import com.gabchmel.common.data.dataStore.DataStore
import com.gabchmel.common.utils.dataCsvFileName
import com.gabchmel.predicitonmodule.PredictionModelBuiltIn
import com.gabchmel.sensorprocessor.data.model.ProcessedCsvValues
import com.gabchmel.sensorprocessor.data.receiver.ActivityTransitionReceiver
import com.gabchmel.sensorprocessor.data.receiver.TransitionList
import com.gabchmel.sensorprocessor.utils.InputProcessHelper.getProcessedSensorValues
import com.gabchmel.sensorprocessor.utils.InputProcessHelper.processInputCSV
import com.gabchmel.sensorprocessor.utils.SensorReader
import com.google.android.gms.location.ActivityRecognition
import com.google.android.gms.location.ActivityTransitionRequest
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import kotlinx.datetime.Clock
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import java.io.File
import java.io.IOException
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine
import kotlin.math.acos
import kotlin.math.roundToInt
import kotlin.math.sqrt
import kotlin.reflect.full.memberProperties


class SensorDataProcessingService : Service() {

    private val job = SupervisorJob()
    private val coroutineScope = CoroutineScope(Dispatchers.Default + job)

    private val _sensorValues = MutableStateFlow(SensorValues())
    val sensorValues: StateFlow<SensorValues> = _sensorValues

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

    override fun onCreate() {
        super.onCreate()

        // CSV file with sensor measurements and context data
        csvFile = File(this.filesDir, dataCsvFileName)

        registerLocationListener()
        registerActivityDetectionListener()
    }

    override fun onBind(intent: Intent): IBinder {
        onServiceBound()

        return binder
    }

    private fun onServiceBound() {
        coroutineScope.launch {
            val sensorTypes = listOf(
                Sensor.TYPE_ACCELEROMETER,
                Sensor.TYPE_LIGHT,
                Sensor.TYPE_AMBIENT_TEMPERATURE,
                Sensor.TYPE_PROXIMITY,
                Sensor.TYPE_HEART_RATE
            )

            headphonesPluggedInDetection()
            readAdditionalInformation()

            sensorTypes.forEach { sensorType ->

                SensorReader.registerSensorReader(
                    baseContext, sensorType,
                ).onEach { sensorValue ->
                    when (sensorType) {
                        Sensor.TYPE_ACCELEROMETER -> {
                            sensorValues.value.isDeviceLying =
                                isDeviceLying(sensorValue.toMutableList())
                        }

                        Sensor.TYPE_LIGHT -> {
                            sensorValues.value.lightSensorValue = sensorValue.first()
                        }

                        Sensor.TYPE_AMBIENT_TEMPERATURE -> {
                            sensorValues.value.temperature = sensorValue.first()
                        }

                        Sensor.TYPE_PROXIMITY -> {
                            sensorValues.value.proximity = sensorValue.first()
                        }

                        Sensor.TYPE_HEART_RATE -> {
                            sensorValues.value.heartRate = sensorValue.first()
                        }
                    }

                    DataStore.saveSensorData(
                        context = this@SensorDataProcessingService,
                        sensorData = sensorValues.value
                    )
                }.launchIn(coroutineScope)
            }
        }
    }

    /**
     * Writes sensor values to CSV file
     *
     * @param songID
     */
    suspend fun writeToFile(songID: String) {
        //TODO update periodically
        readAdditionalInformation()

        var sensorValuesCount = 1
        for (prop in SensorValues::class.memberProperties) {
            sensorValuesCount++
        }

        if (csvFile.length() == 0L) {
            // If the file is empty, save the current MeasuredSensorValues class size
            val editor = getSharedPreferences("MyPrefsFile", MODE_PRIVATE).edit()
            editor.putInt("csv", sensorValuesCount)
            editor.apply()
        } else {
            // If the file is not empty, then check if the size of SensorData didn't change
            val prefs = getSharedPreferences("MyPrefsFile", MODE_PRIVATE)
            val counterOld = prefs.getInt("csv", 0)
            if (counterOld != sensorValuesCount && counterOld != 0) {
                // If the size changed, delete the CSV file
                val inputFile = File(this.filesDir, dataCsvFileName)
                if (inputFile.exists()) {
                    this.deleteFile(dataCsvFileName)
                }
                // Create new CSV file
                csvFile = File(this.filesDir, dataCsvFileName)
            }
        }

        try {
            // Write to csv file
            sensorValues.value.also {
                csvFile.appendText(
                    songID + ","
                            + it.currentTime + ","
                            + it.longitude + ","
                            + it.latitude + ","
                            + it.userActivity + ","
                            + it.lightSensorValue + ","
                            + it.temperature + ","
                            + it.proximity + ","
                            + it.isDeviceLying + ","
                            + it.isHeadphonesPluggedIn + ","
                            + it.isBluetoothDeviceConnected + ","
                            + it.wifiSsid + ","
                            + it.networkConnectionType + ","
                            + it.isDeviceCharging + ","
                            + it.chargingType + ","
                            + it.heartRate + "\n"
                )
            }
        } catch (e: IOException) {
            Log.e("Err", "Couldn't write to file", e)
        }
    }

    fun createModel(): Boolean {
        processedCsvValues = processInputCSV(this)

        //TODO: If we don't have enough input data, don't create a model
        return predictionModel.createModel(
            processedCsvValues.classNames,
            processedCsvValues.wifiNames
        )
    }

    suspend fun triggerPrediction(): String? {
        readAdditionalInformation()

        val input = getProcessedSensorValues(sensorValues.value)

        // Check for the case that there is different wifi
        if (processedCsvValues.wifiNames.contains(input.wifi)) {
            return predictionModel.predict(input, processedCsvValues.classNames)
        }

        return null
    }

    private suspend fun SensorDataProcessingService.readAdditionalInformation() {
        _sensorValues.value.also {
            it.currentTime = Clock.System.now().toLocalDateTime(TimeZone.UTC)
            it.wifiSsid = getConnectedWiFiSsid()
            it.networkConnectionType = getCurrentNetworkConnectionType(this)
            it.isDeviceCharging = isDeviceCharging()
            it.chargingType = getChargingMethod()
            it.isBluetoothDeviceConnected = isBluetoothDeviceConnected(this)
        }
    }

    suspend fun hasContextChanged(): Boolean {
        val sensorDataFlow = DataStore.getSensorDataFlow(this)
        var result = false

        sensorDataFlow.collectLatest { sensorData ->
            result = when {
                (sensorValues.value.userActivity != sensorData.userActivity
                        && sensorData.userActivity != UserActivity.UNKNOWN) -> true

                (sensorValues.value.isDeviceLying != sensorData.isDeviceLying
                        && sensorData.isDeviceLying != null) -> true

                (sensorValues.value.isBluetoothDeviceConnected != sensorData.isBluetoothDeviceConnected
                        && sensorData.isBluetoothDeviceConnected != null) -> true

                (sensorValues.value.isHeadphonesPluggedIn != sensorData.isHeadphonesPluggedIn
                        && sensorData.isHeadphonesPluggedIn != null) -> true

                (sensorValues.value.wifiSsid != sensorData.wifiSsid
                        && sensorData.wifiSsid != null) -> true

                (sensorValues.value.networkConnectionType != sensorData.networkConnectionType
                        && sensorData.networkConnectionType != NetworkType.NONE) -> true

                (sensorValues.value.isDeviceCharging != sensorData.isDeviceCharging
                        && sensorData.isDeviceCharging != null) -> true

                (sensorValues.value.chargingType != sensorData.chargingType
                        && sensorData.chargingType != null) -> true

                else -> false
            }
        }

        return result
    }

    /**
     * Method to detect user activity
     *
     */
    private fun registerActivityDetectionListener() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            return
        }

        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACTIVITY_RECOGNITION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            //TODO request permission
            Log.e("permission", "permission not granted")
            return
        }

        val request = ActivityTransitionRequest(TransitionList.getTransitions())
        val intent = Intent(this, ActivityTransitionReceiver::class.java)
        val pendingIntent =
            PendingIntent.getBroadcast(this, 0, intent, PendingIntent.FLAG_IMMUTABLE)

        val task = ActivityRecognition.getClient(this)
            .requestActivityTransitionUpdates(request, pendingIntent)

        task.addOnSuccessListener {
        }

        task.addOnFailureListener { e: Exception ->
            Log.e(
                "ActivityRecognition",
                "Transitions Api could NOT be registered ${e.localizedMessage}"
            )
        }
    }

    /**
     * Detection if the device is lying
     *
     * @param coordinates X, Y, Z coordinates
     */
    private fun isDeviceLying(coordinates: MutableList<Float>): Boolean {
        // Inspired by: https://stackoverflow.com/questions/30948131/how-to-know-if-android-device-is-flat-on-table
        when (coordinates.size) {
            3 -> {
                val norm = sqrt(
                    (coordinates[0] * coordinates[0]
                            + coordinates[1] * coordinates[1]
                            + coordinates[2] * coordinates[2])
                        .toDouble()
                )

                // Normalize the accelerometer vector
                repeat(3) { index ->
                    coordinates[index] = (coordinates[index] / norm).toFloat()
                }

                val inclination = Math.toDegrees(acos(coordinates[2]).toDouble()).roundToInt()

                // Device detected as lying is with inclination in range < 25 or > 155 degrees
                return inclination < 25 || inclination > 155
            }

            else -> return false
        }
    }

    private fun isBluetoothDeviceConnected(context: Context): Boolean? {
        // Check if the device supports bluetooth
        if (!packageManager.hasSystemFeature(PackageManager.FEATURE_BLUETOOTH)) {
            return null
        }

        val bluetoothManager =
            context.getSystemService(BLUETOOTH_SERVICE) as BluetoothManager
        val bluetoothAdapter = bluetoothManager.adapter ?: return null

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
            return null
        }

        return bluetoothAdapter.getProfileConnectionState(BluetoothProfile.HEADSET) == BluetoothAdapter.STATE_CONNECTED
    }

    // Function to detect if the headphones are plugged in
    private fun headphonesPluggedInDetection() {
        val broadcastReceiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context?, intent: Intent) {
                if (intent.action == Intent.ACTION_HEADSET_PLUG) {
                    _sensorValues.value.isHeadphonesPluggedIn =
                        intent.getIntExtra("state", -1) == 1
                }
            }
        }

        val receiverFilter = IntentFilter(Intent.ACTION_HEADSET_PLUG)
        registerReceiver(broadcastReceiver, receiverFilter)
    }

    private suspend fun getConnectedWiFiSsid(): UInt? {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val request = NetworkRequest.Builder()
                .addTransportType(NetworkCapabilities.TRANSPORT_WIFI)
                .build()
            val connectivityManager =
                applicationContext.getSystemService(ConnectivityManager::class.java)

            return suspendCoroutine { continuation ->
                connectivityManager.registerNetworkCallback(request, object : NetworkCallback() {
                    override fun onAvailable(network: Network) {}

                    override fun onCapabilitiesChanged(
                        network: Network,
                        networkCapabilities: NetworkCapabilities
                    ) {
                        val wifiInfo = networkCapabilities.transportInfo as WifiInfo

                        if (wifiInfo.ssid == "<unknown ssid>") {
                            continuation.resume(null)
                        } else {
                            continuation.resume(wifiInfo.ssid.hashCode().toUInt())
                        }
                    }
                })
            }
        } else {
            val wifiManager = applicationContext.getSystemService(WIFI_SERVICE) as WifiManager
            @Suppress("DEPRECATION") val wifiInfo = wifiManager.connectionInfo

            if (wifiInfo.ssid == "<unknown ssid>") {
                return null
            }

            return wifiInfo.ssid.hashCode().toUInt()
        }
    }

    private fun getCurrentNetworkConnectionType(context: Context): NetworkType {
        val connectivityManager =
            context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val capabilities: NetworkCapabilities?
        // Function activeNetwork requires minimal level 23
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            capabilities =
                connectivityManager.getNetworkCapabilities(connectivityManager.activeNetwork)
        } else {
            return NetworkType.NONE
        }

        capabilities?.let {
            when {
                capabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) -> {
                    return NetworkType.CELLULAR
                }

                capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) -> {
                    return NetworkType.TRANSPORT_WIFI
                }

                else -> return NetworkType.NONE
            }
        }

        return NetworkType.NONE
    }

    /**
     * Current location detection
     *
     */
    fun registerLocationListener() {
        val locationListener = LocationListener { location ->
            _sensorValues.value.longitude = location.longitude
            _sensorValues.value.latitude = location.latitude
        }

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

    private fun isDeviceCharging(): Boolean {
        val batteryStatus = IntentFilter(Intent.ACTION_BATTERY_CHANGED).let { iFilter ->
            this.registerReceiver(null, iFilter)
        }

        return when (batteryStatus?.getIntExtra(BatteryManager.EXTRA_STATUS, -1)) {
            BatteryManager.BATTERY_STATUS_CHARGING -> true

            //TODO test if necessary, if it is not enough to have only the charging
            BatteryManager.BATTERY_STATUS_FULL -> true

            BatteryManager.BATTERY_STATUS_NOT_CHARGING -> false

            else -> false
        }
    }

    private fun getChargingMethod(): ChargingMethod {
        val batteryStatus = IntentFilter(Intent.ACTION_BATTERY_CHANGED).let { iFilter ->
            this.registerReceiver(null, iFilter)
        }

        return when (batteryStatus?.getIntExtra(BatteryManager.EXTRA_PLUGGED, -1)) {
            BatteryManager.BATTERY_PLUGGED_USB -> ChargingMethod.USB

            BatteryManager.BATTERY_PLUGGED_AC -> ChargingMethod.AC

            BatteryManager.BATTERY_PLUGGED_WIRELESS -> ChargingMethod.WIRELESS

            //TODO decide if it is better for the model to use NONE or null
            else -> ChargingMethod.NONE
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        job.cancel()
    }
}