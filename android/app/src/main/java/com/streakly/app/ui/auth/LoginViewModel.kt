package com.streakly.app.ui.auth

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.streakly.app.data.AuthRepository
import kotlinx.coroutines.launch

data class LoginUiState(
    val email: String = "",
    val password: String = "",
    val isRegister: Boolean = false,
    val loading: Boolean = false,
    val error: String? = null,
)

class LoginViewModel(private val repository: AuthRepository) : ViewModel() {

    var state by mutableStateOf(LoginUiState())
        private set

    fun onEmail(value: String) { state = state.copy(email = value, error = null) }
    fun onPassword(value: String) { state = state.copy(password = value, error = null) }
    fun toggleMode() { state = state.copy(isRegister = !state.isRegister, error = null) }

    fun submit() {
        val email = state.email.trim()
        when {
            !email.contains("@") || email.length < 5 -> { state = state.copy(error = "Escribe un correo válido."); return }
            state.isRegister && state.password.length < 8 -> { state = state.copy(error = "La contraseña debe tener al menos 8 caracteres."); return }
            state.password.isEmpty() -> { state = state.copy(error = "Escribe tu contraseña."); return }
        }

        state = state.copy(loading = true, error = null)
        viewModelScope.launch {
            val result = if (state.isRegister) repository.register(email, state.password)
            else repository.login(email, state.password)
            // Si sale bien, TokenStore emite el token y MainActivity cambia a la app.
            state = state.copy(loading = false, error = result.exceptionOrNull()?.message)
        }
    }
}
