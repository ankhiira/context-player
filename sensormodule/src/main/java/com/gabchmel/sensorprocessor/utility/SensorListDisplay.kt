package com.gabchmel.sensorprocessor.utility;

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorManager;

class SensorListDisplay(context: Context) {

    // This functions serve to retrieve the list of sensors
    var sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as SensorManager
    val deviceSensors: List<Sensor> = sensorManager.getSensorList(Sensor.TYPE_ALL)
}
