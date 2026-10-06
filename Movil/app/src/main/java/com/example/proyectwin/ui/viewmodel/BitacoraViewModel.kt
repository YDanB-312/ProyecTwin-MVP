package com.example.proyectwin.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.proyectwin.data.model.AuditLog
import com.example.proyectwin.domain.repository.AuditRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed class BitacoraUiState {
    data object Loading : BitacoraUiState()
    data class Success(val registros: List<AuditLog>) : BitacoraUiState()
    data class Error(val message: String) : BitacoraUiState()
}

/** Bitácora inmutable (`GET /audit-logs`) con filtros del backend. */
@HiltViewModel
class BitacoraViewModel @Inject constructor(
    private val auditRepository: AuditRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow<BitacoraUiState>(BitacoraUiState.Loading)
    val uiState: StateFlow<BitacoraUiState> = _uiState.asStateFlow()

    fun load(
        accion: String? = null,
        entidad: String? = null,
        idUsuario: Int? = null,
        desde: String? = null,
        hasta: String? = null,
    ) {
        viewModelScope.launch {
            _uiState.value = BitacoraUiState.Loading
            auditRepository.listar(accion, entidad, idUsuario, desde, hasta).fold(
                onSuccess = { _uiState.value = BitacoraUiState.Success(it) },
                onFailure = { e ->
                    _uiState.value = BitacoraUiState.Error(e.message ?: "No se pudo cargar la bitácora.")
                },
            )
        }
    }
}
