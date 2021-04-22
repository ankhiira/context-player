package com.gabchmel.contextmusicplayer.theme

import androidx.compose.material.MaterialTheme
import androidx.compose.material.lightColors
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val LightColors = lightColors(
    primary = Blue700,
    primaryVariant = Red900,
    onPrimary = Color.White,
    secondary = Blue700,
    secondaryVariant = Red900,
    onSecondary = Color.White,
    error = Red800
)

@Composable
fun JetnewsTheme(content: @Composable () -> Unit) {
    MaterialTheme(colors = LightColors, content = content)
}
