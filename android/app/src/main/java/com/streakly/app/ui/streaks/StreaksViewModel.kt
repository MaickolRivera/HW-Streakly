package com.streakly.app.ui.streaks

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.streakly.app.data.HabitRepository
import com.streakly.app.data.remote.ComplianceDto
import com.streakly.app.data.remote.HabitDto
import com.streakly.app.data.remote.HabitStatsDto
import kotlinx.coroutines.async
import kotlinx.coroutines.launch

const val HEATMAP_WEEKS = 18

data class StreaksUiState(
    val loading: Boolean = true,
    val error: String? = null,
    val habits: List<HabitDto> = emptyList(),
    val selectedId: String? = null,
    val stats: HabitStatsDto? = null,
    val statsLoading: Boolean = false,
    val compliance: List<ComplianceDto> = emptyList(),
)

class StreaksViewModel(private val repository: HabitRepository) : ViewModel() {

    var state by mutableStateOf(StreaksUiState())
        private set

    fun load() {
        if (state.habits.isEmpty()) state = state.copy(loading = true, error = null)
        viewModelScope.launch {
            val habitsCall = async { repository.habits() }
            val complianceCall = async { repository.compliance(30) }
            val habits = habitsCall.await()
            val compliance = complianceCall.await()

            habits.onFailure { state = state.copy(loading = false, error = it.message); return@launch }
            val list = habits.getOrDefault(emptyList())
            val selected = state.selectedId?.takeIf { id -> list.any { it.id == id } } ?: list.firstOrNull()?.id
            state = state.copy(
                loading = false,
                error = null,
                habits = list,
                selectedId = selected,
                compliance = compliance.getOrDefault(emptyList()),
            )
            selected?.let { loadStats(it) } ?: run { state = state.copy(stats = null) }
        }
    }

    fun select(habitId: String) {
        if (habitId == state.selectedId) return
        state = state.copy(selectedId = habitId)
        loadStats(habitId)
    }

    private fun loadStats(habitId: String) {
        state = state.copy(statsLoading = true)
        viewModelScope.launch {
            repository.stats(habitId, HEATMAP_WEEKS)
                .onSuccess { if (state.selectedId == habitId) state = state.copy(stats = it, statsLoading = false) }
                .onFailure { state = state.copy(statsLoading = false, error = it.message) }
        }
    }
}
