package com.example.proyectwin.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.proyectwin.data.model.ClassGroupStatus
import com.example.proyectwin.data.model.Ficha
import com.example.proyectwin.domain.repository.FichasRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

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

@HiltViewModel
class FichasViewModel @Inject constructor(
    private val fichasRepository: FichasRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow<FichasUiState>(FichasUiState.Loading)
    val uiState: StateFlow<FichasUiState> = _uiState.asStateFlow()

    private val _actionState = MutableStateFlow(FichasActionState())
    val actionState: StateFlow<FichasActionState> = _actionState.asStateFlow()

    fun loadAllFichas() {
        viewModelScope.launch {
            _uiState.value = FichasUiState.Loading
            fichasRepository.listar().fold(
                onSuccess = { fichas -> _uiState.value = FichasUiState.Success(fichas) },
                onFailure = { e ->
                    _uiState.value = FichasUiState.Error(e.message ?: "Error al cargar las fichas")
                },
            )
        }
    }

    fun loadActiveFichas() {
        viewModelScope.launch {
            _uiState.value = FichasUiState.Loading
            fichasRepository.listar().fold(
                onSuccess = { fichas ->
                    _uiState.value = FichasUiState.Success(
                        fichas.filter { it.estado == ClassGroupStatus.ACTIVO.value },
                    )
                },
                onFailure = { e ->
                    _uiState.value = FichasUiState.Error(e.message ?: "Error al cargar las fichas")
                },
            )
        }
    }

    fun loadFichaById(id: Int) {
        viewModelScope.launch {
            _uiState.value = FichasUiState.Loading
            fichasRepository.obtener(id).fold(
                onSuccess = { ficha ->
                    _actionState.value = _actionState.value.copy(selectedFicha = ficha)
                    _uiState.value = FichasUiState.Success(listOf(ficha))
                },
                onFailure = { e ->
                    _uiState.value = FichasUiState.Error(e.message ?: "Error al cargar la ficha")
                },
            )
        }
    }

    /** Valida el código contra el servidor (misma preview que el frontend). */
    fun validarCodigo(codigo: String) {
        viewModelScope.launch {
            _actionState.value = _actionState.value.copy(codigoValido = null, selectedFicha = null)
            fichasRepository.fichaPorCodigo(codigo).fold(
                onSuccess = { ficha ->
                    _actionState.value = _actionState.value.copy(
                        codigoValido = true,
                        selectedFicha = ficha,
                    )
                },
                onFailure = {
                    _actionState.value = _actionState.value.copy(
                        codigoValido = false,
                        selectedFicha = null,
                    )
                },
            )
        }
    }

    fun generarCodigo() {
        _actionState.value = _actionState.value.copy(codigoGenerado = Ficha.generarCodigo())
    }

    fun joinFicha(fichaId: Int) {
        viewModelScope.launch {
            val codigo = _actionState.value.selectedFicha?.codigo
            if (codigo == null) {
                _actionState.value = _actionState.value.copy(
                    message = "Valida el código de la ficha antes de unirte.",
                )
                return@launch
            }
            fichasRepository.unirmeAFicha(codigo).fold(
                onSuccess = { ficha ->
                    _actionState.value = _actionState.value.copy(
                        selectedFicha = ficha,
                        joinSuccess = true,
                        message = null,
                    )
                },
                onFailure = { e ->
                    _actionState.value = _actionState.value.copy(
                        message = e.message ?: "Error al unirse a la ficha",
                    )
                },
            )
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
