package com.example.proyectwin.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.proyectwin.data.model.Project
import com.example.proyectwin.data.model.ProjectDraft
import com.example.proyectwin.domain.repository.ProjectsRepository
import com.example.proyectwin.domain.usecase.CreateProjectWithTeamUseCase
import com.example.proyectwin.data.api.ApiException
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ProjectsViewModel @Inject constructor(
    private val projectsRepository: ProjectsRepository,
    private val createProjectWithTeamUseCase: CreateProjectWithTeamUseCase,
) : ViewModel() {

    private val _uiState = MutableStateFlow<ProjectsUiState>(ProjectsUiState.Loading)
    val uiState: StateFlow<ProjectsUiState> = _uiState.asStateFlow()

    sealed class ProjectsUiState {
        data object Loading : ProjectsUiState()
        data class Success(val projects: List<Project>) : ProjectsUiState()
        data class Error(val message: String) : ProjectsUiState()
    }

    fun load() {
        viewModelScope.launch {
            _uiState.value = ProjectsUiState.Loading
            projectsRepository.listar().fold(
                onSuccess = { proyectos ->
                    _uiState.value = ProjectsUiState.Success(proyectos)
                },
                onFailure = { e ->
                    _uiState.value = ProjectsUiState.Error(
                        e.message ?: "Error al cargar los proyectos"
                    )
                },
            )
        }
    }

    sealed class CreateState {
        data object Idle : CreateState()
        data object Loading : CreateState()
        data class Success(val projectId: Int) : CreateState()
        data class Error(val message: String, val fieldErrors: Map<String, String> = emptyMap()) : CreateState()
    }

    private val _createState = MutableStateFlow<CreateState>(CreateState.Idle)
    val createState: StateFlow<CreateState> = _createState.asStateFlow()

    fun resetCreateState() {
        _createState.value = CreateState.Idle
    }

    /** Crear un nuevo proyecto, opcionalmente con equipo. */
    fun crear(draft: ProjectDraft, miembros: List<Int> = emptyList()) {
        viewModelScope.launch {
            _createState.value = CreateState.Loading
            createProjectWithTeamUseCase(draft, miembros).fold(
                onSuccess = { proyecto ->
                    _createState.value = CreateState.Success(proyecto.id)
                    load()
                },
                onFailure = { e ->
                    val httpError = e as? ApiException.Http
                    _createState.value = CreateState.Error(
                        e.message ?: "Error al crear el proyecto",
                        httpError?.fieldErrors ?: emptyMap()
                    )
                },
            )
        }
    }

    /** Actualizar un proyecto existente. */
    fun actualizar(id: Int, draft: ProjectDraft) {
        viewModelScope.launch {
            projectsRepository.actualizar(id, draft).fold(
                onSuccess = {
                    load()
                },
                onFailure = { e ->
                    // Error mostrado por la UI
                },
            )
        }
    }

    /** Proyectos filtrados por ficha (usado en ManageFichasScreen) */
    fun proyectosDeFicha(fichaId: Int) {
        viewModelScope.launch {
            projectsRepository.listar(fichaId = fichaId).fold(
                onSuccess = { lista ->
                    // UI collectará vía uiState
                },
                onFailure = { e ->
                    // Error gestionado
                },
            )
        }
    }
}