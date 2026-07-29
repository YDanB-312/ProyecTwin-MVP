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

data class DashboardUiState(
    val isLoading: Boolean = true,
    val projects: List<Project> = emptyList(),
    val notifications: List<Notification> = emptyList(),
    val unreadCount: Int = 0,
    val error: String? = null
)

class DashboardViewModel : ViewModel() {
    private val projectsRepository = ProjectsRepository()
    private val notificationsRepository = NotificationsRepository()

    private val _uiState = MutableStateFlow(DashboardUiState())
    val uiState: StateFlow<DashboardUiState> = _uiState.asStateFlow()

    fun loadStudentDashboard(studentId: Int) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            projectsRepository.getProjectsByStudent(studentId).collect { projects ->
                _uiState.value = _uiState.value.copy(projects = projects, isLoading = false)
            }
        }
    }

    fun loadInstructorDashboard(instructorId: Int) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            projectsRepository.getProjectsByInstructor(instructorId).collect { projects ->
                _uiState.value = _uiState.value.copy(projects = projects, isLoading = false)
            }
        }
    }

    fun loadAdminDashboard() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            projectsRepository.getAllProjects().collect { projects ->
                _uiState.value = _uiState.value.copy(projects = projects, isLoading = false)
            }
        }
    }

    fun loadNotifications(userId: Int) {
        viewModelScope.launch {
            notificationsRepository.getNotificationsByUser(userId).collect { notifications ->
                _uiState.value = _uiState.value.copy(notifications = notifications)
            }
        }
    }

    fun loadUnreadCount(userId: Int) {
        viewModelScope.launch {
            notificationsRepository.getUnreadCount(userId).collect { count ->
                _uiState.value = _uiState.value.copy(unreadCount = count)
            }
        }
    }

    fun refresh() {
        val current = _uiState.value
        _uiState.value = current.copy(isLoading = true)
        _uiState.value = current.copy(isLoading = false)
    }
}
