package com.gabchmel.common.data

data class SensorDataPreferences(
    val state: String,
    val isDeviceLying: Float,
    val isBluetoothDeviceConnected: Float,
    val areHeadphonesConnected: Float,
    val connectedWifiSsid: Int,
    val currentNetworkConnection: String,
    val batteryState: String,
    val chargingMethod: String
    )
