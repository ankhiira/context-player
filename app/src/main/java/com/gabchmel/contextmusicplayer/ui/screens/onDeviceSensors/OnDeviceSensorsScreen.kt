package com.gabchmel.contextmusicplayer.ui.screens.onDeviceSensors

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Scaffold
import androidx.compose.material.Text
import androidx.compose.material.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.gabchmel.contextmusicplayer.R
import com.gabchmel.contextmusicplayer.ui.theme.appFontFamily
import com.gabchmel.contextmusicplayer.ui.theme.spacing
import com.gabchmel.sensorprocessor.utils.OnDeviceSensors


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
                            imageVector =
                            ImageVector.vectorResource(R.drawable.ic_back),
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
            LazyColumn(
                verticalArrangement =
                Arrangement.spacedBy(MaterialTheme.spacing.medium),
                contentPadding = padding,
                modifier = Modifier.padding(vertical = MaterialTheme.spacing.medium)
            ) {
                items(sensorReader.onDeviceSensors) { sensor ->
                    Text(
                        text = sensor.name,
                        color = MaterialTheme.colors.onPrimary
                    )
                }
            }
        }
    )
}