package com.gabchmel.contextmusicplayer.ui.screens.settingsScreen

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Icon
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.NavigateNext
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun SettingsItem(
    iconVector: ImageVector,
    textRes: Int,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(16.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row {
            Icon(
                imageVector = iconVector,
                contentDescription = "Setting item icon",
            )
            Text(
                text = stringResource(id = textRes),
                color = MaterialTheme.colors.onPrimary,
                fontSize = 18.sp,
                modifier = Modifier.padding(horizontal = 8.dp)
            )
        }
        Icon(
            imageVector = Icons.Filled.NavigateNext,
            contentDescription = "NavigateToNext"
        )
    }
}