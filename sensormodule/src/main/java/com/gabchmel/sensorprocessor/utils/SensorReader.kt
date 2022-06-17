package com.gabchmel.sensorprocessor.utils

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow


object SensorReader {

    fun registerSensorReader(context: Context, sensorType: Int): Flow<Collection<Float>> =
        callbackFlow {
            val sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as SensorManager

            val sensorEventListener = object : SensorEventListener {
                override fun onSensorChanged(sensorEvent: SensorEvent) {
                    val sensorEventValues = sensorEvent.values.toMutableList()
                    trySend(sensorEventValues)
                }

                override fun onAccuracyChanged(sensor: Sensor, i: Int) {}
            }

            registerSensorListener(sensorManager, sensorEventListener, sensorType)
            awaitClose { unregisterSensorListener(sensorManager, sensorEventListener, sensorType) }
        }

    private fun registerSensorListener(
        sensorManager: SensorManager,
        sensorEventListener: SensorEventListener,
        sensorType: Int
    ) {
        sensorManager.registerListener(
            sensorEventListener,
            sensorManager.getDefaultSensor(sensorType),
            SensorManager.SENSOR_DELAY_NORMAL
        )
    }

    private fun unregisterSensorListener(
        sensorManager: SensorManager,
        sensorEventListener: SensorEventListener,
        sensorType: Int
    ) {
        sensorManager.unregisterListener(
            sensorEventListener,
            sensorManager.getDefaultSensor(sensorType)
        )
    }
}