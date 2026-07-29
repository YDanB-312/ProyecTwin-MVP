package com.example.proyectwin.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.proyectwin.data.local.SessionManager
import com.example.proyectwin.data.model.Ficha
import com.example.proyectwin.data.repository.FichasRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class FichasUiState(
    val isLoading: Boolean = true,
    val fichas: List<Ficha> = emptyList(),
    val selectedFicha: Ficha? = null,
    val codigoGenerado: String? = null,
    val codigoValido: Boolean? = null,
    val joinSuccess: Boolean = false,
    val error: String? = null
)

class FichasViewModel(
    private val fichasRepository: FichasRepository = FichasRepository(),
    private val sessionManager: SessionManager? = null
) : ViewModel() {

    private val _uiState = MutableStateFlow(FichasUiState())
    val uiState: StateFlow<FichasUiState> = _uiState.asStateFlow()

    fun loadAllFichas() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            fichasRepository.getAllFichas().collect { fichas ->
                _uiState.value = _uiState.value.copy(fichas = fichas, isLoading = false)
            }
        }
    }

    fun loadActiveFichas() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            fichasRepository.getActiveFichas().collect { fichas ->
                _uiState.value = _uiState.value.copy(fichas = fichas, isLoading = false)
            }
        }
    }

    fun loadFichaById(id: Int) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            fichasRepository.getFichaById(id).collect { ficha ->
                _uiState.value = _uiState.value.copy(selectedFicha = ficha, isLoading = false)
            }
        }
    }

    fun validarCodigo(codigo: String) {
        val valido = fichasRepository.esCodigoValido(codigo)
        _uiState.value = _uiState.value.copy(codigoValido = valido)
        if (valido) {
            viewModelScope.launch {
                fichasRepository.getFichaByCodigo(codigo).collect { ficha ->
                    _uiState.value = _uiState.value.copy(selectedFicha = ficha)
                }
            }
        }
    }

    fun generarCodigo() {
        val codigo = fichasRepository.generarCodigo()
        _uiState.value = _uiState.value.copy(codigoGenerado = codigo)
    }

    fun joinFicha(fichaId: Int) {
        viewModelScope.launch {
            try {
                sessionManager?.joinFicha(fichaId)
                _uiState.value = _uiState.value.copy(joinSuccess = true)
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(error = e.message)
            }
        }
    }

    fun clearJoinSuccess() {
        _uiState.value = _uiState.value.copy(joinSuccess = false)
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }
}
