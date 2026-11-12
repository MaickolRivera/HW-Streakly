package com.streakly.app.ui.streaks

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.streakly.app.data.remote.HabitStatsDto
import com.streakly.app.ui.Spanish
import com.streakly.app.ui.appViewModel
import com.streakly.app.ui.components.Dot
import com.streakly.app.ui.components.EmptyState
import com.streakly.app.ui.components.ErrorBox
import com.streakly.app.ui.components.LoadingBox
import com.streakly.app.ui.components.ProgressBar
import com.streakly.app.ui.components.StreaklyCard
import com.streakly.app.ui.theme.StreaklyColors
import java.time.LocalDate
import java.time.format.DateTimeFormatter

@Composable
fun StreaksScreen(viewModel: StreaksViewModel = appViewModel { StreaksViewModel(it.habitRepository) }) {
    val state = viewModel.state
    LaunchedEffect(Unit) { viewModel.load() }

    when {
        state.loading -> LoadingBox()
        state.error != null && state.habits.isEmpty() -> ErrorBox(state.error) { viewModel.load() }
        else -> LazyColumn(
            contentPadding = PaddingValues(vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            item {
                Column(Modifier.padding(horizontal = 20.dp)) {
                    Text("Últimas $HEATMAP_WEEKS semanas", style = MaterialTheme.typography.bodyMedium, color = StreaklyColors.TextSecondary)
                    Spacer(Modifier.height(4.dp))
                    Text("RACHAS", style = MaterialTheme.typography.headlineLarge)
                }
            }
            if (state.habits.isEmpty()) {
                item { EmptyState("Aún no hay rachas", "Crea un hábito en Hoy y vuelve aquí para ver tu progreso.", Modifier.padding(horizontal = 20.dp)) }
                return@LazyColumn
            }
            item {
                LazyRow(contentPadding = PaddingValues(horizontal = 20.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(state.habits, key = { it.id }) { habit ->
                        HabitChip(habit.name, habit.id == state.selectedId) { viewModel.select(habit.id) }
                    }
                }
            }
            state.stats?.let { stats ->
                item {
                    Row(
                        Modifier.padding(horizontal = 20.dp).alpha(if (state.statsLoading) 0.5f else 1f),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        StatCard("Racha actual", "${stats.currentStreak}", "días", Modifier.weight(1f))
                        StatCard("Mejor racha", "${stats.bestStreak}", "récord", Modifier.weight(1f))
                        StatCard("Cumplimiento", "${stats.compliancePercent}", "%", Modifier.weight(1f))
                    }
                }
                item { HeatmapCard(stats, Modifier.padding(horizontal = 20.dp).alpha(if (state.statsLoading) 0.5f else 1f)) }
            }
            if (state.compliance.isNotEmpty()) {
                item { ComplianceCard(state.compliance.map { it.name to it.percent }, Modifier.padding(horizontal = 20.dp)) }
            }
        }
    }
}

@Composable
private fun HabitChip(label: String, selected: Boolean, onClick: () -> Unit) {
    val shape = RoundedCornerShape(22.dp)
    Box(
        Modifier
            .clip(shape)
            .background(if (selected) StreaklyColors.Green else StreaklyColors.Surface)
            .border(1.dp, if (selected) StreaklyColors.Green else StreaklyColors.Border, shape)
            .clickable(onClick = onClick)
            .padding(horizontal = 18.dp, vertical = 11.dp),
    ) {
        Text(label, style = MaterialTheme.typography.titleSmall, color = if (selected) StreaklyColors.OnGreen else StreaklyColors.TextPrimary)
    }
}

@Composable
private fun StatCard(label: String, value: String, suffix: String, modifier: Modifier = Modifier) {
    StreaklyCard(modifier) {
        Column(Modifier.fillMaxWidth().padding(12.dp)) {
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

private val monthFormatter = DateTimeFormatter.ofPattern("MMM", Spanish)
private val rowLabels = mapOf(0 to "L", 2 to "M", 4 to "V")

@Composable
private fun HeatmapCard(stats: HabitStatsDto, modifier: Modifier = Modifier) {
    val days = stats.heatmap
    val weeks = (days.size + 6) / 7

    StreaklyCard(modifier.fillMaxWidth()) {
        Column(Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(stats.name, style = MaterialTheme.typography.titleLarge, modifier = Modifier.weight(1f), maxLines = 1)
                Text("${stats.completedDays} de ${stats.scheduledDays} días", style = MaterialTheme.typography.bodySmall, color = StreaklyColors.TextSecondary)
            }
            Spacer(Modifier.height(14.dp))

            BoxWithConstraints(Modifier.fillMaxWidth()) {
                val labelWidth = 16.dp
                val gap = 3.dp
                val cell = ((maxWidth - labelWidth - gap * (weeks - 1)) / weeks).coerceAtMost(16.dp)

                Column {
                    // Meses: se muestra la etiqueta en la primera semana de cada mes.
                    Row {
                        Spacer(Modifier.width(labelWidth))
                        var lastMonth = -1
                        for (w in 0 until weeks) {
                            val date = days.getOrNull(w * 7)?.let { LocalDate.parse(it.date) }
                            val month = date?.monthValue ?: -1
                            Box(Modifier.width(cell + if (w < weeks - 1) gap else 0.dp)) {
                                if (month != lastMonth && date != null && (w == 0 || date.dayOfMonth <= 7)) {
                                    Text(
                                        date.format(monthFormatter).replace(".", ""),
                                        style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                                        color = StreaklyColors.TextSecondary,
                                        softWrap = false,
                                        maxLines = 1,
                                    )
                                }
                            }
                            if (month != -1) lastMonth = month
                        }
                    }
                    Spacer(Modifier.height(4.dp))
                    for (row in 0 until 7) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(Modifier.width(labelWidth)) {
                                rowLabels[row]?.let {
                                    Text(it, style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp), color = StreaklyColors.TextSecondary)
                                }
                            }
                            for (w in 0 until weeks) {
                                val day = days.getOrNull(w * 7 + row)
                                if (day != null) {
                                    val color = StreaklyColors.Heat[day.level.coerceIn(0, 4)]
                                    Dot(if (day.scheduled || day.level > 0) color else color.copy(alpha = 0.35f), size = cell)
                                } else {
                                    Spacer(Modifier.size(cell))
                                }
                                if (w < weeks - 1) Spacer(Modifier.width(gap))
                            }
                        }
                        if (row < 6) Spacer(Modifier.height(gap))
                    }
                }
            }

            Spacer(Modifier.height(12.dp))
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End, verticalAlignment = Alignment.CenterVertically) {
                Text("Menos", style = MaterialTheme.typography.labelSmall, color = StreaklyColors.TextSecondary)
                Spacer(Modifier.width(6.dp))
                StreaklyColors.Heat.forEach {
                    Dot(it, size = 11.dp)
                    Spacer(Modifier.width(3.dp))
                }
                Spacer(Modifier.width(3.dp))
                Text("Más", style = MaterialTheme.typography.labelSmall, color = StreaklyColors.TextSecondary)
            }
        }
    }
}

@Composable
private fun ComplianceCard(items: List<Pair<String, Int>>, modifier: Modifier = Modifier) {
    StreaklyCard(modifier.fillMaxWidth()) {
        Column(Modifier.padding(16.dp)) {
            Text("Cumplimiento por hábito · 30 días", style = MaterialTheme.typography.titleLarge.copy(fontSize = 15.sp))
            Spacer(Modifier.height(14.dp))
            items.forEachIndexed { index, (name, percent) ->
                Row {
                    Text(name, style = MaterialTheme.typography.bodyMedium, modifier = Modifier.weight(1f))
                    Text("$percent%", style = MaterialTheme.typography.bodySmall, color = StreaklyColors.TextSecondary)
                }
                Spacer(Modifier.height(6.dp))
                ProgressBar(percent / 100f, height = 5.dp)
                if (index < items.lastIndex) Spacer(Modifier.height(14.dp))
            }
        }
    }
}
