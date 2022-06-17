package com.gabchmel.sensorprocessor.data.model

data class ProcessedCsvValues(
    val classNames: ArrayList<String> = arrayListOf(),
    val wifiNames: ArrayList<UInt> = arrayListOf()
)