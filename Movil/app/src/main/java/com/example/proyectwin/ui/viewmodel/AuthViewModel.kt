package com.example.proyectwin.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.proyectwin.data.local.SessionManager
import com.example.proyectwin.data.model.GeneralUser
import com.example.proyectwin.data.model.UserRole
import com.example.proyectwin.data.repository.AuthRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed class AuthUiState {
    data object Loading : AuthUiState()
    data object LoggedOut : AuthUiState()
    data class LoggedIn(val user: GeneralUser) : AuthUiState()
    data class Error(val message: String) : AuthUiState()
}

class AuthViewModel(application: Application) : AndroidViewModel(application) {
    private val sessionManager = SessionManager(application)
    private val authRepository = AuthRepository(sessionManager)

    private val _uiState = MutableStateFlow<AuthUiState>(AuthUiState.Loading)
    val uiState: StateFlow<AuthUiState> = _uiState.asStateFlow()

    private val _isSubmitting = MutableStateFlow(false)
    val isSubmitting: StateFlow<Boolean> = _isSubmitting.asStateFlow()

    init {
        viewModelScope.launch {
            authRepository.currentUser.collect { user ->
                _uiState.value = if (user != null) AuthUiState.LoggedIn(user) else AuthUiState.LoggedOut
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
                }
            )
            _isSubmitting.value = false
        }
    }

    fun register(name: String, email: String, password: String, role: String = UserRole.APRENDIZ.value) {
        viewModelScope.launch {
            _isSubmitting.value = true
            authRepository.register(name, email, password, role).fold(
                onSuccess = { user ->
                    _uiState.value = AuthUiState.LoggedIn(user)
                },
                onFailure = { e ->
                    _uiState.value = AuthUiState.Error(e.message ?: "Error de registro")
                }
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
