package com.gabchmel.contextmusicplayer.theme

import androidx.compose.material.Typography
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import com.gabchmel.contextmusicplayer.R

val appFontFamily = FontFamily(
    Font(R.font.notosansjp_regular),
    Font(R.font.notosansjp_medium, FontWeight.W500),
    Font(R.font.notosansjp_bold, FontWeight.Bold)
)

private val defaultTypography = Typography()
val MyTypography = Typography(
    h1 = defaultTypography.h1.copy(fontFamily = appFontFamily),
    body1 = defaultTypography.body1.copy(fontFamily = appFontFamily),
//    h1 = TextStyle(
//        fontFamily = appFontFamily,
//        fontWeight = FontWeight.W300,
//        fontSize = 96.sp
//    ),
//    body1 = TextStyle(
//        fontFamily = appFontFamily,
//        fontWeight = FontWeight.W600,
//        fontSize = 16.sp
//    )
    /*...*/
)