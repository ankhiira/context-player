package com.gabchmel.sensorprocessor

import java.util.*

data class SensorData(
    var currentTime: Date?,
    var longitude: Double?,
    var latitude: Double?,
    var currentState: String?,
    var lightSensorValue: Float?,
    var deviceLying: Float?,
    var BTdeviceConnected: Float?,
    var headphonesPluggedIn: Float?,
    var pressure: Float?,
    var temperature: Float?
)