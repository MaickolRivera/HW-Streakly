package com.streakly.app.ui.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.Logout
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.streakly.app.ui.Spanish
import com.streakly.app.ui.appViewModel
import com.streakly.app.ui.components.LoadingBox
import com.streakly.app.ui.components.SectionLabel
import com.streakly.app.ui.components.StreaklyCard
import com.streakly.app.ui.theme.StreaklyColors
import java.time.OffsetDateTime
import java.time.format.DateTimeFormatter

@Composable
fun ProfileScreen(
    viewModel: ProfileViewModel = appViewModel { ProfileViewModel(it.authRepository, it.habitRepository) },
) {
    val state = viewModel.state
    var confirmLogout by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { viewModel.load() }

    if (state.loading) {
        LoadingBox()
        return
    }

    Column(
        Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp, vertical = 16.dp),
    ) {
        Text("Tu cuenta", style = MaterialTheme.typography.bodyMedium, color = StreaklyColors.TextSecondary)
        Spacer(Modifier.height(4.dp))
        Text("PERFIL", style = MaterialTheme.typography.headlineLarge)
        Spacer(Modifier.height(20.dp))

        StreaklyCard(Modifier.fillMaxWidth()) {
            Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                val email = state.user?.email.orEmpty()
                Box(
                    Modifier.size(52.dp).clip(CircleShape).background(StreaklyColors.Green),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(email.firstOrNull()?.uppercase() ?: "?", style = MaterialTheme.typography.headlineSmall, color = StreaklyColors.OnGreen)
                }
                Spacer(Modifier.width(14.dp))
                Column {
                    Text(email.ifEmpty { "Sin conexión" }, style = MaterialTheme.typography.titleMedium)
                    state.user?.createdAt?.let { createdAt ->
                        Text(
                            "Miembro desde ${memberSince(createdAt)}",
                            style = MaterialTheme.typography.bodySmall,
                            color = StreaklyColors.TextSecondary,
                        )
                    }
                }
            }
        }

        state.summary?.let { summary ->
            Spacer(Modifier.height(22.dp))
            SectionLabel("Resumen")
            Spacer(Modifier.height(10.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                SummaryTile("Racha global", "${summary.globalStreak}", "días", Modifier.weight(1f))
                SummaryTile("Mejor racha", "${summary.bestGlobalStreak}", "días", Modifier.weight(1f))
            }
            Spacer(Modifier.height(10.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                SummaryTile("Hábitos", "${summary.totalHabits}", "activos", Modifier.weight(1f))
                SummaryTile("Mínimos cumplidos", "${summary.totalCompletions}", "en total", Modifier.weight(1f))
            }
        }

        state.error?.let {
            Spacer(Modifier.height(14.dp))
            Text(it, color = StreaklyColors.Danger, style = MaterialTheme.typography.bodySmall)
        }

        Spacer(Modifier.height(28.dp))
        OutlinedButton(
            onClick = { confirmLogout = true },
            modifier = Modifier.fillMaxWidth().height(52.dp),
            shape = RoundedCornerShape(26.dp),
            border = ButtonDefaults.outlinedButtonBorder(enabled = true).copy(width = 1.dp),
            colors = ButtonDefaults.outlinedButtonColors(contentColor = StreaklyColors.Danger),
        ) {
            Icon(Icons.AutoMirrored.Outlined.Logout, null, modifier = Modifier.size(18.dp))
            Spacer(Modifier.width(8.dp))
            Text("Cerrar sesión", style = MaterialTheme.typography.labelLarge)
        }
        Spacer(Modifier.height(16.dp))
        Text(
            "Streakly · hábitos pequeños, todos los días.",
            style = MaterialTheme.typography.bodySmall,
            color = StreaklyColors.TextMuted,
            modifier = Modifier.align(Alignment.CenterHorizontally),
        )
    }

    if (confirmLogout) {
        AlertDialog(
            onDismissRequest = { confirmLogout = false },
            title = { Text("¿Cerrar sesión?") },
            text = { Text("Tus hábitos y rachas quedan guardados en tu cuenta.") },
            confirmButton = { TextButton(onClick = { confirmLogout = false; viewModel.logout() }) { Text("Cerrar sesión", color = StreaklyColors.Danger) } },
            dismissButton = { TextButton(onClick = { confirmLogout = false }) { Text("Cancelar") } },
            containerColor = StreaklyColors.Surface,
        )
    }
}

@Composable
private fun SummaryTile(label: String, value: String, suffix: String, modifier: Modifier = Modifier) {
    Box(
        modifier
            .clip(RoundedCornerShape(16.dp))
            .background(StreaklyColors.Surface)
            .border(1.dp, StreaklyColors.Border, RoundedCornerShape(16.dp))
            .padding(14.dp),
    ) {
        Column {
            Text(label, style = MaterialTheme.typography.bodySmall, color = StreaklyColors.TextSecondary, maxLines = 1)
            Spacer(Modifier.height(6.dp))
            Row(verticalAlignment = Alignment.Bottom) {
                Text(value, style = MaterialTheme.typography.headlineSmall.copy(fontSize = 26.sp))
                Spacer(Modifier.width(4.dp))
                Text(suffix, style = MaterialTheme.typography.bodySmall, color = StreaklyColors.TextSecondary, modifier = Modifier.padding(bottom = 3.dp))
            }
        }
    }
}

private fun memberSince(raw: String): String = runCatching {
    OffsetDateTime.parse(raw).format(DateTimeFormatter.ofPattern("MMMM 'de' yyyy", Spanish))
}.getOrElse { raw.take(10) }
