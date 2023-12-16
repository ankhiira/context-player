package com.gabchmel.common.data

import kotlinx.serialization.Serializable

@Serializable
data class SensorDataPreferences(
    val userActivity: UserActivity = UserActivity.UNKNOWN,
    val isDeviceLying: Boolean? = null,
    val isBluetoothDeviceConnected: Boolean? = null,
    val areHeadphonesConnected: Boolean? = null,
    val connectedWifiSsid: Int = -1,
    val currentNetworkConnection: NetworkType = NetworkType.NONE,
    val batteryStatus: BatteryStatus? = null,
    val chargingMethod: ChargingMethod? = null
)