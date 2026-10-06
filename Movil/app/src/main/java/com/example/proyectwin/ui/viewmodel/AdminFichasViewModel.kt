package com.example.proyectwin.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.proyectwin.data.model.Ficha
import com.example.proyectwin.domain.repository.FichasRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed class AdminFichasUiState {
    data object Loading : AdminFichasUiState()
    data class Success(val fichas: List<Ficha>) : AdminFichasUiState()
    data class Error(val message: String) : AdminFichasUiState()
}

sealed class AdminFichaAction {
    data object Idle : AdminFichaAction()
    data object Loading : AdminFichaAction()
    data class Message(val text: String) : AdminFichaAction()
    data class Error(val message: String) : AdminFichaAction()
}

/** Listado admin de fichas, roster bajo demanda, PDF y eliminación/anulación. */
@HiltViewModel
class AdminFichasViewModel @Inject constructor(
    private val fichasRepository: FichasRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow<AdminFichasUiState>(AdminFichasUiState.Loading)
    val uiState: StateFlow<AdminFichasUiState> = _uiState.asStateFlow()

    private val _accion = MutableStateFlow<AdminFichaAction>(AdminFichaAction.Idle)
    val accion: StateFlow<AdminFichaAction> = _accion.asStateFlow()

    /** Detalle por ficha (roster) cargado bajo demanda. */
    private val _detalles = MutableStateFlow<Map<Int, Ficha>>(emptyMap())
    val detalles: StateFlow<Map<Int, Ficha>> = _detalles.asStateFlow()

    fun cargarDetalle(fichaId: Int) {
        if (_detalles.value.containsKey(fichaId)) return
        viewModelScope.launch {
            fichasRepository.obtener(fichaId).onSuccess { ficha ->
                _detalles.value = _detalles.value + (fichaId to ficha)
            }
        }
    }

    fun resetAccion() {
        _accion.value = AdminFichaAction.Idle
    }

    fun load() {
        viewModelScope.launch {
            _uiState.value = AdminFichasUiState.Loading
            _detalles.value = emptyMap()
            fichasRepository.listar().fold(
                onSuccess = { _uiState.value = AdminFichasUiState.Success(it) },
                onFailure = { e ->
                    _uiState.value = AdminFichasUiState.Error(e.message ?: "No se pudieron cargar las fichas.")
                },
            )
        }
    }

    /**
     * Recarga sin pantalla de carga y conservando el scroll: se usa al volver
     * de editar una ficha para reflejar altas/bajas sin saltar al inicio.
     */
    fun refrescar() {
        viewModelScope.launch {
            _detalles.value = emptyMap()
            fichasRepository.listar().onSuccess { fichas ->
                _uiState.value = AdminFichasUiState.Success(fichas)
            }
        }
    }

    fun eliminar(fichaId: Int) {
        if (_accion.value is AdminFichaAction.Loading) return
        viewModelScope.launch {
            _accion.value = AdminFichaAction.Loading
            fichasRepository.eliminar(fichaId).fold(
                onSuccess = { resultado ->
                    _accion.value = AdminFichaAction.Message(
                        if (resultado.anulada) "La ficha tenía dependencias: quedó anulada." else "Ficha eliminada.",
                    )
                    load()
                },
                onFailure = { e ->
                    _accion.value = AdminFichaAction.Error(e.message ?: "No se pudo eliminar la ficha.")
                },
            )
        }
    }

    /** Bytes del PDF de credenciales de la ficha (solo admin). */
    fun credencialesPdf(fichaId: Int, onResult: (Result<ByteArray>) -> Unit) {
        viewModelScope.launch { onResult(fichasRepository.descargarCredencialesPdf(fichaId)) }
    }
}
