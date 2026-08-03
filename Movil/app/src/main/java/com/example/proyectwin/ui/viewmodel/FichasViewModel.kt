package com.example.proyectwin.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.proyectwin.data.local.SessionManager
import com.example.proyectwin.data.mock.MockDataProvider
import com.example.proyectwin.data.model.Ficha
import com.example.proyectwin.data.repository.FichasRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

sealed class FichasUiState {
    data object Loading : FichasUiState()
    data class Success(val fichas: List<Ficha>) : FichasUiState()
    data class Error(val message: String) : FichasUiState()
}

data class FichasActionState(
    val selectedFicha: Ficha? = null,
    val codigoValido: Boolean? = null,
    val codigoGenerado: String? = null,
    val joinSuccess: Boolean = false,
    val message: String? = null
)

class FichasViewModel(application: Application) : AndroidViewModel(application) {
    private val fichasRepository = FichasRepository()
    private val sessionManager = SessionManager(application)

    private val _uiState = MutableStateFlow<FichasUiState>(FichasUiState.Loading)
    val uiState: StateFlow<FichasUiState> = _uiState.asStateFlow()

    private val _actionState = MutableStateFlow(FichasActionState())
    val actionState: StateFlow<FichasActionState> = _actionState.asStateFlow()

    fun loadAllFichas() {
        viewModelScope.launch {
            _uiState.value = FichasUiState.Loading
            fichasRepository.getAllFichas().collect { fichas ->
                _uiState.value = FichasUiState.Success(fichas)
            }
        }
    }

    fun loadActiveFichas() {
        viewModelScope.launch {
            _uiState.value = FichasUiState.Loading
            fichasRepository.getActiveFichas().collect { fichas ->
                _uiState.value = FichasUiState.Success(fichas)
            }
        }
    }

    fun loadFichaById(id: Int) {
        viewModelScope.launch {
            _uiState.value = FichasUiState.Loading
            fichasRepository.getFichaById(id).collect { ficha ->
                _actionState.value = _actionState.value.copy(selectedFicha = ficha)
                _uiState.value = FichasUiState.Success(ficha?.let { listOf(it) } ?: emptyList())
            }
        }
    }

    fun validarCodigo(codigo: String) {
        val valido = fichasRepository.esCodigoValido(codigo)
        _actionState.value = _actionState.value.copy(codigoValido = valido, selectedFicha = null)
        if (valido) {
            viewModelScope.launch {
                fichasRepository.getFichaByCodigo(codigo).collect { ficha ->
                    _actionState.value = _actionState.value.copy(selectedFicha = ficha)
                }
            }
        }
    }

    fun generarCodigo() {
        val codigo = fichasRepository.generarCodigo()
        _actionState.value = _actionState.value.copy(codigoGenerado = codigo)
    }

    fun joinFicha(fichaId: Int) {
        viewModelScope.launch {
            try {
                sessionManager.joinFicha(fichaId)
                val ficha = MockDataProvider.findFichaById(fichaId)
                val user = sessionManager.currentUser.first()
                if (ficha != null && user != null) {
                    MockDataProvider.joinFicha(ficha.codigo, user)
                }
                _actionState.value = _actionState.value.copy(joinSuccess = true, message = null)
            } catch (e: Exception) {
                _actionState.value = _actionState.value.copy(message = e.message ?: "Error al unirse a la ficha")
            }
        }
    }

    fun clearJoinSuccess() {
        _actionState.value = _actionState.value.copy(joinSuccess = false)
    }

    fun clearError() {
        _actionState.value = _actionState.value.copy(message = null)
        if (_uiState.value is FichasUiState.Error) {
            _uiState.value = FichasUiState.Loading
        }
    }
}
