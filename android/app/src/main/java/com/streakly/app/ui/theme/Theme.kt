package com.streakly.app.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp

private val ColorScheme = darkColorScheme(
    primary = StreaklyColors.Green,
    onPrimary = StreaklyColors.OnGreen,
    secondary = StreaklyColors.Amber,
    onSecondary = StreaklyColors.OnGreen,
    background = StreaklyColors.Background,
    onBackground = StreaklyColors.TextPrimary,
    surface = StreaklyColors.Surface,
    onSurface = StreaklyColors.TextPrimary,
    surfaceVariant = StreaklyColors.SurfaceHigh,
    onSurfaceVariant = StreaklyColors.TextSecondary,
    surfaceContainer = StreaklyColors.Surface,
    surfaceContainerHigh = StreaklyColors.SurfaceHigh,
    surfaceContainerHighest = StreaklyColors.SurfaceHigh,
    outline = StreaklyColors.Border,
    outlineVariant = StreaklyColors.Border,
    error = StreaklyColors.Danger,
)

private val StreaklyShapes = Shapes(
    small = RoundedCornerShape(10.dp),
    medium = RoundedCornerShape(14.dp),
    large = RoundedCornerShape(18.dp),
)

@Composable
fun StreaklyTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = ColorScheme,
        typography = StreaklyTypography,
        shapes = StreaklyShapes,
        content = content,
    )
}
