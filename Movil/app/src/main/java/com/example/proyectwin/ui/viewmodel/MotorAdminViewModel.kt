package com.example.proyectwin.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.proyectwin.data.model.MotorConfig
import com.example.proyectwin.domain.repository.MotorRepository
import com.example.proyectwin.domain.repository.SimilaritiesRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class MotorAdminData(
    val config: MotorConfig,
    val totalPares: Int = 0,
)

sealed class MotorAdminUiState {
    data object Loading : MotorAdminUiState()
    data class Success(val data: MotorAdminData) : MotorAdminUiState()
    data class Error(val message: String) : MotorAdminUiState()
}

sealed class MotorAdminAction {
    data object Idle : MotorAdminAction()
    data object Loading : MotorAdminAction()
    data class Message(val text: String) : MotorAdminAction()
    data class Error(val message: String) : MotorAdminAction()
}

/** Configuración del motor de similitud y recálculo global (admin). */
@HiltViewModel
class MotorAdminViewModel @Inject constructor(
    private val motorRepository: MotorRepository,
    private val similaritiesRepository: SimilaritiesRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow<MotorAdminUiState>(MotorAdminUiState.Loading)
    val uiState: StateFlow<MotorAdminUiState> = _uiState.asStateFlow()

    private val _accion = MutableStateFlow<MotorAdminAction>(MotorAdminAction.Idle)
    val accion: StateFlow<MotorAdminAction> = _accion.asStateFlow()

    fun resetAccion() {
        _accion.value = MotorAdminAction.Idle
    }

    fun load() {
        viewModelScope.launch {
            _uiState.value = MotorAdminUiState.Loading
            val config = motorRepository.obtenerConfig().getOrElse { e ->
                _uiState.value = MotorAdminUiState.Error(e.message ?: "No se pudo cargar la configuración.")
                return@launch
            }
            val total = similaritiesRepository.listar().getOrDefault(emptyList()).size
            _uiState.value = MotorAdminUiState.Success(MotorAdminData(config = config, totalPares = total))
        }
    }

    fun guardar(umbral: Double, meses: Int) {
        ejecutar { motorRepository.actualizarConfig(umbral, meses) }
    }

    fun recalcular() {
        if (_accion.value is MotorAdminAction.Loading) return
        viewModelScope.launch {
            _accion.value = MotorAdminAction.Loading
            similaritiesRepository.recalcular().fold(
                onSuccess = { resultado ->
                    _accion.value = MotorAdminAction.Message(
                        "Recálculo: ${resultado.eliminadas} archivadas, ${resultado.creadas} nuevas.",
                    )
                    load()
                },
                onFailure = { e ->
                    _accion.value = MotorAdminAction.Error(e.message ?: "No se pudo recalcular el motor.")
                },
            )
        }
    }

    private fun ejecutar(bloque: suspend () -> Result<*>) {
        if (_accion.value is MotorAdminAction.Loading) return // doble toque
        viewModelScope.launch {
            _accion.value = MotorAdminAction.Loading
            bloque().fold(
                onSuccess = {
                    _accion.value = MotorAdminAction.Message("Configuración guardada.")
                    load()
                },
                onFailure = { e ->
                    _accion.value = MotorAdminAction.Error(e.message ?: "No se pudo guardar la configuración.")
                },
            )
        }
    }
}
