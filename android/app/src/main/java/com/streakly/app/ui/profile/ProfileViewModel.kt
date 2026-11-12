package com.streakly.app.ui.profile

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.streakly.app.data.AuthRepository
import com.streakly.app.data.HabitRepository
import com.streakly.app.data.remote.SummaryDto
import com.streakly.app.data.remote.UserDto
import kotlinx.coroutines.async
import kotlinx.coroutines.launch

data class ProfileUiState(
    val loading: Boolean = true,
    val user: UserDto? = null,
    val summary: SummaryDto? = null,
    val error: String? = null,
)

class ProfileViewModel(
    private val authRepository: AuthRepository,
    private val habitRepository: HabitRepository,
) : ViewModel() {

    var state by mutableStateOf(ProfileUiState())
        private set

    fun load() {
        viewModelScope.launch {
            val user = async { authRepository.me() }
            val summary = async { habitRepository.summary() }
            val userResult = user.await()
            val summaryResult = summary.await()
            state = ProfileUiState(
                loading = false,
                user = userResult.getOrNull(),
                summary = summaryResult.getOrNull(),
                error = (userResult.exceptionOrNull() ?: summaryResult.exceptionOrNull())?.message,
            )
        }
    }

    fun logout() {
        viewModelScope.launch { authRepository.logout() }
    }
}
