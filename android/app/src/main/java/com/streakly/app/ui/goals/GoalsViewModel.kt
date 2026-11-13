package com.streakly.app.ui.goals

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.streakly.app.data.GoalRepository
import com.streakly.app.data.remote.GoalDto
import com.streakly.app.data.remote.GoalStatus
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

data class GoalsUiState(
    val loading: Boolean = true,
    val error: String? = null,
    val goals: List<GoalDto> = emptyList(),
    val expanded: Set<String> = emptySet(),
    val message: String? = null,
    /** Criterio 1: texto en titulo o descripcion. */
    val search: String = "",
    /** Criterio 2: All / Active / Completed. */
    val status: String = GoalStatus.ALL,
) {
    val hasFilters: Boolean get() = search.isNotBlank() || status != GoalStatus.ALL
}

class GoalsViewModel(private val repository: GoalRepository) : ViewModel() {

    var state by mutableStateOf(GoalsUiState())
        private set

    private var searchJob: Job? = null
    private var firstLoad = true

    fun load() {
        if (state.goals.isEmpty() && !state.hasFilters) state = state.copy(loading = true, error = null)
        viewModelScope.launch {
            repository.goals(state.search, state.status)
                .onSuccess { goals ->
                    // La primera vez, el primer goal arranca expandido como en el diseño.
                    val expanded = if (firstLoad && goals.isNotEmpty()) setOf(goals.first().id) else state.expanded
                    firstLoad = false
                    state = state.copy(loading = false, error = null, goals = goals, expanded = expanded)
                }
                .onFailure { state = state.copy(loading = false, error = it.message) }
        }
    }

    fun onSearch(value: String) {
        state = state.copy(search = value.take(100))
        // Espera a que el usuario deje de escribir para no hacer una peticion por letra.
        searchJob?.cancel()
        searchJob = viewModelScope.launch {
            delay(350)
            load()
        }
    }

    fun onStatus(value: String) {
        if (value == state.status) return
        state = state.copy(status = value)
        load()
    }

    fun clearFilters() {
        searchJob?.cancel()
        state = state.copy(search = "", status = GoalStatus.ALL)
        load()
    }

    fun toggleExpanded(goalId: String) {
        val expanded = if (goalId in state.expanded) state.expanded - goalId else state.expanded + goalId
        state = state.copy(expanded = expanded)
    }

    fun toggleTask(goalId: String, taskId: String) {
        // Optimista: marca la meta al instante.
        state = state.copy(goals = state.goals.map { goal ->
            if (goal.id != goalId) goal else {
                val tasks = goal.tasks.map { if (it.id == taskId) it.copy(isCompleted = !it.isCompleted) else it }
                val done = tasks.count { it.isCompleted }
                goal.copy(tasks = tasks, completedTasks = done, progress = if (tasks.isEmpty()) 0 else done * 100 / tasks.size)
            }
        })
        viewModelScope.launch {
            repository.toggleTask(goalId, taskId)
                .onSuccess { updated ->
                    state = state.copy(goals = state.goals.map { if (it.id == updated.id) updated else it })
                    // Con filtro por estado, un goal puede dejar de cumplirlo al marcar su ultima meta.
                    if (state.status != GoalStatus.ALL) load()
                }
                .onFailure { state = state.copy(message = it.message); load() }
        }
    }

    fun delete(goalId: String) {
        viewModelScope.launch {
            repository.delete(goalId)
                .onSuccess { state = state.copy(goals = state.goals.filterNot { it.id == goalId }, message = "Goal eliminado") }
                .onFailure { state = state.copy(message = it.message) }
        }
    }

    fun messageShown() { state = state.copy(message = null) }
}
