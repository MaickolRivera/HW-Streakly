package com.streakly.app.ui.today

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.Remove
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.streakly.app.data.local.UiPrefs
import com.streakly.app.data.remote.HabitType
import com.streakly.app.data.remote.TodayHabitDto
import com.streakly.app.ui.appViewModel
import com.streakly.app.ui.components.CircleIconButton
import com.streakly.app.ui.components.EmptyState
import com.streakly.app.ui.components.ErrorBox
import com.streakly.app.ui.components.HabitIcons
import com.streakly.app.ui.components.LoadingBox
import com.streakly.app.ui.components.ProgressBar
import com.streakly.app.ui.components.SectionLabel
import com.streakly.app.ui.components.SmallStreak
import com.streakly.app.ui.components.StreakBadge
import com.streakly.app.ui.components.StreaklyCard
import com.streakly.app.ui.longSpanish
import com.streakly.app.ui.minimumText
import com.streakly.app.ui.pretty
import com.streakly.app.ui.theme.StreaklyColors
import java.time.LocalDate

@Composable
fun TodayScreen(
    onNewHabit: () -> Unit,
    onEditHabit: (String) -> Unit,
    viewModel: TodayViewModel = appViewModel { TodayViewModel(it.habitRepository, it.uiPreferences) },
) {
    val state = viewModel.state
    val prefs by viewModel.prefs.collectAsStateWithLifecycle(initialValue = UiPrefs())
    val snackbar = remember { SnackbarHostState() }
    var options by remember { mutableStateOf<TodayHabitDto?>(null) }
    var toDelete by remember { mutableStateOf<TodayHabitDto?>(null) }

    // Se recarga cada vez que se entra a la pantalla (p. ej. al volver de "Nuevo hábito").
    LaunchedEffect(Unit) { viewModel.load() }
    LaunchedEffect(state.message) {
        state.message?.let { snackbar.showSnackbar(it); viewModel.messageShown() }
    }

    Box(Modifier.fillMaxSize()) {
        when {
            state.loading && state.data == null -> LoadingBox()
            state.error != null && state.data == null -> ErrorBox(state.error) { viewModel.load() }
            else -> state.data?.let { data ->
                LazyColumn(
                    contentPadding = PaddingValues(horizontal = 20.dp, vertical = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    item { Header(data.globalStreak, onNewHabit) }
                    item { MinimumsCard(data.completed, data.total) }
                    item { SectionLabel("Tus hábitos", Modifier.padding(top = 14.dp, bottom = 2.dp)) }
                    // Preferencias de interfaz: se aplican solo en la vista, el conteo "3 de 7" no cambia.
                    val visible = data.habits
                        .let { list -> if (prefs.sortByName) list.sortedBy { it.name.lowercase() } else list }
                        .let { list -> if (prefs.hideCompleted) list.filterNot { it.completed } else list }
                    when {
                        data.habits.isEmpty() -> item {
                            EmptyState("No hay hábitos para hoy", "Toca + para crear tu primer hábito y empezar tu racha.")
                        }
                        visible.isEmpty() -> item {
                            EmptyState("¡Cumpliste todos tus mínimos!", "Los hábitos cumplidos están ocultos. Puedes mostrarlos desde Perfil → Preferencias.")
                        }
                    }
                    items(visible, key = { it.id }) { habit ->
                        HabitRow(
                            habit = habit,
                            onToggle = { viewModel.toggle(habit) },
                            onIncrement = { viewModel.increment(habit) },
                            onDecrement = { viewModel.decrement(habit) },
                            onLongPress = { options = habit },
                        )
                    }
                    item { Spacer(Modifier.height(12.dp)) }
                }
            }
        }
        SnackbarHost(snackbar, Modifier.align(Alignment.BottomCenter))
    }

    options?.let { habit ->
        AlertDialog(
            onDismissRequest = { options = null },
            title = { Text(habit.name) },
            text = { Text("¿Qué quieres hacer con este hábito?") },
            confirmButton = {
                TextButton(onClick = { options = null; onEditHabit(habit.id) }) {
                    Text("Editar", color = StreaklyColors.Green)
                }
            },
            dismissButton = {
                TextButton(onClick = { options = null; toDelete = habit }) {
                    Text("Eliminar", color = StreaklyColors.Danger)
                }
            },
            containerColor = StreaklyColors.Surface,
        )
    }

    toDelete?.let { habit ->
        AlertDialog(
            onDismissRequest = { toDelete = null },
            title = { Text("¿Eliminar \"${habit.name}\"?") },
            text = { Text("Se borrará el hábito y todo su historial de rachas.") },
            confirmButton = {
                TextButton(onClick = { viewModel.delete(habit); toDelete = null }) {
                    Text("Eliminar", color = StreaklyColors.Danger)
                }
            },
            dismissButton = { TextButton(onClick = { toDelete = null }) { Text("Cancelar") } },
            containerColor = StreaklyColors.Surface,
        )
    }
}

@Composable
private fun Header(globalStreak: Int, onNewHabit: () -> Unit) {
    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(bottom = 8.dp)) {
        Column(Modifier.weight(1f)) {
            Text(LocalDate.now().longSpanish(), style = MaterialTheme.typography.bodyMedium, color = StreaklyColors.TextSecondary)
            Spacer(Modifier.height(4.dp))
            Text("HOY", style = MaterialTheme.typography.headlineLarge)
        }
        StreakBadge(globalStreak)
        Spacer(Modifier.width(10.dp))
        CircleIconButton(Icons.Rounded.Add, "Nuevo hábito", onNewHabit)
    }
}

@Composable
private fun MinimumsCard(completed: Int, total: Int) {
    StreaklyCard(Modifier.fillMaxWidth()) {
        Column(Modifier.padding(18.dp)) {
            Row {
                Text("Mínimos de hoy", style = MaterialTheme.typography.bodyMedium, color = StreaklyColors.TextSecondary, modifier = Modifier.weight(1f))
                val missing = total - completed
                Text(
                    if (total == 0) "" else if (missing == 0) "¡Completo!" else "Faltan $missing",
                    style = MaterialTheme.typography.bodyMedium,
                    color = if (missing == 0 && total > 0) StreaklyColors.Green else StreaklyColors.TextSecondary,
                )
            }
            Spacer(Modifier.height(8.dp))
            Row(verticalAlignment = Alignment.Bottom) {
                Text("$completed", style = MaterialTheme.typography.displayLarge.copy(fontSize = 36.sp))
                Spacer(Modifier.width(6.dp))
                Text("de $total", style = MaterialTheme.typography.titleMedium, color = StreaklyColors.TextSecondary, modifier = Modifier.padding(bottom = 6.dp))
            }
            Spacer(Modifier.height(12.dp))
            ProgressBar(if (total == 0) 0f else completed.toFloat() / total)
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun HabitRow(
    habit: TodayHabitDto,
    onToggle: () -> Unit,
    onIncrement: () -> Unit,
    onDecrement: () -> Unit,
    onLongPress: () -> Unit,
) {
    val isCounter = habit.type == HabitType.COUNTER
    val shape = RoundedCornerShape(16.dp)
    Row(
        Modifier
            .fillMaxWidth()
            .clip(shape)
            .background(if (habit.completed) StreaklyColors.GreenTint else StreaklyColors.Surface)
            .border(1.dp, if (habit.completed) StreaklyColors.GreenBorder else StreaklyColors.Border, shape)
            .combinedClickable(onClick = { if (!isCounter) onToggle() }, onLongClick = onLongPress)
            .padding(horizontal = 14.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            Modifier
                .size(38.dp)
                .clip(CircleShape)
                .background(if (habit.completed) StreaklyColors.Green else StreaklyColors.SurfaceHigh),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                HabitIcons.of(habit.icon), null,
                tint = if (habit.completed) StreaklyColors.OnGreen else StreaklyColors.TextSecondary,
                modifier = Modifier.size(20.dp),
            )
        }
        Spacer(Modifier.width(12.dp))
        Column(Modifier.weight(1f)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(habit.name, style = MaterialTheme.typography.titleMedium, maxLines = 1)
                Spacer(Modifier.width(8.dp))
                SmallStreak(habit.currentStreak)
            }
            Text(minimumText(habit.targetValue, habit.unit), style = MaterialTheme.typography.bodySmall, color = StreaklyColors.TextSecondary)
        }

        if (isCounter) {
            Icon(
                Icons.Rounded.Remove, "Restar",
                tint = if (habit.value > 0) StreaklyColors.TextPrimary else StreaklyColors.TextMuted,
                modifier = Modifier.size(32.dp).clip(CircleShape).clickable(enabled = habit.value > 0, onClick = onDecrement).padding(6.dp),
            )
            Text(
                "${habit.value.pretty()} / ${habit.targetValue.pretty()}",
                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                modifier = Modifier.padding(horizontal = 6.dp),
            )
            CircleIconButton(Icons.Rounded.Add, "Sumar", onIncrement, size = 36.dp, filled = false)
        } else {
            CheckCircle(habit.completed)
        }
    }
}

@Composable
private fun CheckCircle(checked: Boolean) {
    Box(
        Modifier
            .size(26.dp)
            .clip(CircleShape)
            .background(if (checked) StreaklyColors.Green else StreaklyColors.Surface)
            .border(1.5.dp, if (checked) StreaklyColors.Green else StreaklyColors.TextMuted, CircleShape),
        contentAlignment = Alignment.Center,
    ) {
        if (checked) Icon(Icons.Rounded.Check, "Hecho", tint = StreaklyColors.OnGreen, modifier = Modifier.size(18.dp))
    }
}
