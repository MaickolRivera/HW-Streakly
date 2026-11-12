package com.streakly.app.ui.theme

import androidx.compose.ui.graphics.Color

// Paleta tomada de los mockups (tema oscuro unico).
object StreaklyColors {
    val Background = Color(0xFF0E1311)
    val Surface = Color(0xFF151B18)
    val SurfaceHigh = Color(0xFF1B231F)
    val Border = Color(0xFF232C27)

    val Green = Color(0xFF4ADE80)
    val GreenDark = Color(0xFF16A34A)
    val GreenTint = Color(0xFF14261B)
    val GreenBorder = Color(0xFF235A36)
    val OnGreen = Color(0xFF07130B)

    val Amber = Color(0xFFF5B754)
    val AmberTint = Color(0xFF2A2414)

    val TextPrimary = Color(0xFFE9EEEB)
    val TextSecondary = Color(0xFF9AA59F)
    val TextMuted = Color(0xFF65706A)

    val Danger = Color(0xFFF87171)

    // Intensidades del heatmap (0 = sin registro, 4 = superado)
    val Heat = listOf(
        Color(0xFF1C2420),
        Color(0xFF1E4D2F),
        Color(0xFF26803F),
        Color(0xFF3BBF63),
        Color(0xFF4ADE80),
    )
}
