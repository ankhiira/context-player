package com.gabchmel.sensorprocessor

import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.util.Log

object SensorManagerUtility {

    // Utility function to get onSensorChanged listener for requested sensor
    fun sensorReader(sensorManager: SensorManager, sensorType: Int, sensorName: String) {

        var isOrientSensor = false
        var sensorList : List<Sensor> = emptyList()

        if(sensorType == Sensor.TYPE_ORIENTATION) {
            isOrientSensor = true
        }

        if (isOrientSensor) {
            sensorList = sensorManager.getSensorList(sensorType)
        }

        // Identify the sensor
        val sensor = sensorManager.getDefaultSensor(sensorType)

        val sensorEventListener: SensorEventListener = object : SensorEventListener {
            override fun onSensorChanged(sensorEvent: SensorEvent) {
                val values = sensorEvent.values

                // TODO save values to structure maybe
                if(isOrientSensor) {
//                orientSensorAzimuthZAxis = event.values[0]
//                orientSensorPitchXAxis = event.values[1]
//                orientSensorRollYAxis = event.values[2]
                }

                // Log the values of sensors
                if (isOrientSensor) {
                    Log.d(sensorName, "$sensorName: ${values[0]}, ${values[1]}, ${values[2]}")
                } else {
                    Log.d(sensorName, "$sensorName: ${values[0]}")
                }
            }

            override fun onAccuracyChanged(sensor: Sensor, i: Int) {}
        }

        sensorManager.registerListener(
            sensorEventListener,
            if (isOrientSensor) sensorList[0] else sensor,
            SensorManager.SENSOR_DELAY_NORMAL
        )
    }
}