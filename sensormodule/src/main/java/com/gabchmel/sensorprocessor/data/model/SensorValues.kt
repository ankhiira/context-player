package com.gabchmel.sensorprocessor.data.model

import com.gabchmel.common.data.BatteryStatus
import com.gabchmel.common.data.ChargingMethod
import com.gabchmel.common.data.NetworkType
import com.gabchmel.common.data.UserActivity
import java.util.Date

data class SensorValues(
    var currentTime: Date? = null,
    var longitude: Double = 0.0,
    var latitude: Double = 0.0,
    var isDeviceLying: Boolean? = null,
    var lightSensorValue: Float = 0.0f,
    var pressure: Float = 0.0f,
    var temperature: Float = 0.0f,
    var isBluetoothDeviceConnected: Boolean? = null,
    var isHeadphonesPluggedIn: Boolean? = null,
    var wifiSsid: UInt = 0u,
    var userActivity: UserActivity = UserActivity.UNKNOWN,
    var networkConnectionType: NetworkType = NetworkType.NONE,
    var batteryStatus: BatteryStatus? = null,
    var chargingType: ChargingMethod? = null,
    var proximity: Float = 0.0f,
    var humidity: Float = 0.0f,
    var heartBeat: Float = 0.0f,
    var heartRate: Float = 0.0f
)