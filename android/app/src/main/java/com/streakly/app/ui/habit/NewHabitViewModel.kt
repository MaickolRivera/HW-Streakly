package com.streakly.app.ui.habit

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.streakly.app.data.HabitRepository
import com.streakly.app.data.remote.CreateHabitRequest
import com.streakly.app.data.remote.HabitType
import com.streakly.app.data.remote.UpdateHabitRequest
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
    /** true cuando se edita un habito existente (el tipo queda bloqueado). */
    val isEditing: Boolean = false,
    val loading: Boolean = false,
    val saving: Boolean = false,
    val error: String? = null,
    val saved: Boolean = false,
)

/**
 * Sirve para crear (habitId = null) y para editar (habitId = id del habito).
 */
class NewHabitViewModel(
    private val repository: HabitRepository,
    private val habitId: String? = null,
) : ViewModel() {

    var state by mutableStateOf(NewHabitUiState(isEditing = habitId != null, loading = habitId != null))
        private set

    init {
        if (habitId != null) loadHabit(habitId)
    }

    private fun loadHabit(id: String) {
        viewModelScope.launch {
            repository.habit(id)
                .onSuccess { habit ->
                    state = state.copy(
                        loading = false,
                        name = habit.name,
                        icon = habit.icon,
                        type = habit.type,
                        target = habit.targetValue.toInt().coerceAtLeast(1),
                        unit = habit.unit,
                        days = habit.days.toSet(),
                        reminderEnabled = habit.reminderTime != null,
                        reminderTime = habit.reminderTime?.let { runCatching { LocalTime.parse(it) }.getOrNull() } ?: LocalTime.of(7, 0),
                    )
                }
                .onFailure { state = state.copy(loading = false, error = it.message) }
        }
    }

    fun onName(value: String) { state = state.copy(name = value.take(60), error = null) }
    fun onIcon(value: String) { state = state.copy(icon = value) }
    fun onUnit(value: String) { state = state.copy(unit = value.take(30)) }
    fun onReminderEnabled(value: Boolean) { state = state.copy(reminderEnabled = value) }
    fun onReminderTime(value: LocalTime) { state = state.copy(reminderTime = value) }

    fun onType(value: String) {
        if (state.isEditing) return
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
        val reminder = if (state.reminderEnabled) "%02d:%02d:00".format(state.reminderTime.hour, state.reminderTime.minute) else null

        viewModelScope.launch {
            val result = if (habitId == null) {
                repository.create(
                    CreateHabitRequest(
                        name = state.name.trim(),
                        icon = state.icon,
                        type = state.type,
                        targetValue = state.target.toDouble(),
                        unit = state.unit.trim(),
                        days = state.days.sorted(),
                        reminderTime = reminder,
                    ),
                )
            } else {
                repository.update(
                    habitId,
                    UpdateHabitRequest(
                        name = state.name.trim(),
                        icon = state.icon,
                        targetValue = state.target.toDouble(),
                        unit = state.unit.trim(),
                        days = state.days.sorted(),
                        reminderTime = reminder,
                    ),
                )
            }
            result
                .onSuccess { state = state.copy(saving = false, saved = true) }
                .onFailure { state = state.copy(saving = false, error = it.message) }
        }
    }
}
