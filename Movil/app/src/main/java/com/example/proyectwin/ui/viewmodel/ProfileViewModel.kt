package com.example.proyectwin.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.proyectwin.data.mapper.dividirNombre
import com.example.proyectwin.data.model.GeneralUser
import com.example.proyectwin.domain.repository.AuthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed class ProfileUiState {
    data object Loading : ProfileUiState()
    data class Loaded(val user: GeneralUser?) : ProfileUiState()
    data class Error(val message: String) : ProfileUiState()
}

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val authRepository: AuthRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow<ProfileUiState>(ProfileUiState.Loading)
    val uiState: StateFlow<ProfileUiState> = _uiState.asStateFlow()

    private val _isSaving = MutableStateFlow(false)
    val isSaving: StateFlow<Boolean> = _isSaving.asStateFlow()

    private val _saveSuccess = MutableStateFlow(false)
    val saveSuccess: StateFlow<Boolean> = _saveSuccess.asStateFlow()

    private val _saveError = MutableStateFlow<String?>(null)
    val saveError: StateFlow<String?> = _saveError.asStateFlow()

    private val _isChangingEmail = MutableStateFlow(false)
    val isChangingEmail: StateFlow<Boolean> = _isChangingEmail.asStateFlow()

    init {
        viewModelScope.launch {
            authRepository.currentUser.collect { user ->
                _uiState.value = ProfileUiState.Loaded(user)
            }
        }
    }

    /** Solo nombre/apellido: el correo propio se cambia con [changeEmail]. */
    fun updateProfile(name: String) {
        val (nombre, apellido) = dividirNombre(name)
        updateProfileNombres(nombre, apellido)
    }

    /** Edición con campos separados (evita re-dividir nombres compuestos). */
    fun updateProfileNombres(nombre: String, apellido: String) {
        viewModelScope.launch {
            _isSaving.value = true
            _saveError.value = null
            authRepository.updateProfile(nombre, apellido).fold(
                onSuccess = {
                    _isSaving.value = false
                    _saveSuccess.value = true
                },
                onFailure = { e ->
                    _isSaving.value = false
                    _saveError.value = e.message ?: "Error al actualizar el perfil"
                },
            )
        }
    }

    fun updateFoto(fotoBase64: String?) {
        viewModelScope.launch {
            _isSaving.value = true
            _saveError.value = null
            authRepository.updateFoto(fotoBase64).fold(
                onSuccess = {
                    _isSaving.value = false
                    _saveSuccess.value = true
                },
                onFailure = { e ->
                    _isSaving.value = false
                    _saveError.value = e.message ?: "Error al actualizar la foto"
                },
            )
        }
    }

    fun changeEmail(correo: String, passwordActual: String) {
        viewModelScope.launch {
            _isChangingEmail.value = true
            _isSaving.value = true
            _saveError.value = null
            authRepository.changeEmail(correo, passwordActual).fold(
                onSuccess = {
                    _isChangingEmail.value = false
                    _isSaving.value = false
                    _saveSuccess.value = true
                },
                onFailure = { e ->
                    _isChangingEmail.value = false
                    _isSaving.value = false
                    _saveError.value = e.message ?: "Error al cambiar el correo"
                },
            )
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
