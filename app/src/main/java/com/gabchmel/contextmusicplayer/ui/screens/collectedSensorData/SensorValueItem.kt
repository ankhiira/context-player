package com.gabchmel.contextmusicplayer.ui.screens.collectedSensorData

import androidx.compose.foundation.layout.Row
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable

@Composable
fun SensorRow(
    sensorName: String,
    sensorValue: Any
) {
    Row {
        Text(
            text = "$sensorName: ",
            color = MaterialTheme.colorScheme.onPrimary,
        )
        Text(
            text = "$sensorValue",
        )
    }
}