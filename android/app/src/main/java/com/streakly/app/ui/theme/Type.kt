package com.streakly.app.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

/**
 * Fuente "display" para titulos (HOY, RACHAS, GOALS...).
 * Para usar una fuente pixel (p. ej. Silkscreen / Pixelify Sans, licencia OFL),
 * copia el .ttf en res/font/ y cambia esta linea por:
 *   FontFamily(Font(R.font.silkscreen_bold, FontWeight.Bold))
 */
val DisplayFont: FontFamily = FontFamily.Monospace
val BodyFont: FontFamily = FontFamily.SansSerif

val StreaklyTypography = Typography(
    displayLarge = TextStyle(fontFamily = DisplayFont, fontWeight = FontWeight.Black, fontSize = 34.sp, letterSpacing = 1.sp),
    headlineLarge = TextStyle(fontFamily = DisplayFont, fontWeight = FontWeight.Black, fontSize = 28.sp, letterSpacing = 1.sp),
    headlineSmall = TextStyle(fontFamily = DisplayFont, fontWeight = FontWeight.Bold, fontSize = 22.sp),
    titleLarge = TextStyle(fontFamily = DisplayFont, fontWeight = FontWeight.Bold, fontSize = 17.sp),
    titleMedium = TextStyle(fontFamily = BodyFont, fontWeight = FontWeight.SemiBold, fontSize = 16.sp),
    titleSmall = TextStyle(fontFamily = BodyFont, fontWeight = FontWeight.SemiBold, fontSize = 14.sp),
    bodyLarge = TextStyle(fontFamily = BodyFont, fontSize = 16.sp, lineHeight = 22.sp),
    bodyMedium = TextStyle(fontFamily = BodyFont, fontSize = 14.sp, lineHeight = 20.sp),
    bodySmall = TextStyle(fontFamily = BodyFont, fontSize = 12.sp, lineHeight = 16.sp),
    labelLarge = TextStyle(fontFamily = BodyFont, fontWeight = FontWeight.SemiBold, fontSize = 15.sp),
    labelMedium = TextStyle(fontFamily = BodyFont, fontWeight = FontWeight.Medium, fontSize = 12.sp, letterSpacing = 0.6.sp),
    labelSmall = TextStyle(fontFamily = BodyFont, fontWeight = FontWeight.Medium, fontSize = 11.sp),
)
