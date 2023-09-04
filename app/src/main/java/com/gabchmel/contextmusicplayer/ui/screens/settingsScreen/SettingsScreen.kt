package com.gabchmel.contextmusicplayer.ui.screens.settingsScreen

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DriveFileRenameOutline
import androidx.compose.material.icons.filled.Note
import androidx.compose.material.icons.filled.Sensors
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.gabchmel.contextmusicplayer.R
import com.gabchmel.contextmusicplayer.data.service.MediaBrowserConnector
import com.gabchmel.contextmusicplayer.utils.convertFileForSend
import java.io.File


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    navController: NavHostController
) {

    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = stringResource(id = R.string.settings_title)
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
                            modifier = Modifier.size(24.dp),
                            tint = MaterialTheme.colorScheme.onPrimary
                        )
                    }
                }
            )
        },
        content = { padding ->
            Column(
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
            ) {
                SettingsItem(
                    iconVector = Icons.Filled.Sensors,
                    textRes = R.string.settings_item_sensors,
                    onClick = {
                        navController.navigate("on_device_sensors")
                    }
                )
                SettingsItem(
                    iconVector = Icons.Filled.DriveFileRenameOutline,
                    textRes = R.string.settings_item_data,
                    onClick = {
                        navController.navigate("collected_sensor_data")
                    }
                )
                SettingsItem(
                    iconVector = Icons.Filled.Note,
                    textRes = R.string.settings_item_privacy_policy,
                    onClick = {
//                                                findNavController().navigate(
//                                                    SettingsFragmentDirections
//                                                        .actionSettingsFragmentToSensorValuesFragment()
//                                                )
                    }
                )
                val uriHandler = LocalUriHandler.current
                Text(
                    modifier = Modifier.clickable {
                        uriHandler.openUri("https://github.com/ankhiira/context-player/blob/dev/privacyPolicy/Privacy%20Policy.txt")
                    },
                    text = "privacy policy",
                    style = MaterialTheme.typography.bodyMedium
                )
                SettingsButtonItem(
                    textRes = R.string.settings_button_item_recreate_model,
                    buttonRes = R.string.settings_item_button_recreate_model,
                    onClick = {
                        MediaBrowserConnector(
                            lifecycleOwner,
                            context
                        )
                    }
                )

                val openDialog = remember { mutableStateOf(false) }

                SettingsButtonItem(
                    textRes = R.string.settings_button_item_delete_data,
                    buttonRes = R.string.settings_item_button_delete_data,
                    onClick = {
                        openDialog.value = true
                    }
                )

                if (openDialog.value) {
                    AlertDialog(
                        onDismissRequest = {
                            // Dismiss the dialog when the user clicks outside the dialog or on the back
                            // button. If you want to disable that functionality, simply use an empty
                            // onCloseRequest.
                            openDialog.value = false
                        },
                        title = {
                            Text(text = "Delete all saved data")
                        },
                        text = {
                            Text("Are you sure you want to delete all saved data?")
                        },
                        confirmButton = {
                            Button(
                                onClick = {
                                    openDialog.value = false
                                    val inputFile =
                                        File(
                                            context.filesDir,
                                            "data.csv"
                                        )
                                    if (inputFile.exists()) {
                                        context.deleteFile("data.csv")
                                    }
                                }) {
                                Text("YES")
                            }
                        },
                        dismissButton = {
                            Button(
                                onClick = {
                                    openDialog.value = false
                                }
                            ) {
                                Text("NO")
                            }
                        }
                    )
                }
                SettingsButtonItem(
                    textRes = R.string.settings_button_item_send_data,
                    buttonRes = R.string.settings_item_button_send_data,
                    onClick = {
                        sendEmail(context)
                    }
                )
            }
        }
    )
}

private fun sendEmail(context: Context) {
    val locationNewFile =
        File(context.filesDir, "convertedData.csv")
    val origFileArff =
        File(
            context.filesDir,
            "arffData_converted.arff"
        )
    val origFilePrediction =
        File(context.filesDir, "predictions.csv")
    val origFileData =
        File(context.filesDir, "data.csv")

    val data = ArrayList<Uri>()

    val pathResultData =
        convertFileForSend(
            context,
            "convertedData",
            ".csv",
            locationNewFile
        )
    val pathResultArff =
        convertFileForSend(
            context,
            "arffData_converted",
            ".arff",
            origFileArff
        )
    val pathResultPrediction =
        convertFileForSend(
            context,
            "predictions",
            ".csv",
            origFilePrediction
        )
    val pathResultDataOriginal =
        convertFileForSend(
            context,
            "data",
            ".csv",
            origFileData
        )
    data.add(pathResultData)
    data.add(pathResultArff)
    data.add(pathResultPrediction)
    data.add(pathResultDataOriginal)

    val emailIntent = Intent(Intent.ACTION_SEND_MULTIPLE).apply {
        // Email type
        type = "text/csv"
        // Recipient
        putExtra(Intent.EXTRA_EMAIL, arrayOf("chmelarova.gabik@gmail.com"))
        // Attachment
        putParcelableArrayListExtra(Intent.EXTRA_STREAM, data)
        // Subject
        putExtra(Intent.EXTRA_SUBJECT, R.string.email_data_from)
        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
    }

    context.startActivity(emailIntent)
}