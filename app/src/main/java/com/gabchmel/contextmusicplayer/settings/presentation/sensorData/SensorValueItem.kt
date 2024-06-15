package com.gabchmel.contextmusicplayer.settings.presentation.sensorData

import androidx.compose.foundation.layout.Row
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable

@Composable
fun SensorRow(
    sensorName: String,
    sensorValue: Any?
) {
    Row {
        Text(
            text = "$sensorName: ",
            color = MaterialTheme.colorScheme.onPrimary,
            style = MaterialTheme.typography.bodyMedium
        )
        Text(
            text = when(sensorValue) {
                is Boolean -> if (sensorValue) "Yes" else "No"
                null -> "UNKNOWN"
                else -> sensorValue.toString()
            },
            style = MaterialTheme.typography.bodyMedium
        )
    }
}