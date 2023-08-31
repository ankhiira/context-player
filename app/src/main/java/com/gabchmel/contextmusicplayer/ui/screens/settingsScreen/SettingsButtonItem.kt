package com.gabchmel.contextmusicplayer.ui.screens.settingsScreen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Button
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp

@Composable
fun SettingsButtonItem(
    textRes: Int,
    buttonRes: Int,
    onClick: () -> Unit
) {
    Row {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = stringResource(id = textRes),
                color = MaterialTheme.colors.onPrimary,
                textAlign = TextAlign.Justify
            )
            Button(
                onClick = onClick,
                shape = RoundedCornerShape(50)
            ) {
                Text(
                    text = stringResource(id = buttonRes),
                    color = MaterialTheme.colors.onPrimary
                )
            }
        }
    }
}