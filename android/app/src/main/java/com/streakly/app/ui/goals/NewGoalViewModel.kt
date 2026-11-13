package com.streakly.app.ui.goals

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.streakly.app.data.GoalRepository
import com.streakly.app.data.remote.CreateGoalRequest
import com.streakly.app.data.remote.UpdateGoalRequest
import kotlinx.coroutines.launch
import java.time.LocalDate

/** Una meta en el formulario. id = null significa que aun no existe en el servidor. */
data class TaskItem(val id: String?, val title: String)

data class NewGoalUiState(
    val title: String = "",
    val description: String = "",
    val dueDate: LocalDate? = null,
    val tasks: List<TaskItem> = emptyList(),
    val newTask: String = "",
    val isEditing: Boolean = false,
    val loading: Boolean = false,
    val saving: Boolean = false,
    val error: String? = null,
    val saved: Boolean = false,
)

/**
 * Sirve para crear (goalId = null) y para editar (goalId = id del goal).
 */
class NewGoalViewModel(
    private val repository: GoalRepository,
    private val goalId: String? = null,
) : ViewModel() {

    var state by mutableStateOf(NewGoalUiState(isEditing = goalId != null, loading = goalId != null))
        private set

    /** Metas existentes que el usuario quito en modo edicion (se borran al guardar). */
    private val removedTaskIds = mutableSetOf<String>()

    init {
        if (goalId != null) loadGoal(goalId)
    }

    private fun loadGoal(id: String) {
        viewModelScope.launch {
            repository.goal(id)
                .onSuccess { goal ->
                    state = state.copy(
                        loading = false,
                        title = goal.title,
                        description = goal.description.orEmpty(),
                        dueDate = goal.dueDate?.let { runCatching { LocalDate.parse(it) }.getOrNull() },
                        tasks = goal.tasks.map { TaskItem(it.id, it.title) },
                    )
                }
                .onFailure { state = state.copy(loading = false, error = it.message) }
        }
    }

    fun onTitle(value: String) { state = state.copy(title = value.take(200), error = null) }
    fun onDescription(value: String) { state = state.copy(description = value.take(1000)) }
    fun onDueDate(value: LocalDate?) { state = state.copy(dueDate = value) }
    fun onNewTask(value: String) { state = state.copy(newTask = value.take(200)) }

    fun addTask() {
        val task = state.newTask.trim()
        if (task.isEmpty()) return
        state = state.copy(tasks = state.tasks + TaskItem(null, task), newTask = "", error = null)
    }

    fun removeTask(index: Int) {
        state.tasks.getOrNull(index)?.id?.let { removedTaskIds += it }
        state = state.copy(tasks = state.tasks.filterIndexed { i, _ -> i != index })
    }

    fun save() {
        if (state.title.isBlank()) { state = state.copy(error = "Ponle un título al goal."); return }
        // Si quedo una meta escrita sin agregar, se incluye.
        val pending = state.newTask.trim().takeIf { it.isNotEmpty() }?.let { TaskItem(null, it) }
        val tasks = state.tasks + listOfNotNull(pending)
        val description = state.description.trim().ifEmpty { null }

        state = state.copy(saving = true, error = null)
        viewModelScope.launch {
            val result = if (goalId == null) {
                repository.create(
                    CreateGoalRequest(
                        title = state.title.trim(),
                        description = description,
                        dueDate = state.dueDate?.toString(),
                        tasks = tasks.map { it.title },
                    ),
                ).map { }
            } else {
                saveChanges(goalId, description, tasks)
            }
            result
                .onSuccess { state = state.copy(saving = false, saved = true) }
                .onFailure { state = state.copy(saving = false, error = it.message) }
        }
    }

    /** Edicion: actualiza los datos del goal, borra las metas quitadas y crea las nuevas. */
    private suspend fun saveChanges(id: String, description: String?, tasks: List<TaskItem>): Result<Unit> {
        repository.update(id, UpdateGoalRequest(state.title.trim(), description, state.dueDate?.toString()))
            .onFailure { return Result.failure(it) }
        for (taskId in removedTaskIds) {
            repository.removeTask(id, taskId).onFailure { return Result.failure(it) }
        }
        removedTaskIds.clear()
        for (task in tasks.filter { it.id == null }) {
            repository.addTask(id, task.title).onFailure { return Result.failure(it) }
        }
        return Result.success(Unit)
    }
}
