package com.gabchmel.contextmusicplayer.settings.presentation.sensorData

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.size
import androidx.compose.material.Text
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun CategoryTitle(
    text: String
) {
    Spacer(modifier = Modifier.size(8.dp))
    Text(
        text = text,
        style = MaterialTheme.typography.titleMedium
    )
}