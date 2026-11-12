package com.streakly.app.ui.habit

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.streakly.app.data.HabitRepository
import com.streakly.app.data.remote.CreateHabitRequest
import com.streakly.app.data.remote.HabitType
import kotlinx.coroutines.launch
import java.time.LocalTime

data class NewHabitUiState(
    val name: String = "",
    val icon: String = "sun",
    val type: String = HabitType.BOOLEAN,
    val target: Int = 20,
    val unit: String = "min",
    val days: Set<Int> = setOf(1, 2, 3, 4, 5),
    val reminderEnabled: Boolean = false,
    val reminderTime: LocalTime = LocalTime.of(7, 0),
    val saving: Boolean = false,
    val error: String? = null,
    val saved: Boolean = false,
)

class NewHabitViewModel(private val repository: HabitRepository) : ViewModel() {

    var state by mutableStateOf(NewHabitUiState())
        private set

    fun onName(value: String) { state = state.copy(name = value.take(60), error = null) }
    fun onIcon(value: String) { state = state.copy(icon = value) }
    fun onUnit(value: String) { state = state.copy(unit = value.take(30)) }
    fun onReminderEnabled(value: Boolean) { state = state.copy(reminderEnabled = value) }
    fun onReminderTime(value: LocalTime) { state = state.copy(reminderTime = value) }

    fun onType(value: String) {
        // Sugerencias de unidad al cambiar de tipo, sin pisar lo que el usuario ya escribio.
        val unit = when {
            value == HabitType.COUNTER && state.unit == "min" -> "veces"
            value == HabitType.BOOLEAN && state.unit == "veces" -> "min"
            else -> state.unit
        }
        val target = if (value == HabitType.COUNTER && state.target > 20) 2 else state.target
        state = state.copy(type = value, unit = unit, target = target)
    }

    fun increment() { state = state.copy(target = (state.target + stepFor(state.target)).coerceAtMost(999)) }
    fun decrement() { state = state.copy(target = (state.target - stepFor(state.target - 1)).coerceAtLeast(1)) }

    // En hechos/no hecho con minutos se avanza de 5 en 5; en contadores de 1 en 1.
    private fun stepFor(value: Int) = if (state.type == HabitType.BOOLEAN && value >= 5) 5 else 1

    fun toggleDay(day: Int) {
        val days = if (day in state.days) state.days - day else state.days + day
        state = state.copy(days = days, error = null)
    }

    fun save() {
        when {
            state.name.isBlank() -> { state = state.copy(error = "Ponle un nombre al hábito."); return }
            state.days.isEmpty() -> { state = state.copy(error = "Elige al menos un día."); return }
        }
        state = state.copy(saving = true, error = null)
        viewModelScope.launch {
            val request = CreateHabitRequest(
                name = state.name.trim(),
                icon = state.icon,
                type = state.type,
                targetValue = state.target.toDouble(),
                unit = state.unit.trim(),
                days = state.days.sorted(),
                reminderTime = if (state.reminderEnabled) "%02d:%02d:00".format(state.reminderTime.hour, state.reminderTime.minute) else null,
            )
            repository.create(request)
                .onSuccess { state = state.copy(saving = false, saved = true) }
                .onFailure { state = state.copy(saving = false, error = it.message) }
        }
    }
}
