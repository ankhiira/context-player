package com.gabchmel.contextmusicplayer.settings.presentation.sensors

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.gabchmel.contextmusicplayer.R
import com.gabchmel.contextmusicplayer.core.presentation.components.NavigationTopAppBar
import com.gabchmel.sensorprocessor.utils.OnDeviceSensors


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OnDeviceSensorsScreen(
    popBackStack: () -> Unit = {}
) {
    val context = LocalContext.current
    val sensorReader = OnDeviceSensors(context)

    Scaffold(
        topBar = {
            NavigationTopAppBar(
                title = stringResource(id = R.string.settings_item_on_device_sensors),
                onNavigateBack = {
                    popBackStack()
                }
            )
        },
        content = { padding ->
            LazyColumn(
                verticalArrangement =
                Arrangement.spacedBy(8.dp),
                contentPadding = padding,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
            ) {
                items(sensorReader.onDeviceSensors) { sensor ->
                    Text(
                        text = sensor.name,
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                }
            }
        }
    )
}