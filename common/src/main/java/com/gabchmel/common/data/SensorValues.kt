package com.gabchmel.common.data

import kotlinx.datetime.LocalDateTime
import kotlinx.serialization.Serializable

@Serializable
data class SensorValues(
    var currentTime: LocalDateTime? = null,
    var longitude: Double = 0.0,
    var latitude: Double = 0.0,
    var userActivity: UserActivity = UserActivity.UNKNOWN,
    var lightSensorValue: Float = 0.0f,
    var temperature: Float = 0.0f,
    var isDeviceLying: Boolean? = null,
    var proximity: Float = 0.0f,
    var isHeadphonesPluggedIn: Boolean? = null,
    var isBluetoothDeviceConnected: Boolean? = null,
    var wifiSsid: UInt? = null,
    var networkConnectionType: NetworkType = NetworkType.NONE,
    var isDeviceCharging: Boolean? = null,
    var chargingType: ChargingMethod? = null,
    var heartRate: Float = 0.0f
)