package com.gabchmel.contextmusicplayer.ui.screens.collectedSensorData

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Scaffold
import androidx.compose.material.Text
import androidx.compose.material.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.gabchmel.common.data.ConvertedData
import com.gabchmel.contextmusicplayer.R
import com.gabchmel.contextmusicplayer.ui.theme.appFontFamily
import com.gabchmel.sensorprocessor.data.service.SensorDataProcessingService


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
                        "Collected sensor values",
                        fontFamily = appFontFamily
                    )
                },
                navigationIcon = {
                    IconButton(
                        onClick = {
                            navController.popBackStack()
                        }
                    ) {
                        Icon(
                            imageVector = ImageVector.vectorResource(R.drawable.ic_back),
                            contentDescription = "Back",
                            modifier = Modifier.fillMaxHeight(0.4f),
                            tint = MaterialTheme.colors.onPrimary
                        )
                    }
                },
                elevation = 0.dp,
                backgroundColor = Color.Transparent
            )
        },
        content = { padding ->
            Column(
                modifier = Modifier
                    .padding(padding)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                sensorDataProcessingService?.let { sensorProcessService ->
                    val sensorData by
                    sensorProcessService.measuredSensorValues.collectAsState(null)
                }

                SensorRow(
                    "Current Activity",
                    collectedSensorData.state
                )
                SensorRow(
                    "Ambient light",
                    collectedSensorData.lightSensorValue
                )
                SensorRow(
                    "Is device lying",
                    if (collectedSensorData.deviceLying == 0.0f) "No" else "Yes"
                )
                SensorRow(
                    "Are cable headphones connected",
                    if (collectedSensorData.headphonesPluggedIn == 0.0f) "No" else "Yes"
                )
                SensorRow(
                    "Are bluetooth headphones connected",
                    if (collectedSensorData.BTDeviceConnected == 0.0f) "No" else "Yes"
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
                    "Hashed WiFi name",
                    collectedSensorData.wifi
                )
                SensorRow(
                    "Connection type",
                    collectedSensorData.connection
                )
                SensorRow(
                    "Battery status",
                    collectedSensorData.batteryStatus
                )
                SensorRow(
                    "Type of charger",
                    collectedSensorData.chargingType
                )
                SensorRow(
                    "Proximity",
                    collectedSensorData.proximity
                )
                SensorRow(
                    "Humidity",
                    collectedSensorData.humidity
                )
                SensorRow(
                    "Heart Beat",
                    collectedSensorData.heartBeat
                )
                SensorRow(
                    "Heart Rate",
                    collectedSensorData.heartRate
                )
                SensorRow(
                    "Location cluster",
                    collectedSensorData.locationCluster
                )
            }
        }
    )
}