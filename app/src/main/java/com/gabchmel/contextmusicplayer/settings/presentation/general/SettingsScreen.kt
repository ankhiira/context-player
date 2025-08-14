package com.gabchmel.contextmusicplayer.settings.presentation.general

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Analytics
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Sensors
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.gabchmel.common.utils.convertedArffFileName
import com.gabchmel.common.utils.dataCsvFileName
import com.gabchmel.contextmusicplayer.R
import com.gabchmel.contextmusicplayer.core.presentation.components.NavigationTopAppBar
import com.gabchmel.contextmusicplayer.settings.domain.convertFileForSend
import com.gabchmel.contextmusicplayer.songPrediction.domain.PredictionCreator
import java.io.File

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    navigateToOnDeviceSensors: () -> Unit,
    navigateToCollectedSensorData: () -> Unit,
    popBackStack: () -> Unit
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val uriHandler = LocalUriHandler.current

    Scaffold(
        topBar = {
            NavigationTopAppBar(
                title = stringResource(id = R.string.settings_title),
                onNavigateBack = {
                    popBackStack()
                }
            )
        },
        content = { padding ->
            Column(
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.padding(padding)
            ) {
                SettingsItem(
                    iconVector = Icons.Filled.Sensors,
                    textRes = R.string.settings_item_sensors,
                    onClick = navigateToOnDeviceSensors
                )
                SettingsItem(
                    iconVector = Icons.Filled.Analytics,
                    textRes = R.string.settings_item_data,
                    onClick = navigateToCollectedSensorData
                )
                SettingsItem(
                    iconVector = Icons.Filled.Description,
                    textRes = R.string.settings_item_privacy_policy,
                    onClick = {
                        uriHandler.openUri("https://github.com/ankhiira/context-player/blob/dev/privacyPolicy/Privacy%20Policy.txt")
                    }
                )
                Divider()
                Column(
                    modifier = Modifier.padding(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    SettingsButtonItem(
                        title = stringResource(R.string.settings_item_title_recreate_model),
                        description = stringResource(R.string.settings_button_item_recreate_model),
                        buttonText = stringResource(R.string.settings_item_button_recreate_model),
                        onClick = {
                            //TODO create via Media3
                            PredictionCreator(
                                lifecycleOwner,
                                context
                            )
                        }
                    )

                    val openDialog = remember { mutableStateOf(false) }

                    SettingsButtonItem(
                        title = stringResource(R.string.settings_item_title_delete_all_saved_data),
                        description = stringResource(R.string.settings_button_item_delete_data),
                        buttonText = stringResource(R.string.settings_item_button_delete_data),
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
                                        val inputFile = File(context.filesDir, dataCsvFileName)
                                        if (inputFile.exists()) {
                                            context.deleteFile(dataCsvFileName)
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
                        title = stringResource(R.string.settings_item_title_send_collected_data),
                        description = stringResource(R.string.settings_button_item_send_data),
                        buttonText = stringResource(R.string.settings_item_button_send_data),
                        onClick = {
                            sendEmail(context)
                        }
                    )
                }
            }
        }
    )
}

private fun sendEmail(context: Context) {
    val locationNewFile = File(context.filesDir, "convertedData.csv")
    val origFileArff = File(context.filesDir, convertedArffFileName)
    val origFilePrediction = File(context.filesDir, "predictions.csv")
    val origFileData = File(context.filesDir, dataCsvFileName)

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