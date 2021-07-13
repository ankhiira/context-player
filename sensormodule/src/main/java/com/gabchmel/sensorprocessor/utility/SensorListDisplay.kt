package com.gabchmel.sensorprocessor.utility;

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorManager;

class SensorListDisplay(context: Context) {

    var sensorManager= context.getSystemService(Context.SENSOR_SERVICE) as SensorManager

    val deviceSensors: List<Sensor> = sensorManager.getSensorList(Sensor.TYPE_ALL)
}
