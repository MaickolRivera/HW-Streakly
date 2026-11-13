package com.streakly.app.ui.admin

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.streakly.app.data.remote.ActiveUserDto
import com.streakly.app.ui.appViewModel
import com.streakly.app.ui.components.BackHeader
import com.streakly.app.ui.components.EmptyState
import com.streakly.app.ui.components.ErrorBox
import com.streakly.app.ui.components.LoadingBox
import com.streakly.app.ui.components.SectionLabel
import com.streakly.app.ui.components.StreaklyCard
import com.streakly.app.ui.theme.StreaklyColors
import java.time.Duration
import java.time.OffsetDateTime

/** Vista de administrador: solo lectura de los usuarios activos en los ultimos dias. */
@Composable
fun ActiveUsersScreen(
    onBack: () -> Unit,
    viewModel: ActiveUsersViewModel = appViewModel { ActiveUsersViewModel(it.adminRepository) },
) {
    val state = viewModel.state
    LaunchedEffect(Unit) { viewModel.load() }

    Column(Modifier.fillMaxSize().padding(horizontal = 20.dp)) {
        BackHeader("Usuarios activos", onBack)

        when {
            state.loading -> LoadingBox()
            state.error != null -> ErrorBox(state.error, onRetry = viewModel::load)
            else -> LazyColumn(
                contentPadding = PaddingValues(vertical = 12.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                item {
                    SectionLabel("${state.users.size} activos · últimos ${ActiveUsersViewModel.PERIOD_DAYS} días")
                }
                if (state.users.isEmpty()) {
                    item { EmptyState("Sin actividad", "Nadie ha usado la app en este periodo.") }
                }
                items(state.users, key = { it.id }) { ActiveUserRow(it) }
            }
        }
    }
}

@Composable
private fun ActiveUserRow(user: ActiveUserDto) {
    StreaklyCard(Modifier.fillMaxWidth()) {
        Row(Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(
                Modifier.size(40.dp).clip(CircleShape).background(StreaklyColors.GreenTint),
                contentAlignment = Alignment.Center,
            ) {
                Text(user.email.firstOrNull()?.uppercase() ?: "?", style = MaterialTheme.typography.titleMedium, color = StreaklyColors.Green)
            }
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Text(user.email, style = MaterialTheme.typography.titleMedium, maxLines = 1, overflow = TextOverflow.Ellipsis)
                Spacer(Modifier.height(2.dp))
                Text(lastSeen(user.lastActiveAt), style = MaterialTheme.typography.bodySmall, color = StreaklyColors.TextSecondary)
            }
        }
    }
}

/** "Activo hace 5 min" / "hace 3 h" / "hace 2 días" */
private fun lastSeen(raw: String): String = runCatching {
    val elapsed = Duration.between(OffsetDateTime.parse(raw), OffsetDateTime.now())
    when {
        elapsed.toMinutes() < 1 -> "Activo ahora"
        elapsed.toHours() < 1 -> "Activo hace ${elapsed.toMinutes()} min"
        elapsed.toDays() < 1 -> "Activo hace ${elapsed.toHours()} h"
        elapsed.toDays() == 1L -> "Activo ayer"
        else -> "Activo hace ${elapsed.toDays()} días"
    }
}.getOrElse { raw.take(10) }
