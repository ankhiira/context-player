package com.gabchmel.contextmusicplayer.settingsScreen

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.gabchmel.contextmusicplayer.R
import com.gabchmel.contextmusicplayer.theme.JetnewsTheme
import com.gabchmel.sensorprocessor.SensorLister

class OnDeviceSensorsFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return ComposeView(requireContext()).apply {
            setContent {
                JetnewsTheme {
                    val sensorReader = SensorLister(context)
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
                                        color = materialYel400,
                                        fontSize = 18.sp,
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
                                })
                        },
                        content = {
                            LazyColumn (
                                verticalArrangement = Arrangement.spacedBy(8.dp),
                                contentPadding = PaddingValues(horizontal = 16.dp)
                                    ) {
                                items(sensorReader.deviceSensors) { sensor ->
                                    Text(
                                        text = sensor.name,
                                        color = materialYel400,
                                        fontSize = 18.sp
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