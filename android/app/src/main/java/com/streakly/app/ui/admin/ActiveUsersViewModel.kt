package com.streakly.app.ui.admin

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.streakly.app.data.AdminRepository
import com.streakly.app.data.remote.ActiveUserDto
import kotlinx.coroutines.launch

data class ActiveUsersUiState(
    val loading: Boolean = true,
    val users: List<ActiveUserDto> = emptyList(),
    val error: String? = null,
)

class ActiveUsersViewModel(private val adminRepository: AdminRepository) : ViewModel() {

    var state by mutableStateOf(ActiveUsersUiState())
        private set

    fun load() {
        state = state.copy(loading = true, error = null)
        viewModelScope.launch {
            adminRepository.activeUsers(PERIOD_DAYS)
                .onSuccess { state = ActiveUsersUiState(loading = false, users = it) }
                .onFailure { state = ActiveUsersUiState(loading = false, error = it.message ?: "No se pudo cargar la lista.") }
        }
    }

    companion object {
        const val PERIOD_DAYS = 7
    }
}
