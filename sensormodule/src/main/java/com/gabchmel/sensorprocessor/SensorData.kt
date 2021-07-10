package com.gabchmel.sensorprocessor

import java.util.*

data class SensorData(
    val currentTime: Date?,
    val longitude: Double?,
    val latitude: Double?,
    val currentState: String?,
    val lightSensorValue: Float?,
    val deviceLying: Float?,
    val BTdeviceConnected: Float?,
    val headphonesPluggedIn: Float?
)