package com.example.proyectwin.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.proyectwin.data.mock.MockDataProvider
import com.example.proyectwin.data.model.BugReport
import com.example.proyectwin.data.model.GeneralUser
import com.example.proyectwin.data.model.Project
import com.example.proyectwin.data.model.Similarity
import com.example.proyectwin.data.repository.BugReportsRepository
import com.example.proyectwin.data.repository.ProjectsRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class AdminUiState(
    val isLoading: Boolean = true,
    val users: List<GeneralUser> = emptyList(),
    val projects: List<Project> = emptyList(),
    val bugReports: List<BugReport> = emptyList(),
    val similarities: List<Similarity> = emptyList(),
    val selectedUser: GeneralUser? = null,
    val selectedProject: Project? = null,
    val selectedBugReport: BugReport? = null,
    val selectedSimilarity: Similarity? = null,
    val error: String? = null
)

class AdminViewModel : ViewModel() {
    private val projectsRepository = ProjectsRepository()
    private val bugReportsRepository = BugReportsRepository()

    private val _uiState = MutableStateFlow(AdminUiState())
    val uiState: StateFlow<AdminUiState> = _uiState.asStateFlow()

    init {
        loadAll()
    }

    fun loadAll() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)

            _uiState.value = _uiState.value.copy(
                users = MockDataProvider.users,
                projects = MockDataProvider.getAllProjects(),
                bugReports = MockDataProvider.getAllBugReports(),
                similarities = MockDataProvider.getAllSimilarities(),
                isLoading = false
            )
        }
    }

    fun selectUser(id: Int) {
        _uiState.value = _uiState.value.copy(selectedUser = MockDataProvider.findUserById(id))
    }

    fun selectProject(id: Int) {
        viewModelScope.launch {
            projectsRepository.getProjectById(id).collect { project ->
                _uiState.value = _uiState.value.copy(selectedProject = project)
            }
        }
    }

    fun refresh() {
        loadAll()
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }
}
