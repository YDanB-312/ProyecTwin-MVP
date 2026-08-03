package com.example.proyectwin.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.proyectwin.data.local.SessionManager
import com.example.proyectwin.data.model.GeneralUser
import com.example.proyectwin.data.repository.AuthRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed class ProfileUiState {
    data object Loading : ProfileUiState()
    data class Loaded(val user: GeneralUser?) : ProfileUiState()
    data class Error(val message: String) : ProfileUiState()
}

class ProfileViewModel(application: Application) : AndroidViewModel(application) {
    private val authRepository = AuthRepository(SessionManager(application))

    private val _uiState = MutableStateFlow<ProfileUiState>(ProfileUiState.Loading)
    val uiState: StateFlow<ProfileUiState> = _uiState.asStateFlow()

    private val _isSaving = MutableStateFlow(false)
    val isSaving: StateFlow<Boolean> = _isSaving.asStateFlow()

    private val _saveSuccess = MutableStateFlow(false)
    val saveSuccess: StateFlow<Boolean> = _saveSuccess.asStateFlow()

    private val _saveError = MutableStateFlow<String?>(null)
    val saveError: StateFlow<String?> = _saveError.asStateFlow()

    init {
        viewModelScope.launch {
            authRepository.currentUser.collect { user ->
                _uiState.value = ProfileUiState.Loaded(user)
            }
        }
    }

    fun updateProfile(name: String, email: String, telefono: String?) {
        viewModelScope.launch {
            _isSaving.value = true
            _saveError.value = null
            try {
                authRepository.updateProfile(name, email, telefono)
                _isSaving.value = false
                _saveSuccess.value = true
            } catch (e: Exception) {
                _isSaving.value = false
                _saveError.value = e.message ?: "Error al actualizar el perfil"
            }
        }
    }

    fun updateFoto(fotoBase64: String?) {
        viewModelScope.launch {
            _isSaving.value = true
            _saveError.value = null
            try {
                authRepository.updateFoto(fotoBase64)
                _isSaving.value = false
                _saveSuccess.value = true
            } catch (e: Exception) {
                _isSaving.value = false
                _saveError.value = e.message ?: "Error al actualizar la foto"
            }
        }
    }

    fun clearSaveSuccess() {
        _saveSuccess.value = false
    }

    fun clearError() {
        _saveError.value = null
        if (_uiState.value is ProfileUiState.Error) {
            _uiState.value = ProfileUiState.Loading
        }
    }
}
