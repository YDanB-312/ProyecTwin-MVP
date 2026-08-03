package com.example.proyectwin.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.proyectwin.data.model.Notification
import com.example.proyectwin.data.model.Project
import com.example.proyectwin.data.repository.NotificationsRepository
import com.example.proyectwin.data.repository.ProjectsRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed class DashboardUiState {
    data object Loading : DashboardUiState()
    data class Success(
        val projects: List<Project>,
        val notifications: List<Notification> = emptyList(),
        val unreadCount: Int = 0
    ) : DashboardUiState()
    data class Error(val message: String) : DashboardUiState()
}

class DashboardViewModel : ViewModel() {
    private val projectsRepository = ProjectsRepository()
    private val notificationsRepository = NotificationsRepository()

    private val _uiState = MutableStateFlow<DashboardUiState>(DashboardUiState.Loading)
    val uiState: StateFlow<DashboardUiState> = _uiState.asStateFlow()

    fun loadStudentDashboard(studentId: Int) {
        viewModelScope.launch {
            _uiState.value = DashboardUiState.Loading
            try {
                projectsRepository.getProjectsByStudent(studentId).collect { projects ->
                    _uiState.value = withProjects(projects)
                }
            } catch (e: Exception) {
                _uiState.value = DashboardUiState.Error(e.message ?: "Error al cargar el dashboard")
            }
        }
    }

    fun loadInstructorDashboard(instructorId: Int) {
        viewModelScope.launch {
            _uiState.value = DashboardUiState.Loading
            try {
                projectsRepository.getProjectsByInstructor(instructorId).collect { projects ->
                    _uiState.value = withProjects(projects)
                }
            } catch (e: Exception) {
                _uiState.value = DashboardUiState.Error(e.message ?: "Error al cargar el dashboard")
            }
        }
    }

    fun loadAdminDashboard() {
        viewModelScope.launch {
            _uiState.value = DashboardUiState.Loading
            try {
                projectsRepository.getAllProjects().collect { projects ->
                    _uiState.value = withProjects(projects)
                }
            } catch (e: Exception) {
                _uiState.value = DashboardUiState.Error(e.message ?: "Error al cargar el dashboard")
            }
        }
    }

    fun loadNotifications(userId: Int) {
        viewModelScope.launch {
            try {
                notificationsRepository.getNotificationsByUser(userId).collect { notifications ->
                    val current = _uiState.value
                    if (current is DashboardUiState.Success) {
                        _uiState.value = current.copy(notifications = notifications)
                    }
                }
            } catch (e: Exception) {
                _uiState.value = DashboardUiState.Error(e.message ?: "Error al cargar notificaciones")
            }
        }
    }

    fun loadUnreadCount(userId: Int) {
        viewModelScope.launch {
            try {
                notificationsRepository.getUnreadCount(userId).collect { count ->
                    val current = _uiState.value
                    if (current is DashboardUiState.Success) {
                        _uiState.value = current.copy(unreadCount = count)
                    }
                }
            } catch (e: Exception) {
                _uiState.value = DashboardUiState.Error(e.message ?: "Error al cargar notificaciones")
            }
        }
    }

    fun refresh() {
        val current = _uiState.value
        if (current is DashboardUiState.Success) {
            _uiState.value = current.copy()
        }
    }

    private fun withProjects(projects: List<Project>): DashboardUiState {
        val current = _uiState.value
        return if (current is DashboardUiState.Success) {
            current.copy(projects = projects)
        } else {
            DashboardUiState.Success(projects)
        }
    }
}
