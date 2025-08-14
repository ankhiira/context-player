package com.gabchmel.sensorprocessor.utils

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorManager

class OnDeviceSensors(context: Context) {

    private val sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as SensorManager
    val onDeviceSensors: List<Sensor> = sensorManager.getSensorList(Sensor.TYPE_ALL)
}
