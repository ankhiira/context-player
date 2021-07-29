package com.gabchmel.contextmusicplayer.theme

import androidx.compose.material.MaterialTheme
import androidx.compose.material.darkColors
import androidx.compose.material.lightColors
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkColors = darkColors(
    primary = Pink500,
    secondary = Grey400,
    // On objets - text color
    onBackground = Yel900,
    onPrimary = Yel900,
    onSecondary = Yel900
)

@Composable
fun JetnewsTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colors = DarkColors,
        content = content,
        typography = MyTypography
    )
}
