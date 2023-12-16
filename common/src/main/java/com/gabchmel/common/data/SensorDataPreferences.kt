package com.gabchmel.common.data

import kotlinx.serialization.Serializable

@Serializable
data class SensorDataPreferences(
    val userActivity: UserActivity = UserActivity.UNKNOWN,
    val isDeviceLying: Boolean? = null,
    val isBluetoothDeviceConnected: Boolean? = null,
    val areHeadphonesConnected: Boolean? = null,
    val connectedWifiSsid: Int? = null,
    val currentNetworkConnection: NetworkType = NetworkType.NONE,
    val isDeviceCharging: Boolean? = null,
    val chargingMethod: ChargingMethod? = null
)