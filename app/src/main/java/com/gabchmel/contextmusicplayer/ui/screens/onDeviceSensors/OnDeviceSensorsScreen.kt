package com.gabchmel.contextmusicplayer.ui.screens.onDeviceSensors

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.gabchmel.contextmusicplayer.R
import com.gabchmel.contextmusicplayer.ui.theme.bahnSchrift
import com.gabchmel.sensorprocessor.utils.OnDeviceSensors


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OnDeviceSensorsScreen(
    navController: NavHostController
) {
    val context = LocalContext.current
    val sensorReader = OnDeviceSensors(context)

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Device sensors",
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
                            imageVector =
                            ImageVector.vectorResource(R.drawable.ic_back),
                            contentDescription = "Back",
                            modifier = Modifier.fillMaxHeight(0.4f),
                            tint = MaterialTheme.colorScheme.onPrimary
                        )
                    }
                }
            )
        },
        content = { padding ->
            LazyColumn(
                verticalArrangement =
                Arrangement.spacedBy(8.dp),
                contentPadding = padding,
                modifier = Modifier.padding(vertical = 8.dp)
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