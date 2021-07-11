package com.gabchmel.contextmusicplayer.settingsScreen

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.fragment.app.Fragment
import androidx.lifecycle.ProcessLifecycleOwner
import androidx.navigation.fragment.findNavController
import com.gabchmel.contextmusicplayer.MediaBrowserConnector
import com.gabchmel.contextmusicplayer.R
import com.gabchmel.contextmusicplayer.theme.JetnewsTheme
import com.gabchmel.sensorprocessor.SensorLister

@Deprecated ("Not used, already in settings screen")
class PredictionModelSettingsFragment : Fragment() {
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
                            Column(
                                verticalArrangement = Arrangement.spacedBy(16.dp),
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(16.dp)
                            ) {
                                Column(
                                    modifier = Modifier
                                        .clickable(onClick = {
                                            MediaBrowserConnector(
                                                ProcessLifecycleOwner.get(),
                                                requireContext()
                                            )
                                        })
                                ) {
                                    Text(
                                        text = "Recreate model",
                                        fontWeight = FontWeight.Bold,
                                        color = materialYel400,
                                        fontSize = 18.sp
                                    )
                                    Text(
                                        text = "Clicking on this causes recreation of the model and triggers new song prediction",
                                        color = materialYel400
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