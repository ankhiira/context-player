package com.gabchmel.common.data

//TODO boolean nullable - muze byt nezjistene
data class ConvertedData(
    var sinTime: Double = 0.0,
    var cosTime: Double = 0.0,
    var dayOfWeekSin: Double = 0.0,
    var dayOfWeekCos: Double = 0.0,
    var currentActivity: UserActivity = UserActivity.UNKNOWN,
    var lightSensorValue: Float = 0.0f,
    var isDeviceLying: Int = 0,
    var bluetoothDeviceConnected: Int = 0,
    var headphonesPluggedIn: Int = 0,
    var temperature: Float = 0.0f,
    var wifi: UInt = 0u,
    var connection: NetworkType = NetworkType.NONE,
    var isDeviceCharging: Int = 0,
    var chargingType: ChargingMethod? = null,
    var proximity: Float = 0.0f,
    var heartRate: Float = 0.0f,
    var locationCluster: Int = -1,
    var xCoord: Double = 0.0,
    var yCoord: Double = 0.0,
    var zCoord: Double = 0.0
)