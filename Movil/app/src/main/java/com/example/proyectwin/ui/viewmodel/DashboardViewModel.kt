package com.example.proyectwin.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.proyectwin.data.model.Notification
import com.example.proyectwin.data.model.Project
import com.example.proyectwin.domain.repository.NotificationsRepository
import com.example.proyectwin.domain.repository.ProjectsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed class DashboardUiState {
    data object Loading : DashboardUiState()
    data class Success(
        val projects: List<Project>,
        val notifications: List<Notification> = emptyList(),
        val unreadCount: Int = 0
    ) : DashboardUiState()
    data class Error(val message: String) : DashboardUiState()
}

/**
 * Dashboards de aprendiz/instructor/admin. `GET /projects` ya viene acotado por
 * rol del lado del backend (`paraUsuario`), por lo que los tres flujos cargan
 * el mismo listado y el servidor devuelve lo que corresponde.
 */
@HiltViewModel
class DashboardViewModel @Inject constructor(
    private val projectsRepository: ProjectsRepository,
    private val notificationsRepository: NotificationsRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow<DashboardUiState>(DashboardUiState.Loading)
    val uiState: StateFlow<DashboardUiState> = _uiState.asStateFlow()

    private var ultimaCarga: (() -> Unit)? = null

    fun loadStudentDashboard(studentId: Int) = cargarProyectos()

    fun loadInstructorDashboard(instructorId: Int) = cargarProyectos()

    fun loadAdminDashboard() = cargarProyectos()

    private fun cargarProyectos() {
        ultimaCarga = { cargarProyectos() }
        viewModelScope.launch {
            _uiState.value = DashboardUiState.Loading
            projectsRepository.listar().fold(
                onSuccess = { proyectos -> _uiState.value = conProyectos(proyectos) },
                onFailure = { e ->
                    _uiState.value = DashboardUiState.Error(e.message ?: "Error al cargar el dashboard")
                },
            )
        }
    }

    fun loadNotifications(userId: Int) {
        ultimaCarga = { loadNotifications(userId) }
        viewModelScope.launch {
            notificationsRepository.listar(userId).fold(
                onSuccess = { notificaciones ->
                    val actual = _uiState.value
                    if (actual is DashboardUiState.Success) {
                        _uiState.value = actual.copy(
                            notifications = notificaciones,
                            unreadCount = notificaciones.count { !it.leido },
                        )
                    }
                },
                // Si falla la bandeja se conserva el dashboard ya cargado.
                onFailure = { },
            )
        }
    }

    fun loadUnreadCount(userId: Int) = loadNotifications(userId)

    fun refresh() {
        ultimaCarga?.invoke()
    }

    private fun conProyectos(projects: List<Project>): DashboardUiState {
        val actual = _uiState.value
        return if (actual is DashboardUiState.Success) {
            actual.copy(projects = projects)
        } else {
            DashboardUiState.Success(projects)
        }
    }
}
