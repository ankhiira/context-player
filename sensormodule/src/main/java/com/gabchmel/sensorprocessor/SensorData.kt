package com.gabchmel.sensorprocessor

import java.util.*

data class SensorData(
    var currentTime: Date? = null,
    var longitude: Double? = 0.0,
    var latitude: Double?= 0.0,
    var currentState: String? = "NONE",
    var lightSensorValue: Float? = 0.0f,
    var deviceLying: Float? = 0.0f,
    var BTdeviceConnected: Float? = 0.0f,
    var headphonesPluggedIn: Float? = 0.0f,
    var pressure: Float? = 0.0f,
    var temperature: Float? = 0.0f,
    var wifi: UInt? = 0u,
    var connection: String? = "NONE",
    var batteryStatus: String? = "NONE",
    var chargingType: String = "NONE"
) {
    fun getData() = listOf(
        currentTime, longitude, latitude, currentState, lightSensorValue,
        deviceLying, BTdeviceConnected, headphonesPluggedIn, pressure, temperature, wifi, connection
    )
}