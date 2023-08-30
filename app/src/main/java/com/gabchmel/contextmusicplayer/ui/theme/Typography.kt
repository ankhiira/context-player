package com.gabchmel.contextmusicplayer.ui.theme

import androidx.compose.material.Typography
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import com.gabchmel.contextmusicplayer.R

val appFontFamily = FontFamily(
    Font(R.font.bahnschrift_14),
    Font(R.font.bahnschrift_14, FontWeight.W500),
    Font(R.font.bahnschrift_14, FontWeight.Bold)
)

private val defaultTypography = Typography()

val MyTypography = Typography(
    h1 = defaultTypography.h1.copy(fontFamily = appFontFamily),
    body1 = defaultTypography.body1.copy(fontFamily = appFontFamily)
)