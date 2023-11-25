package com.gabchmel.contextmusicplayer.ui.screens.collectedSensorData

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.gabchmel.common.data.ConvertedData
import com.gabchmel.contextmusicplayer.R
import com.gabchmel.contextmusicplayer.ui.theme.bahnSchrift
import com.gabchmel.sensorprocessor.data.service.SensorDataProcessingService


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CollectedSensorDataScreen(
    navController: NavHostController,
    collectedSensorData: ConvertedData
) {
    val sensorDataProcessingService: SensorDataProcessingService? by remember { mutableStateOf(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = stringResource(id = R.string.settings_item_data),
                        fontFamily = bahnSchrift
                    )
                },
                navigationIcon = {
                    IconButton(
                        onClick = {
                            navController.popBackStack()
                        }
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.ArrowBack,
                            contentDescription = "Navigate Back",
                            modifier = Modifier.size(24.dp),
                            tint = MaterialTheme.colorScheme.onPrimary
                        )
                    }
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
                sensorDataProcessingService?.let { sensorProcessService ->
                    val sensorData by
                    sensorProcessService.measuredSensorValues.collectAsState(null)
                }

                CategoryTitle(
                    text = stringResource(id = R.string.collected_data_title_result_category)
                )
                SensorRow(
                    "Result category",
                    collectedSensorData.locationCluster
                )

                CategoryTitle(
                    text = stringResource(id = R.string.collected_data_title_activity)
                )
                SensorRow(
                    "Current Activity",
                    collectedSensorData.state
                )
                CategoryTitle(
                    text = stringResource(id = R.string.collected_data_title_physical_values)
                )
                SensorRow(
                    "Ambient light",
                    collectedSensorData.lightSensorValue
                )
                SensorRow(
                    "Pressure",
                    collectedSensorData.pressure
                )
                SensorRow(
                    "Temperature",
                    collectedSensorData.temperature
                )
                SensorRow(
                    "Humidity",
                    collectedSensorData.humidity
                )
                CategoryTitle(
                    text = stringResource(id = R.string.collected_data_title_device_position)
                )
                SensorRow(
                    "Is device lying",
                    if (collectedSensorData.deviceLying == 0.0f) "No" else "Yes"
                )
                SensorRow(
                    "Proximity",
                    collectedSensorData.proximity
                )
                CategoryTitle(
                    text = stringResource(id = R.string.collected_data_title_conected_devices)
                )
                SensorRow(
                    "Cable headphones connected",
                    if (collectedSensorData.headphonesPluggedIn == 0.0f) "No" else "Yes"
                )
                SensorRow(
                    "Bluetooth headphones connected",
                    if (collectedSensorData.BTDeviceConnected == 0.0f) "No" else "Yes"
                )
                CategoryTitle(
                    text = stringResource(id = R.string.collected_data_title_network)
                )
                SensorRow(
                    "Hashed WiFi name",
                    collectedSensorData.wifi
                )
                SensorRow(
                    "Connection type",
                    collectedSensorData.connection
                )
                CategoryTitle(
                    text = stringResource(id = R.string.collected_data_title_battery)
                )
                SensorRow(
                    "Battery status",
                    collectedSensorData.batteryStatus
                )
                SensorRow(
                    "Type of charger",
                    collectedSensorData.chargingType
                )
                CategoryTitle(
                    text = stringResource(id = R.string.collected_data_title_health)
                )
                SensorRow(
                    "Heart Beat",
                    collectedSensorData.heartBeat
                )
                SensorRow(
                    "Heart Rate",
                    collectedSensorData.heartRate
                )
            }
        }
    )
}