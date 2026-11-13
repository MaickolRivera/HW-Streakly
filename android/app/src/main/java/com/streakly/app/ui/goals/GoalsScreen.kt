package com.streakly.app.ui.goals

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CalendarToday
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.KeyboardArrowDown
import androidx.compose.material.icons.rounded.KeyboardArrowUp
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.HorizontalDivider
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
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import com.streakly.app.data.remote.GoalDto
import com.streakly.app.data.remote.GoalTaskDto
import com.streakly.app.data.remote.GoalStatus
import com.streakly.app.ui.components.StreaklyTextField
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Search
import com.streakly.app.ui.appViewModel
import com.streakly.app.ui.components.CircleIconButton
import com.streakly.app.ui.components.EmptyState
import com.streakly.app.ui.components.ErrorBox
import com.streakly.app.ui.components.LoadingBox
import com.streakly.app.ui.components.ProgressBar
import com.streakly.app.ui.components.SectionLabel
import com.streakly.app.ui.components.StreaklyCard
import com.streakly.app.ui.remainingText
import com.streakly.app.ui.shortSpanish
import com.streakly.app.ui.theme.StreaklyColors
import java.time.LocalDate

@Composable
fun GoalsScreen(
    onNewGoal: () -> Unit,
    onEditGoal: (String) -> Unit,
    viewModel: GoalsViewModel = appViewModel { GoalsViewModel(it.goalRepository) },
) {
    val state = viewModel.state
    val snackbar = remember { SnackbarHostState() }
    var toDelete by remember { mutableStateOf<GoalDto?>(null) }

    LaunchedEffect(Unit) { viewModel.load() }
    LaunchedEffect(state.message) {
        state.message?.let { snackbar.showSnackbar(it); viewModel.messageShown() }
    }

    Box(Modifier.fillMaxSize()) {
        when {
            state.loading -> LoadingBox()
            state.error != null && state.goals.isEmpty() -> ErrorBox(state.error) { viewModel.load() }
            else -> LazyColumn(
                contentPadding = PaddingValues(horizontal = 20.dp, vertical = 16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                item {
                    val inProgress = state.goals.count { it.totalTasks == 0 || it.completedTasks < it.totalTasks }
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Column(Modifier.weight(1f)) {
                            Text("$inProgress en curso", style = MaterialTheme.typography.bodyMedium, color = StreaklyColors.TextSecondary)
                            Spacer(Modifier.height(4.dp))
                            Text("GOALS", style = MaterialTheme.typography.headlineLarge)
                        }
                        CircleIconButton(Icons.Rounded.Add, "Nuevo goal", onNewGoal)
                    }
                }
                item {
                    GoalFilters(
                        search = state.search,
                        status = state.status,
                        onSearch = viewModel::onSearch,
                        onStatus = viewModel::onStatus,
                    )
                }
                if (state.goals.isEmpty()) {
                    item {
                        if (state.hasFilters) {
                            EmptyState("Sin resultados", "Ningún goal coincide con la búsqueda o el filtro.")
                            Text(
                                "Quitar filtros",
                                color = StreaklyColors.Green,
                                style = MaterialTheme.typography.titleSmall,
                                modifier = Modifier.padding(top = 12.dp).clickable { viewModel.clearFilters() }.padding(6.dp),
                            )
                        } else {
                            EmptyState("Sin goals todavía", "Un goal es un objetivo grande dividido en metas pequeñas. Toca + para crear uno.")
                        }
                    }
                }
                items(state.goals, key = { it.id }) { goal ->
                    GoalCard(
                        goal = goal,
                        expanded = goal.id in state.expanded,
                        onToggleExpanded = { viewModel.toggleExpanded(goal.id) },
                        onToggleTask = { task -> viewModel.toggleTask(goal.id, task.id) },
                        onEdit = { onEditGoal(goal.id) },
                        onDelete = { toDelete = goal },
                    )
                }
            }
        }
        SnackbarHost(snackbar, Modifier.align(Alignment.BottomCenter))
    }

    toDelete?.let { goal ->
        AlertDialog(
            onDismissRequest = { toDelete = null },
            title = { Text("¿Eliminar \"${goal.title}\"?") },
            text = { Text("Se borrarán también sus ${goal.totalTasks} metas.") },
            confirmButton = {
                TextButton(onClick = { viewModel.delete(goal.id); toDelete = null }) { Text("Eliminar", color = StreaklyColors.Danger) }
            },
            dismissButton = { TextButton(onClick = { toDelete = null }) { Text("Cancelar") } },
            containerColor = StreaklyColors.Surface,
        )
    }
}

@Composable
private fun GoalCard(
    goal: GoalDto,
    expanded: Boolean,
    onToggleExpanded: () -> Unit,
    onToggleTask: (GoalTaskDto) -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
) {
    StreaklyCard(Modifier.fillMaxWidth()) {
        Column {
            Column(Modifier.clickable(onClick = onToggleExpanded).padding(16.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(goal.title, style = MaterialTheme.typography.titleLarge, modifier = Modifier.weight(1f))
                    Icon(
                        if (expanded) Icons.Rounded.KeyboardArrowUp else Icons.Rounded.KeyboardArrowDown,
                        contentDescription = if (expanded) "Contraer" else "Expandir",
                        tint = StreaklyColors.TextSecondary,
                    )
                }
                goal.description?.let {
                    Spacer(Modifier.height(6.dp))
                    Text(it, style = MaterialTheme.typography.bodyMedium, color = StreaklyColors.TextSecondary)
                }
                goal.dueDate?.let { raw ->
                    val due = LocalDate.parse(raw)
                    Spacer(Modifier.height(10.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Outlined.CalendarToday, null, tint = StreaklyColors.TextSecondary, modifier = Modifier.size(13.dp))
                        Spacer(Modifier.width(6.dp))
                        Text(
                            "Termina el ${due.shortSpanish()} · ${due.remainingText()}",
                            style = MaterialTheme.typography.bodySmall,
                            color = StreaklyColors.TextSecondary,
                        )
                    }
                }
                Spacer(Modifier.height(12.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    ProgressBar(goal.progress / 100f, Modifier.weight(1f))
                    Text("${goal.progress}%", style = MaterialTheme.typography.titleSmall, modifier = Modifier.padding(start = 14.dp))
                }
                Spacer(Modifier.height(8.dp))
                Text("${goal.completedTasks} de ${goal.totalTasks} metas", style = MaterialTheme.typography.bodySmall, color = StreaklyColors.TextSecondary)
            }

            AnimatedVisibility(expanded) {
                Column {
                    HorizontalDivider(color = StreaklyColors.Border)
                    Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        SectionLabel("Metas", Modifier.padding(start = 4.dp, top = 4.dp, bottom = 4.dp))
                        goal.tasks.forEach { task -> TaskRow(task) { onToggleTask(task) } }
                        if (goal.tasks.isEmpty()) {
                            Text("Este goal no tiene metas.", style = MaterialTheme.typography.bodySmall, color = StreaklyColors.TextSecondary, modifier = Modifier.padding(4.dp))
                        }
                        Row(Modifier.align(Alignment.End)) {
                            Text(
                                "Editar",
                                color = StreaklyColors.Green,
                                style = MaterialTheme.typography.bodySmall,
                                modifier = Modifier.clickable(onClick = onEdit).padding(6.dp),
                            )
                            Spacer(Modifier.width(8.dp))
                            Text(
                                "Eliminar goal",
                                color = StreaklyColors.Danger,
                                style = MaterialTheme.typography.bodySmall,
                                modifier = Modifier.clickable(onClick = onDelete).padding(6.dp),
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun TaskRow(task: GoalTaskDto, onClick: () -> Unit) {
    val shape = RoundedCornerShape(12.dp)
    Row(
        Modifier
            .fillMaxWidth()
            .clip(shape)
            .background(if (task.isCompleted) StreaklyColors.GreenTint.copy(alpha = 0.6f) else StreaklyColors.SurfaceHigh)
            .clickable(onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            Modifier
                .size(22.dp)
                .clip(RoundedCornerShape(6.dp))
                .background(if (task.isCompleted) StreaklyColors.Green else StreaklyColors.Surface)
                .border(1.5.dp, if (task.isCompleted) StreaklyColors.Green else StreaklyColors.TextMuted, RoundedCornerShape(6.dp)),
            contentAlignment = Alignment.Center,
        ) {
            if (task.isCompleted) Icon(Icons.Rounded.Check, "Completada", tint = StreaklyColors.OnGreen, modifier = Modifier.size(16.dp))
        }
        Spacer(Modifier.width(12.dp))
        Text(
            task.title,
            style = MaterialTheme.typography.bodyMedium.copy(
                textDecoration = if (task.isCompleted) TextDecoration.LineThrough else TextDecoration.None,
            ),
            color = if (task.isCompleted) StreaklyColors.TextSecondary else StreaklyColors.TextPrimary,
        )
    }
}

/** Busqueda por texto + filtro por estado (RF-09: dos criterios, resueltos en el servidor). */
@Composable
private fun GoalFilters(
    search: String,
    status: String,
    onSearch: (String) -> Unit,
    onStatus: (String) -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        StreaklyTextField(
            value = search,
            onValueChange = onSearch,
            placeholder = "Buscar goal…",
            trailingIcon = {
                if (search.isNotEmpty()) {
                    Icon(
                        Icons.Rounded.Close, "Borrar búsqueda",
                        tint = StreaklyColors.TextSecondary,
                        modifier = Modifier.clip(RoundedCornerShape(12.dp)).clickable { onSearch("") }.padding(4.dp),
                    )
                } else {
                    Icon(Icons.Rounded.Search, null, tint = StreaklyColors.TextMuted)
                }
            },
        )
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            listOf(GoalStatus.ALL to "Todos", GoalStatus.ACTIVE to "En curso", GoalStatus.COMPLETED to "Completados")
                .forEach { (value, label) ->
                    val selected = value == status
                    val shape = RoundedCornerShape(20.dp)
                    Text(
                        label,
                        style = MaterialTheme.typography.titleSmall,
                        color = if (selected) StreaklyColors.OnGreen else StreaklyColors.TextPrimary,
                        modifier = Modifier
                            .clip(shape)
                            .background(if (selected) StreaklyColors.Green else StreaklyColors.Surface)
                            .border(1.dp, if (selected) StreaklyColors.Green else StreaklyColors.Border, shape)
                            .clickable { onStatus(value) }
                            .padding(horizontal = 14.dp, vertical = 8.dp),
                    )
                }
        }
    }
}
