package com.streakly.app.ui.today

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.streakly.app.data.HabitRepository
import com.streakly.app.data.local.UiPreferences
import com.streakly.app.data.local.UiPrefs
import kotlinx.coroutines.flow.Flow
import com.streakly.app.data.remote.HabitType
import com.streakly.app.data.remote.TodayDto
import com.streakly.app.data.remote.TodayHabitDto
import kotlinx.coroutines.launch

data class TodayUiState(
    val loading: Boolean = true,
    val data: TodayDto? = null,
    val error: String? = null,
    val message: String? = null,
)

class TodayViewModel(
    private val repository: HabitRepository,
    uiPreferences: UiPreferences,
) : ViewModel() {

    /** Preferencias de interfaz (ocultar cumplidos / ordenar por nombre). */
    val prefs: Flow<UiPrefs> = uiPreferences.prefs

    var state by mutableStateOf(TodayUiState())
        private set

    fun load(showSpinner: Boolean = state.data == null) {
        if (showSpinner) state = state.copy(loading = true, error = null)
        viewModelScope.launch {
            repository.today()
                .onSuccess { state = state.copy(loading = false, data = it, error = null) }
                .onFailure { state = state.copy(loading = false, error = it.message) }
        }
    }

    fun toggle(habit: TodayHabitDto) {
        val newValue = if (habit.completed) 0.0 else 1.0
        updateValue(habit, newValue)
    }

    fun increment(habit: TodayHabitDto) = updateValue(habit, habit.value + 1)

    fun decrement(habit: TodayHabitDto) {
        if (habit.value <= 0) return
        updateValue(habit, habit.value - 1)
    }

    /** Actualizacion optimista: cambia la UI al instante y luego sincroniza con el backend. */
    private fun updateValue(habit: TodayHabitDto, value: Double) {
        val current = state.data ?: return
        val completed = when (habit.type) {
            HabitType.COUNTER -> value >= habit.targetValue
            else -> value >= 1
        }
        val streakDelta = when {
            completed && !habit.completed -> 1
            !completed && habit.completed -> -1
            else -> 0
        }
        val updated = habit.copy(value = value, completed = completed, currentStreak = (habit.currentStreak + streakDelta).coerceAtLeast(0))
        val habits = current.habits.map { if (it.id == habit.id) updated else it }
        state = state.copy(data = current.copy(habits = habits, completed = habits.count { it.completed }))

        viewModelScope.launch {
            repository.setTodayValue(habit.id, value)
                .onSuccess { load(showSpinner = false) }
                .onFailure {
                    state = state.copy(message = it.message)
                    load(showSpinner = false)
                }
        }
    }

    fun delete(habit: TodayHabitDto) {
        viewModelScope.launch {
            repository.delete(habit.id)
                .onSuccess { state = state.copy(message = "Hábito eliminado"); load(showSpinner = false) }
                .onFailure { state = state.copy(message = it.message) }
        }
    }

    fun messageShown() { state = state.copy(message = null) }
}
