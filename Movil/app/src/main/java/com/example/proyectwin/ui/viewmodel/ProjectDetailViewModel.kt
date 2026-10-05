package com.example.proyectwin.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.proyectwin.data.model.Project
import com.example.proyectwin.domain.repository.ProjectsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed class ProjectDetailUiState {
    data object Loading : ProjectDetailUiState()
    data class Success(val project: Project) : ProjectDetailUiState()
    data class Error(val message: String) : ProjectDetailUiState()
}

@HiltViewModel
class ProjectDetailViewModel @Inject constructor(
    private val projectsRepository: ProjectsRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow<ProjectDetailUiState>(ProjectDetailUiState.Loading)
    val uiState: StateFlow<ProjectDetailUiState> = _uiState.asStateFlow()

    fun loadProject(id: Int) {
        viewModelScope.launch {
            _uiState.value = ProjectDetailUiState.Loading
            projectsRepository.obtener(id).fold(
                onSuccess = { project -> _uiState.value = ProjectDetailUiState.Success(project) },
                onFailure = { e -> _uiState.value = ProjectDetailUiState.Error(e.message ?: "Error al cargar el proyecto") },
            )
        }
    }
}
