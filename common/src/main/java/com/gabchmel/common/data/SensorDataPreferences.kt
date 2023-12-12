package com.gabchmel.common.data

import kotlinx.serialization.Serializable

@Serializable
data class SensorDataPreferences(
    val state: String = "",
    val isDeviceLying: Float = -1.0f,
    val isBluetoothDeviceConnected: Float = -1.0f,
    val areHeadphonesConnected: Float = -1.0f,
    val connectedWifiSsid: Int = -1,
    val currentNetworkConnection: String = "UNDEFINED",
    val batteryState: String = "UNDEFINED",
    val chargingMethod: String = "UNDEFINED"
)