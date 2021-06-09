package com.gabchmel.sensorprocessor;

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorManager;

class SensorReader(context: Context) {

    var sensorManager= context.getSystemService(Context.SENSOR_SERVICE) as SensorManager

    val deviceSensors: List<Sensor> = sensorManager.getSensorList(Sensor.TYPE_ALL)
}
