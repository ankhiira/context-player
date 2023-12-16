package com.gabchmel.common.data

import kotlinx.datetime.LocalDateTime
import kotlinx.serialization.Serializable

@Serializable
data class SensorValues(
    var currentTime: LocalDateTime? = null,
    var longitude: Double = 0.0,
    var latitude: Double = 0.0,
    var isDeviceLying: Boolean? = null,
    var lightSensorValue: Float = 0.0f,
    var pressure: Float = 0.0f,
    var temperature: Float = 0.0f,
    var isBluetoothDeviceConnected: Boolean? = null,
    var isHeadphonesPluggedIn: Boolean? = null,
    var wifiSsid: UInt? = null,
    var userActivity: UserActivity = UserActivity.UNKNOWN,
    var networkConnectionType: NetworkType = NetworkType.NONE,
    var isDeviceCharging: Boolean? = null,
    var chargingType: ChargingMethod? = null,
    var proximity: Float = 0.0f,
    var humidity: Float = 0.0f,
    var heartBeat: Float = 0.0f,
    var heartRate: Float = 0.0f
)