package com.example.proyectwin.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.proyectwin.data.model.Notification
import com.example.proyectwin.data.model.Project
import com.example.proyectwin.data.model.Similarity
import com.example.proyectwin.domain.repository.NotificationsRepository
import com.example.proyectwin.domain.repository.ProjectsRepository
import com.example.proyectwin.domain.repository.SimilaritiesRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
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
        val unreadCount: Int = 0,
        val similarities: List<Similarity> = emptyList(),
    ) : DashboardUiState()
    data class Error(val message: String) : DashboardUiState()
}

/**
 * Dashboards de aprendiz/instructor/admin. `GET /projects` ya viene acotado por
 * rol del lado del backend (`paraUsuario`), por lo que los tres flujos cargan
 * el mismo listado y el servidor devuelve lo que corresponde.
 *
 * Las similitudes y las notificaciones se cargan en paralelo y su fallo no
 * tumba el dashboard: se conservan listas vacías y el estado sigue siendo útil.
 */
@HiltViewModel
class DashboardViewModel @Inject constructor(
    private val projectsRepository: ProjectsRepository,
    private val notificationsRepository: NotificationsRepository,
    private val similaritiesRepository: SimilaritiesRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow<DashboardUiState>(DashboardUiState.Loading)
    val uiState: StateFlow<DashboardUiState> = _uiState.asStateFlow()

    private var ultimaCarga: (() -> Unit)? = null

    fun loadStudentDashboard(studentId: Int) = cargarProyectos(studentId)

    fun loadInstructorDashboard(instructorId: Int) = cargarProyectos(instructorId)

    fun loadAdminDashboard() = cargarProyectos(null)

    private fun cargarProyectos(idUsuario: Int?) {
        ultimaCarga = { cargarProyectos(idUsuario) }
        viewModelScope.launch {
            _uiState.value = DashboardUiState.Loading
            coroutineScope {
                val proyectosDeferred = async { projectsRepository.listar() }
                val notificacionesDeferred = async { notificationsRepository.listar(idUsuario) }
                val similitudesDeferred = async { similaritiesRepository.listar() }

                val notificaciones = notificacionesDeferred.await().getOrElse { emptyList() }
                val similitudes = similitudesDeferred.await().getOrElse { emptyList() }

                proyectosDeferred.await().fold(
                    onSuccess = { proyectos ->
                        _uiState.value = DashboardUiState.Success(
                            projects = proyectos,
                            notifications = notificaciones,
                            unreadCount = notificaciones.count { !it.leido },
                            similarities = similitudes,
                        )
                    },
                    onFailure = { e ->
                        _uiState.value = DashboardUiState.Error(
                            e.message ?: "Error al cargar el dashboard",
                        )
                    },
                )
            }
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
}
