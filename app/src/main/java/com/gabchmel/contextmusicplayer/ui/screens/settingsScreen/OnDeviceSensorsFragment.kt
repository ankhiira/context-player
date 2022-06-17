package com.gabchmel.contextmusicplayer.ui.screens.settingsScreen

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
import com.gabchmel.contextmusicplayer.ui.theme.JetnewsTheme
import com.gabchmel.contextmusicplayer.ui.theme.appFontFamily
import com.gabchmel.contextmusicplayer.ui.theme.spacing
import com.gabchmel.sensorprocessor.utils.OnDeviceSensors

class OnDeviceSensorsFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return ComposeView(requireContext()).apply {
            setContent {
                JetnewsTheme {
                    val sensorReader = OnDeviceSensors(context)
                    val materialColorPrimary = MaterialTheme.colors.onPrimary
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
                                            imageVector =
                                            ImageVector.vectorResource(R.drawable.ic_back),
                                            contentDescription = "Back",
                                            modifier = Modifier.fillMaxHeight(0.4f),
                                            tint = materialColorPrimary
                                        )
                                    }
                                },
                                elevation = 0.dp,
                                backgroundColor = Color.Transparent
                            )
                        },
                        content = {
                            LazyColumn(
                                verticalArrangement =
                                Arrangement.spacedBy(MaterialTheme.spacing.medium),
                                contentPadding =
                                PaddingValues(horizontal = MaterialTheme.spacing.large),
                                modifier = Modifier.padding(vertical = MaterialTheme.spacing.medium)
                            ) {
                                items(sensorReader.onDeviceSensors) { sensor ->
                                    Text(
                                        text = sensor.name,
                                        color = materialColorPrimary
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