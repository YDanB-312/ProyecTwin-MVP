package com.example.proyectwin.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.proyectwin.data.local.SessionManager
import com.example.proyectwin.data.model.GeneralUser
import com.example.proyectwin.data.model.UserRole
import com.example.proyectwin.domain.repository.AuthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed class AuthUiState {
    data object Loading : AuthUiState()
    data object LoggedOut : AuthUiState()
    data class LoggedIn(val user: GeneralUser) : AuthUiState()
    /** Registro creado (sin sesión): la UI va a la pantalla de login. */
    data object Registered : AuthUiState()
    data class Error(val message: String) : AuthUiState()
}

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val sessionManager: SessionManager,
) : ViewModel() {

    private val _uiState = MutableStateFlow<AuthUiState>(AuthUiState.Loading)
    val uiState: StateFlow<AuthUiState> = _uiState.asStateFlow()

    private val _isSubmitting = MutableStateFlow(false)
    val isSubmitting: StateFlow<Boolean> = _isSubmitting.asStateFlow()

    init {
        viewModelScope.launch {
            authRepository.currentUser.collect { user ->
                if (user != null) {
                    _uiState.value = AuthUiState.LoggedIn(user)
                } else if (_uiState.value != AuthUiState.Registered) {
                    _uiState.value = AuthUiState.LoggedOut
                }
            }
        }
    }

    fun login(email: String, password: String) {
        viewModelScope.launch {
            _isSubmitting.value = true
            authRepository.login(email, password).fold(
                onSuccess = { user ->
                    _uiState.value = AuthUiState.LoggedIn(user)
                },
                onFailure = { e ->
                    _uiState.value = AuthUiState.Error(e.message ?: "Error de conexión")
                },
            )
            _isSubmitting.value = false
        }
    }

    fun register(name: String, email: String, password: String, role: String = UserRole.APRENDIZ.value) {
        viewModelScope.launch {
            _isSubmitting.value = true
            authRepository.register(name, email, password, role).fold(
                onSuccess = {
                    // Como el frontend: la cuenta se crea y luego se inicia sesión.
                    authRepository.login(email, password).fold(
                        onSuccess = { user -> _uiState.value = AuthUiState.LoggedIn(user) },
                        onFailure = { _uiState.value = AuthUiState.Registered },
                    )
                },
                onFailure = { e ->
                    _uiState.value = AuthUiState.Error(e.message ?: "Error de registro")
                },
            )
            _isSubmitting.value = false
        }
    }

    fun logout() {
        viewModelScope.launch {
            authRepository.logout()
            _uiState.value = AuthUiState.LoggedOut
        }
    }

    fun clearError() {
        if (_uiState.value is AuthUiState.Error) {
            _uiState.value = AuthUiState.LoggedOut
        }
    }

    suspend fun getToken(): String? = sessionManager.getToken()

    fun getSessionManager(): SessionManager = sessionManager
}
