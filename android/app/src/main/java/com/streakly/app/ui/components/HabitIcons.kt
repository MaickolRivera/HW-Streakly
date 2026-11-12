package com.streakly.app.ui.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.DirectionsRun
import androidx.compose.material.icons.automirrored.outlined.MenuBook
import androidx.compose.material.icons.outlined.AutoAwesome
import androidx.compose.material.icons.outlined.Bedtime
import androidx.compose.material.icons.outlined.ChatBubbleOutline
import androidx.compose.material.icons.outlined.Code
import androidx.compose.material.icons.outlined.FitnessCenter
import androidx.compose.material.icons.outlined.MusicNote
import androidx.compose.material.icons.outlined.Restaurant
import androidx.compose.material.icons.outlined.SelfImprovement
import androidx.compose.material.icons.outlined.Spa
import androidx.compose.material.icons.outlined.WaterDrop
import androidx.compose.material.icons.outlined.WbSunny
import androidx.compose.ui.graphics.vector.ImageVector

/** Iconos disponibles para los habitos. La clave es lo que se guarda en el backend. */
object HabitIcons {
    val all: List<Pair<String, ImageVector>> = listOf(
        "leaf" to Icons.Outlined.Spa,
        "dumbbell" to Icons.Outlined.FitnessCenter,
        "book" to Icons.AutoMirrored.Outlined.MenuBook,
        "drop" to Icons.Outlined.WaterDrop,
        "sun" to Icons.Outlined.WbSunny,
        "chat" to Icons.Outlined.ChatBubbleOutline,
        "code" to Icons.Outlined.Code,
        "meditate" to Icons.Outlined.SelfImprovement,
        "food" to Icons.Outlined.Restaurant,
        "run" to Icons.AutoMirrored.Outlined.DirectionsRun,
        "sleep" to Icons.Outlined.Bedtime,
        "music" to Icons.Outlined.MusicNote,
        "spark" to Icons.Outlined.AutoAwesome,
    )

    private val byKey = all.toMap()

    fun of(key: String?): ImageVector = byKey[key] ?: Icons.Outlined.AutoAwesome
}
