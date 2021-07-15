package com.gabchmel.common

data class ConvertedData (
    var sinTime : Double,
    var cosTime : Double,
    var dayOfWeekSin : Double,
    var dayOfWeekCos : Double,
    var state : String?,
    var lightSensorValue: Float? = 0.0f,
    var deviceLying: Float? = 0.0f,
    var BTdeviceConnected: Float? = 0.0f,
    var headphonesPluggedIn: Float? = 0.0f,
    var pressure: Float? = 0.0f,
    var temperature: Float? = 0.0f,
    var wifi: UInt? = 0u,
    var connection: String? = "NONE",
    var batteryStatus: String? = "NONE",
    var chargingType: String = "NONE",
    var proximity: Float? = 0.0f,
    var humidity: Float? = 0.0f,
    var heartBeat: Float? = 0.0f,
    var heartRate: Float? = 0.0f
        )