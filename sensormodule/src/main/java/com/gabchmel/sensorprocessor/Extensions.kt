package com.gabchmel.sensorprocessor

import kotlin.reflect.full.memberProperties

fun SensorData.toUserViewReflection() : Int = with(::SensorData) {
    return SensorData::class.memberProperties.size
}