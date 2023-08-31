package com.gabchmel.contextmusicplayer.ui.screens.collectedSensorData

import androidx.compose.foundation.layout.Row
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable

@Composable
fun SensorRow(
    sensorName: String,
    sensorValue: Any
) {
    Row {
        Text(
            text = "$sensorName: ",
            color = MaterialTheme.colors.onPrimary,
        )
        Text(
            text = "$sensorValue",
        )
    }
}