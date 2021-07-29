package com.gabchmel.contextmusicplayer.settingsScreen

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.unit.dp
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.gabchmel.contextmusicplayer.R
import com.gabchmel.contextmusicplayer.theme.JetnewsTheme
import com.gabchmel.contextmusicplayer.theme.appFontFamily
import com.gabchmel.sensorprocessor.utility.SensorListDisplay

class OnDeviceSensorsFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Compose view of the On-device sensors screen
        return ComposeView(requireContext()).apply {
            setContent {
                JetnewsTheme {
                    val sensorReader = SensorListDisplay(context)
                    val materialYel400 = MaterialTheme.colors.onPrimary
                    val scaffoldState =
                        rememberScaffoldState(rememberDrawerState(DrawerValue.Open))

                    Scaffold(
                        scaffoldState = scaffoldState,
                        modifier = Modifier
                            .fillMaxSize(),
                        topBar = {
                            TopAppBar(
                                title = {
                                    Text(
                                        "Device sensors",
                                        fontFamily = appFontFamily
                                    )
                                },
                                navigationIcon = {
                                    IconButton(onClick = { findNavController().navigateUp() }) {
                                        Icon(
                                            imageVector = ImageVector.vectorResource(R.drawable.ic_back),
                                            contentDescription = "Back",
                                            modifier = Modifier.fillMaxHeight(0.4f),
                                            tint = materialYel400
                                        )
                                    }
                                },
                                elevation = 0.dp,
                                backgroundColor = Color.Transparent
                            )
                        },
                        content = {
                            LazyColumn(
                                verticalArrangement = Arrangement.spacedBy(8.dp),
                                contentPadding = PaddingValues(horizontal = 16.dp),
                                modifier = Modifier.padding(vertical = 8.dp)
                            ) {
                                items(sensorReader.deviceSensors) { sensor ->
                                    Text(
                                        text = sensor.name,
                                        color = materialYel400,
                                    )
                                }
                            }
                        }
                    )
                }
            }
        }
    }
}