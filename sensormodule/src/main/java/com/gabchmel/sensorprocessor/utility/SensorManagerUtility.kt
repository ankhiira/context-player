package com.gabchmel.sensorprocessor.utility

import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.util.Log
import com.gabchmel.sensorprocessor.SensorProcessService


object SensorManagerUtility {

    // Utility function to get onSensorChanged listener for requested sensor
    fun sensorReader(
        sensorManager: SensorManager, sensorType: Int, sensorName: String,
        sensorProcessService: SensorProcessService
    ) {
        var isOrientSensor = false
        var sensorList: List<Sensor> = emptyList()
        val sensorValueList = mutableListOf<Float>()

        if (sensorType == Sensor.TYPE_ACCELEROMETER) {
            isOrientSensor = true
            sensorList = sensorManager.getSensorList(sensorType)
        }

        // Identify the sensor
        val sensor = sensorManager.getDefaultSensor(sensorType)

        val sensorEventListener: SensorEventListener = object : SensorEventListener {
            override fun onSensorChanged(sensorEvent: SensorEvent) {

                val values = sensorEvent.values

                when {
                    isOrientSensor -> {
                        sensorValueList.clear()
                        with(sensorValueList) {
                            add(values[0])
                            add(values[1])
                            add(values[2])
                        }
                        // TODO call the function for orientation here
                        sensorProcessService.coordList = sensorValueList
                    }
                    sensorType == Sensor.TYPE_LIGHT -> {
                        sensorProcessService._sensorData.value.lightSensorValue = values[0]
                    }
                    sensorType == Sensor.TYPE_PRESSURE -> {
                        sensorProcessService._sensorData.value.pressure = values[0]
                    }
                    sensorType == Sensor.TYPE_AMBIENT_TEMPERATURE -> {
                        sensorProcessService._sensorData.value.temperature = values[0]
                    }
                    sensorType == Sensor.TYPE_PROXIMITY -> {
                        sensorProcessService._sensorData.value.proximity = values[0]
                    }
                    sensorType == Sensor.TYPE_RELATIVE_HUMIDITY -> {
                        sensorProcessService._sensorData.value.humidity = values[0]
                    }
                    sensorType == Sensor.TYPE_HEART_BEAT -> {
                        sensorProcessService._sensorData.value.heartBeat = values[0]
                    }
                    sensorType == Sensor.TYPE_HEART_RATE -> {
                        sensorProcessService._sensorData.value.heartRate = values[0]
                    }
                }

                // Log the values of sensors
                if (isOrientSensor) {
                    Log.d("SensorValues", "$sensorName: ${values[0]}, ${values[1]}, ${values[2]}")
                } else {
                    Log.d("SensorValues", "$sensorName: ${values[0]}")
                }
            }

            override fun onAccuracyChanged(sensor: Sensor, i: Int) {}
        }

        // Check if we have sensor which values we want to collect
        if (sensorManager.getDefaultSensor(sensorType) != null) {
            // Register listener for sensor changes
            sensorManager.registerListener(
                sensorEventListener,
                if (isOrientSensor) sensorList[0] else sensor,
                SensorManager.SENSOR_DELAY_NORMAL
            )
        }
    }
}