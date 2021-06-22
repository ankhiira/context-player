package com.gabchmel.sensorprocessor

import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager

object SensorManagerUtility {

    // Utility function to get onSensorChanged listener for requested sensor
    fun sensorReader(sensorManager: SensorManager, sensorType: Int, sensorName: String): Any {

        var isOrientSensor = false
        var sensorList : List<Sensor> = emptyList()

        var sensorValue = 0.0f
        val sensorValueList = mutableListOf<Float>()

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
                    sensorValueList.add(values[0])
                    sensorValueList.add(values[1])
                    sensorValueList.add(values[2])
                } else {
                    sensorValue = values[0]
                }

                // Log the values of sensors
//                if (isOrientSensor) {
//                    Log.d("SensorValues", "$sensorName: ${values[0]}, ${values[1]}, ${values[2]}")
//                } else {
//                    Log.d("SensorValues", "$sensorName: ${values[0]}")
//                }
            }

            override fun onAccuracyChanged(sensor: Sensor, i: Int) {}
        }

        sensorManager.registerListener(
            sensorEventListener,
            if (isOrientSensor) sensorList[0] else sensor,
            SensorManager.SENSOR_DELAY_NORMAL
        )

        return if (isOrientSensor) {
            sensorValueList
        } else {
            sensorValue
        }
    }
}