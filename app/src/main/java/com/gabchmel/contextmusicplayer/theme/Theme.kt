package com.gabchmel.contextmusicplayer.theme

import androidx.compose.material.MaterialTheme
import androidx.compose.material.darkColors
import androidx.compose.runtime.Composable

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
