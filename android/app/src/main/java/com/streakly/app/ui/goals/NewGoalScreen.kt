package com.streakly.app.ui.goals

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CalendarToday
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDefaults
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
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
import com.streakly.app.ui.appViewModel
import com.streakly.app.ui.components.BackHeader
import com.streakly.app.ui.components.CircleIconButton
import com.streakly.app.ui.components.FieldLabel
import com.streakly.app.ui.components.PrimaryButton
import com.streakly.app.ui.components.StreaklyTextField
import com.streakly.app.ui.theme.StreaklyColors
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter

private val dateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy")

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NewGoalScreen(
    onBack: () -> Unit,
    viewModel: NewGoalViewModel = appViewModel { NewGoalViewModel(it.goalRepository) },
) {
    val state = viewModel.state
    var showDatePicker by remember { mutableStateOf(false) }

    LaunchedEffect(state.saved) { if (state.saved) onBack() }

    Column(Modifier.fillMaxSize().background(StreaklyColors.Background).imePadding().navigationBarsPadding()) {
        Column(
            Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp),
        ) {
            BackHeader("Nuevo goal", onBack)
            Spacer(Modifier.height(16.dp))

            FieldLabel("Título")
            StreaklyTextField(state.title, viewModel::onTitle, placeholder = "Entregar la app Android")

            Spacer(Modifier.height(20.dp))
            FieldLabel("Descripción")
            StreaklyTextField(
                state.description, viewModel::onDescription,
                placeholder = "¿Qué significa terminarlo?",
                singleLine = false, minLines = 3,
            )

            Spacer(Modifier.height(20.dp))
            FieldLabel("Fecha de finalización")
            val shape = RoundedCornerShape(14.dp)
            Row(
                Modifier
                    .fillMaxWidth()
                    .clip(shape)
                    .background(StreaklyColors.Surface)
                    .border(1.dp, StreaklyColors.Border, shape)
                    .clickable { showDatePicker = true }
                    .padding(horizontal = 16.dp, vertical = 16.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    state.dueDate?.format(dateFormatter) ?: "Sin fecha",
                    style = MaterialTheme.typography.bodyLarge,
                    color = if (state.dueDate != null) StreaklyColors.TextPrimary else StreaklyColors.TextMuted,
                    modifier = Modifier.weight(1f),
                )
                Icon(Icons.Outlined.CalendarToday, "Elegir fecha", tint = StreaklyColors.TextSecondary, modifier = Modifier.size(20.dp))
            }

            Spacer(Modifier.height(20.dp))
            FieldLabel("Metas")
            state.tasks.forEachIndexed { index, task ->
                Row(
                    Modifier
                        .fillMaxWidth()
                        .padding(bottom = 8.dp)
                        .clip(shape)
                        .background(StreaklyColors.Surface)
                        .border(1.dp, StreaklyColors.Border, shape)
                        .padding(start = 16.dp, end = 6.dp, top = 6.dp, bottom = 6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(task, style = MaterialTheme.typography.titleSmall, modifier = Modifier.weight(1f))
                    Icon(
                        Icons.Rounded.Close, "Quitar meta",
                        tint = StreaklyColors.TextSecondary,
                        modifier = Modifier.size(36.dp).clip(RoundedCornerShape(18.dp)).clickable { viewModel.removeTask(index) }.padding(8.dp),
                    )
                }
            }
            Row(verticalAlignment = Alignment.CenterVertically) {
                StreaklyTextField(state.newTask, viewModel::onNewTask, placeholder = "Agregar otra meta", modifier = Modifier.weight(1f))
                Spacer(Modifier.width(10.dp))
                CircleIconButton(Icons.Rounded.Add, "Agregar meta", viewModel::addTask, size = 48.dp)
            }
            Spacer(Modifier.height(8.dp))
            Text(
                "Cada meta es de tipo hecho / no hecho. Las irás chuleando desde la tarjeta del goal.",
                style = MaterialTheme.typography.bodySmall,
                color = StreaklyColors.TextSecondary,
            )

            state.error?.let {
                Spacer(Modifier.height(14.dp))
                Text(it, color = StreaklyColors.Danger, style = MaterialTheme.typography.bodyMedium)
            }
            Spacer(Modifier.height(20.dp))
        }

        PrimaryButton(
            "Guardar goal",
            onClick = viewModel::save,
            loading = state.saving,
            modifier = Modifier.padding(horizontal = 20.dp, vertical = 14.dp),
        )
    }

    if (showDatePicker) {
        val initial = (state.dueDate ?: LocalDate.now().plusMonths(1)).atStartOfDay(ZoneOffset.UTC).toInstant().toEpochMilli()
        val pickerState = rememberDatePickerState(initialSelectedDateMillis = initial)
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.onDueDate(pickerState.selectedDateMillis?.let { Instant.ofEpochMilli(it).atZone(ZoneOffset.UTC).toLocalDate() })
                    showDatePicker = false
                }) { Text("Listo", color = StreaklyColors.Green) }
            },
            dismissButton = {
                TextButton(onClick = { viewModel.onDueDate(null); showDatePicker = false }) { Text("Sin fecha") }
            },
            colors = DatePickerDefaults.colors(containerColor = StreaklyColors.Surface),
        ) {
            DatePicker(
                state = pickerState,
                colors = DatePickerDefaults.colors(
                    containerColor = StreaklyColors.Surface,
                    selectedDayContainerColor = StreaklyColors.Green,
                    selectedDayContentColor = StreaklyColors.OnGreen,
                    todayDateBorderColor = StreaklyColors.Green,
                    todayContentColor = StreaklyColors.Green,
                ),
            )
        }
    }
}
