package com.example.proyectwin.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.proyectwin.data.model.Notification
import com.example.proyectwin.domain.repository.NotificationsRepository
import com.example.proyectwin.domain.usecase.MarkAllNotificationsReadUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed class NotificationsUiState {
    data object Loading : NotificationsUiState()
    data class Success(val notifications: List<Notification>) : NotificationsUiState()
    data class Error(val message: String) : NotificationsUiState()
}

/**
 * Versión global de la bandeja: cada vez que cambia (carga, marca leída,
 * eliminación) la UI escucha el contador de no leídas (badge de SenaTopBar)
 * se recalcula sin tener que salir de la pantalla.
 */
object NotificationsVersion {
    private val _version = MutableStateFlow(0)
    val version: StateFlow<Int> = _version.asStateFlow()

    fun bump() { _version.value++ }
}

@HiltViewModel
class NotificationsViewModel @Inject constructor(
    private val notificationsRepository: NotificationsRepository,
    private val markAllNotificationsRead: MarkAllNotificationsReadUseCase,
) : ViewModel() {

    private val _uiState = MutableStateFlow<NotificationsUiState>(NotificationsUiState.Loading)
    val uiState: StateFlow<NotificationsUiState> = _uiState.asStateFlow()

    private var idUsuario: Int? = null

    fun load(userId: Int) {
        idUsuario = userId
        fetch()
    }

    fun refresh() {
        if (idUsuario != null) fetch()
    }

    fun marcarComoLeida(notificacion: Notification) {
        viewModelScope.launch {
            notificationsRepository.marcarComoLeida(notificacion).fold(
                onSuccess = { if (idUsuario != null) fetch() },
                onFailure = { },
            )
        }
    }

    fun marcarTodasComoLeidas() {
        val userId = idUsuario ?: return
        viewModelScope.launch {
            markAllNotificationsRead(userId)
            fetch()
        }
    }

    private val _mensaje = MutableStateFlow<String?>(null)
    val mensaje: StateFlow<String?> = _mensaje.asStateFlow()

    /** Eliminar solo está permitido al admin (el backend lo valida). */
    fun eliminar(id: Int) {
        viewModelScope.launch {
            notificationsRepository.eliminar(id).fold(
                onSuccess = {
                    _mensaje.value = "Notificación eliminada."
                    fetch()
                },
                onFailure = { e -> _mensaje.value = e.message ?: "No se pudo eliminar la notificación." },
            )
        }
    }

    fun limpiarMensaje() {
        _mensaje.value = null
    }

    private fun fetch() {
        val userId = idUsuario ?: return
        viewModelScope.launch {
            notificationsRepository.listar(userId).fold(
                onSuccess = { lista ->
                    _uiState.value = NotificationsUiState.Success(lista)
                    NotificationsVersion.bump()
                },
                onFailure = { e ->
                    _uiState.value = NotificationsUiState.Error(
                        e.message ?: "Error al cargar las notificaciones",
                    )
                },
            )
        }
    }
}
