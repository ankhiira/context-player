package com.gabchmel.common.data

data class ConvertedData(
    var sinTime: Double = 0.0,
    var cosTime: Double = 0.0,
    var dayOfWeekSin: Double = 0.0,
    var dayOfWeekCos: Double = 0.0,
    var currentActivity: String = "NONE",
    var lightSensorValue: Float = 0.0f,
    var deviceLying: Float = 0.0f,
    var bluetoothDeviceConnected: Float = 0.0f,
    var headphonesPluggedIn: Float = 0.0f,
    var pressure: Float = 0.0f,
    var temperature: Float = 0.0f,
    var wifi: UInt = 0u,
    var connection: String = "NONE",
    var batteryStatus: String = "NONE",
    var chargingType: String = "NONE",
    var proximity: Float = 0.0f,
    var humidity: Float = 0.0f,
    var heartBeat: Float = 0.0f,
    var heartRate: Float = 0.0f,
    var locationCluster: Int = -1,
    var xCoord: Double = 0.0,
    var yCoord: Double = 0.0,
    var zCoord: Double = 0.0
)