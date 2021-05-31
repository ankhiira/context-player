package com.gabchmel.sensorprocessor

import android.Manifest
import android.app.Service
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Binder
import android.os.Bundle
import android.os.IBinder
import android.util.Log
import androidx.core.app.ActivityCompat
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import java.io.File
import java.io.IOException
import java.util.*


class SensorProcessService : Service() {

    private var locationManager: LocationManager? = null

    private val _location = MutableStateFlow<Location?>(null)
    val location: StateFlow<Location?> = _location

    private val _time = MutableStateFlow<Date?>(null)
    val time: StateFlow<Date?> = _time

    lateinit var csvFile : File

    // binder given to clients
    private val binder = LocalBinder()

    // class used for the client binder
    inner class LocalBinder : Binder() {
        // Returns instance of SensorProcessService so clients can call public methods
        fun getService()= this@SensorProcessService
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
            ) != PackageManager.PERMISSION_GRANTED
        ) {

        }

        locationManager?.requestLocationUpdates(
            LocationManager.NETWORK_PROVIDER,
            0L,
            0f,
            locationListener
        )

        // Get current time
        _time.value = Calendar.getInstance().time
    }

    override fun onBind(intent: Intent): IBinder {
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

        // TODO Make check that we have a value - maybe we don't have to have value idk
        try {
            // Write to csv file
            csvFile.appendText(
                ID + "," +
                time.value.toString() + ","
                + location.value?.longitude.toString() + ","
                + location.value?.latitude.toString() + "\n"
            )
        } catch (e: IOException) {
            Log.e("Err", "Couldn't write to file", e)
        }
    }
}