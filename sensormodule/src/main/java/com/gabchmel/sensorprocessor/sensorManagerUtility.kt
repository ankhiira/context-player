package com.gabchmel.sensorprocessor

import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.util.Log

object sensorManagerUtility {

    fun sensorReader(sensorManager: SensorManager, sensorType: Int, sensorName: String) {
        // Identify the sensor
        val sensor = sensorManager.getDefaultSensor(sensorType)

        val sensorEventListener: SensorEventListener = object : SensorEventListener {
            override fun onSensorChanged(sensorEvent: SensorEvent) {
                val values = sensorEvent.values
                Log.d(sensorName, "sensorName: ${values[0]}")
            }

            override fun onAccuracyChanged(sensor: Sensor, i: Int) {}
        }

        sensorManager.registerListener(
            sensorEventListener,
            sensor,
            SensorManager.SENSOR_DELAY_NORMAL
        )
    }
}