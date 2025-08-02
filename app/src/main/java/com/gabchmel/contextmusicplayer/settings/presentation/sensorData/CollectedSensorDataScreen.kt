package com.gabchmel.contextmusicplayer.settings.presentation.sensorData

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.gabchmel.contextmusicplayer.R
import com.gabchmel.contextmusicplayer.core.presentation.components.NavigationTopAppBar

@Composable
fun CollectedSensorDataScreen(
    viewModel: CollectedSensorDataViewModel,
    popBackStack: () -> Unit = {}
) {
    val sensorData by viewModel.sensorData.collectAsStateWithLifecycle()
    val context = LocalContext.current

    LaunchedEffect(key1 = Unit) {
        viewModel.getSensorData(context)
    }

    Scaffold(
        topBar = {
            NavigationTopAppBar(
                title = stringResource(id = R.string.settings_item_data),
                onNavigateBack = {
                    popBackStack()
                }
            )
        },
        content = { padding ->
            Column(
                modifier = Modifier
                    .padding(padding)
                    .padding(horizontal = 16.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                CategoryTitle(
                    text = stringResource(id = R.string.collected_data_title_result_category)
                )
                //TODO get result category
//                SensorRow(
//                    "Result category",
//                    sensorData.
//                )
                CategoryTitle(
                    text = stringResource(id = R.string.collected_data_title_activity)
                )
                SensorRow(
                    "Current Activity",
                    sensorData.userActivity
                )
                CategoryTitle(
                    text = stringResource(id = R.string.collected_data_title_physical_values)
                )
                SensorRow(
                    "Ambient light",
                    sensorData.lightSensorValue
                )
                SensorRow(
                    "Temperature",
                    sensorData.temperature
                )
                CategoryTitle(
                    text = stringResource(id = R.string.collected_data_title_device_position)
                )
                SensorRow(
                    "Is device lying",
                    sensorData.isDeviceLying
                )
                SensorRow(
                    "Proximity",
                    sensorData.proximity
                )
                CategoryTitle(
                    text = stringResource(id = R.string.collected_data_title_conected_devices)
                )
                SensorRow(
                    "Cable headphones connected",
                    sensorData.isHeadphonesPluggedIn
                )
                SensorRow(
                    "Bluetooth headphones connected",
                    sensorData.isBluetoothDeviceConnected
                )
                CategoryTitle(
                    text = stringResource(id = R.string.collected_data_title_network)
                )
                SensorRow(
                    "Hashed WiFi name",
                    sensorData.wifiSsid
                )
                SensorRow(
                    "Connection type",
                    sensorData.networkConnectionType
                )
                CategoryTitle(
                    text = stringResource(id = R.string.collected_data_title_battery)
                )
                SensorRow(
                    "Is device charging",
                    sensorData.isDeviceCharging
                )
                SensorRow(
                    "Type of charger",
                    sensorData.chargingType.toString()
                )
                CategoryTitle(
                    text = stringResource(id = R.string.collected_data_title_health)
                )
                SensorRow(
                    "Heart Rate",
                    sensorData.heartRate
                )
            }
        }
    )
}