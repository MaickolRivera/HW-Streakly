package com.streakly.app.ui.goals

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.streakly.app.data.GoalRepository
import com.streakly.app.data.remote.CreateGoalRequest
import kotlinx.coroutines.launch
import java.time.LocalDate

data class NewGoalUiState(
    val title: String = "",
    val description: String = "",
    val dueDate: LocalDate? = null,
    val tasks: List<String> = emptyList(),
    val newTask: String = "",
    val saving: Boolean = false,
    val error: String? = null,
    val saved: Boolean = false,
)

class NewGoalViewModel(private val repository: GoalRepository) : ViewModel() {

    var state by mutableStateOf(NewGoalUiState())
        private set

    fun onTitle(value: String) { state = state.copy(title = value.take(200), error = null) }
    fun onDescription(value: String) { state = state.copy(description = value.take(1000)) }
    fun onDueDate(value: LocalDate?) { state = state.copy(dueDate = value) }
    fun onNewTask(value: String) { state = state.copy(newTask = value.take(200)) }

    fun addTask() {
        val task = state.newTask.trim()
        if (task.isEmpty()) return
        state = state.copy(tasks = state.tasks + task, newTask = "", error = null)
    }

    fun removeTask(index: Int) {
        state = state.copy(tasks = state.tasks.filterIndexed { i, _ -> i != index })
    }

    fun save() {
        if (state.title.isBlank()) { state = state.copy(error = "Ponle un título al goal."); return }
        // Si quedo una meta escrita sin agregar, se incluye.
        val tasks = state.tasks + listOfNotNull(state.newTask.trim().takeIf { it.isNotEmpty() })

        state = state.copy(saving = true, error = null)
        viewModelScope.launch {
            repository.create(
                CreateGoalRequest(
                    title = state.title.trim(),
                    description = state.description.trim().ifEmpty { null },
                    dueDate = state.dueDate?.toString(),
                    tasks = tasks,
                ),
            )
                .onSuccess { state = state.copy(saving = false, saved = true) }
                .onFailure { state = state.copy(saving = false, error = it.message) }
        }
    }
}
