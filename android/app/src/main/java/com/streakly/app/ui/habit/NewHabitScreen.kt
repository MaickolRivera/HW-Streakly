package com.streakly.app.ui.habit

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Remove
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TimePicker
import androidx.compose.material3.TimePickerDefaults
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.streakly.app.data.remote.HabitType
import com.streakly.app.ui.Spanish
import com.streakly.app.ui.appViewModel
import com.streakly.app.ui.components.BackHeader
import com.streakly.app.ui.components.CircleIconButton
import com.streakly.app.ui.components.FieldLabel
import com.streakly.app.ui.components.HabitIcons
import com.streakly.app.ui.components.LoadingBox
import com.streakly.app.ui.components.PrimaryButton
import com.streakly.app.ui.components.StreaklyCard
import com.streakly.app.ui.components.StreaklyTextField
import com.streakly.app.ui.theme.StreaklyColors
import java.time.LocalTime
import java.time.format.DateTimeFormatter

private val dayLabels = listOf(1 to "L", 2 to "M", 3 to "M", 4 to "J", 5 to "V", 6 to "S", 7 to "D")

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NewHabitScreen(
    onBack: () -> Unit,
    habitId: String? = null,
    viewModel: NewHabitViewModel = appViewModel { NewHabitViewModel(it.habitRepository, habitId) },
) {
    val state = viewModel.state
    var showTimePicker by remember { mutableStateOf(false) }

    LaunchedEffect(state.saved) { if (state.saved) onBack() }

    if (state.loading) {
        LoadingBox()
        return
    }

    Column(Modifier.fillMaxSize().background(StreaklyColors.Background).imePadding().navigationBarsPadding()) {
        Column(
            Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp),
        ) {
            BackHeader(if (state.isEditing) "Editar hábito" else "Nuevo hábito", onBack)
            Spacer(Modifier.height(16.dp))

            FieldLabel("Nombre")
            StreaklyTextField(state.name, viewModel::onName, placeholder = "Salir a caminar")

            Spacer(Modifier.height(22.dp))
            FieldLabel("Ícono")
            Row(Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                HabitIcons.all.forEach { (key, icon) ->
                    val selected = key == state.icon
                    Box(
                        Modifier
                            .size(50.dp)
                            .clip(RoundedCornerShape(14.dp))
                            .background(if (selected) StreaklyColors.Green else StreaklyColors.Surface)
                            .border(1.dp, if (selected) StreaklyColors.Green else StreaklyColors.Border, RoundedCornerShape(14.dp))
                            .clickable { viewModel.onIcon(key) },
                        contentAlignment = Alignment.Center,
                    ) {
                        Icon(icon, key, tint = if (selected) StreaklyColors.OnGreen else StreaklyColors.TextPrimary)
                    }
                }
            }

            Spacer(Modifier.height(22.dp))
            FieldLabel("Tipo de meta")
            SegmentedControl(
                options = listOf(HabitType.BOOLEAN to "Hecho / no hecho", HabitType.COUNTER to "Contador"),
                selected = state.type,
                onSelect = viewModel::onType,
                enabled = !state.isEditing,
            )
            if (state.isEditing) {
                Spacer(Modifier.height(6.dp))
                Text(
                    "El tipo no se puede cambiar para no alterar el historial de rachas.",
                    style = MaterialTheme.typography.bodySmall,
                    color = StreaklyColors.TextMuted,
                )
            }

            Spacer(Modifier.height(22.dp))
            FieldLabel("Mínimo diario")
            StreaklyCard(Modifier.fillMaxWidth()) {
                Row(Modifier.padding(10.dp), verticalAlignment = Alignment.CenterVertically) {
                    CircleIconButton(Icons.Rounded.Remove, "Menos", viewModel::decrement, filled = false)
                    Text(
                        "${state.target}",
                        style = MaterialTheme.typography.headlineSmall,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.width(64.dp),
                    )
                    CircleIconButton(Icons.Rounded.Add, "Más", viewModel::increment, filled = false)
                    Spacer(Modifier.width(14.dp))
                    StreaklyTextField(state.unit, viewModel::onUnit, placeholder = "unidad", modifier = Modifier.weight(1f))
                }
            }
            Spacer(Modifier.height(8.dp))
            Text(
                if (state.type == HabitType.COUNTER) "Suma con + durante el día. Al llegar al mínimo, el día cuenta para tu racha."
                else "Con cumplir este mínimo, el día cuenta para tu racha.",
                style = MaterialTheme.typography.bodySmall,
                color = StreaklyColors.TextSecondary,
            )

            Spacer(Modifier.height(22.dp))
            FieldLabel("Días")
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                dayLabels.forEach { (day, label) ->
                    val selected = day in state.days
                    Box(
                        Modifier
                            .size(42.dp)
                            .clip(CircleShape)
                            .background(if (selected) StreaklyColors.Green else StreaklyColors.Surface)
                            .border(1.dp, if (selected) StreaklyColors.Green else StreaklyColors.Border, CircleShape)
                            .clickable { viewModel.toggleDay(day) },
                        contentAlignment = Alignment.Center,
                    ) {
                        Text(label, style = MaterialTheme.typography.titleSmall, color = if (selected) StreaklyColors.OnGreen else StreaklyColors.TextPrimary)
                    }
                }
            }

            Spacer(Modifier.height(22.dp))
            StreaklyCard(Modifier.fillMaxWidth()) {
                Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                    Column(
                        Modifier
                            .weight(1f)
                            .clickable(enabled = state.reminderEnabled) { showTimePicker = true },
                    ) {
                        Text("Recordatorio", style = MaterialTheme.typography.titleMedium)
                        Text(
                            if (state.reminderEnabled) "${daysText(state.days)} a las ${formatTime(state.reminderTime)}" else "Desactivado",
                            style = MaterialTheme.typography.bodySmall,
                            color = StreaklyColors.TextSecondary,
                        )
                    }
                    Switch(
                        checked = state.reminderEnabled,
                        onCheckedChange = viewModel::onReminderEnabled,
                        colors = SwitchDefaults.colors(
                            checkedTrackColor = StreaklyColors.Green,
                            checkedThumbColor = StreaklyColors.OnGreen,
                            uncheckedTrackColor = StreaklyColors.SurfaceHigh,
                            uncheckedBorderColor = StreaklyColors.Border,
                        ),
                    )
                }
            }

            state.error?.let {
                Spacer(Modifier.height(14.dp))
                Text(it, color = StreaklyColors.Danger, style = MaterialTheme.typography.bodyMedium)
            }
            Spacer(Modifier.height(20.dp))
        }

        PrimaryButton(
            if (state.isEditing) "Guardar cambios" else "Guardar hábito",
            onClick = viewModel::save,
            loading = state.saving,
            modifier = Modifier.padding(horizontal = 20.dp, vertical = 14.dp),
        )
    }

    if (showTimePicker) {
        val pickerState = rememberTimePickerState(state.reminderTime.hour, state.reminderTime.minute, is24Hour = false)
        AlertDialog(
            onDismissRequest = { showTimePicker = false },
            containerColor = StreaklyColors.Surface,
            confirmButton = {
                TextButton(onClick = {
                    viewModel.onReminderTime(LocalTime.of(pickerState.hour, pickerState.minute))
                    showTimePicker = false
                }) { Text("Listo", color = StreaklyColors.Green) }
            },
            dismissButton = { TextButton(onClick = { showTimePicker = false }) { Text("Cancelar") } },
            text = {
                TimePicker(
                    state = pickerState,
                    colors = TimePickerDefaults.colors(
                        clockDialColor = StreaklyColors.SurfaceHigh,
                        selectorColor = StreaklyColors.Green,
                        timeSelectorSelectedContainerColor = StreaklyColors.GreenTint,
                        timeSelectorSelectedContentColor = StreaklyColors.Green,
                        periodSelectorSelectedContainerColor = StreaklyColors.GreenTint,
                        periodSelectorSelectedContentColor = StreaklyColors.Green,
                    ),
                )
            },
        )
    }
}

@Composable
fun SegmentedControl(
    options: List<Pair<String, String>>,
    selected: String,
    onSelect: (String) -> Unit,
    enabled: Boolean = true,
) {
    Row(
        Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(StreaklyColors.Surface)
            .border(1.dp, StreaklyColors.Border, RoundedCornerShape(14.dp))
            .padding(4.dp),
    ) {
        options.forEach { (value, label) ->
            val isSelected = value == selected
            Box(
                Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(11.dp))
                    .background(if (isSelected) StreaklyColors.SurfaceHigh else StreaklyColors.Surface)
                    .clickable(enabled = enabled) { onSelect(value) }
                    .padding(vertical = 12.dp),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    label,
                    style = MaterialTheme.typography.titleSmall,
                    color = if (isSelected) StreaklyColors.TextPrimary else StreaklyColors.TextSecondary,
                )
            }
        }
    }
}

private fun formatTime(time: LocalTime): String =
    time.format(DateTimeFormatter.ofPattern("h:mm a", Spanish))

private fun daysText(days: Set<Int>): String = when (days) {
    (1..7).toSet() -> "Todos los días"
    (1..5).toSet() -> "De lunes a viernes"
    setOf(6, 7) -> "Fines de semana"
    else -> "Los días elegidos"
}
