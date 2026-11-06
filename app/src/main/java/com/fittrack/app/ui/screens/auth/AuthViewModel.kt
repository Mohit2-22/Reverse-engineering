package com.fittrack.app.ui.screens.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.fittrack.app.data.repository.AuthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class AuthUiState(
    val loading: Boolean = false,
    val error: String? = null,
    val success: Boolean = false,
)

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val authRepository: AuthRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(AuthUiState())
    val uiState: StateFlow<AuthUiState> = _uiState.asStateFlow()

    fun signIn(email: String, password: String) = run(email, password, signUp = false)
    fun signUp(email: String, password: String) = run(email, password, signUp = true)

    private fun run(email: String, password: String, signUp: Boolean) {
        if (email.isBlank() || password.length < 6) {
            _uiState.update { it.copy(error = "Enter a valid email and a 6+ char password") }
            return
        }
        _uiState.update { it.copy(loading = true, error = null) }
        viewModelScope.launch {
            try {
                if (signUp) authRepository.signUp(email, password)
                else authRepository.signIn(email, password)
                _uiState.update { it.copy(loading = false, success = true) }
            } catch (e: Exception) {
                _uiState.update { it.copy(loading = false, error = e.message ?: "Auth failed") }
            }
        }
    }

    fun clearError() = _uiState.update { it.copy(error = null) }
}
