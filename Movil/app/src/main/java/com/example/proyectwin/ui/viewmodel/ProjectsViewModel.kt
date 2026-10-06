package com.example.proyectwin.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.proyectwin.data.api.ApiException
import com.example.proyectwin.data.model.Project
import com.example.proyectwin.data.model.ProjectDraft
import com.example.proyectwin.domain.repository.ProjectsRepository
import com.example.proyectwin.domain.usecase.CreateProjectWithTeamUseCase
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

    /** Estado de guardar/enviar (crear o editar) con doble-submit protegido. */
    sealed class SaveState {
        data object Idle : SaveState()
        data object Loading : SaveState()
        data class Success(
            val projectId: Int,
            val miembrosFallidos: List<Int> = emptyList(),
        ) : SaveState()
        data class Error(
            val message: String,
            val fieldErrors: Map<String, String> = emptyMap(),
            /** Proyecto ya guardado aunque el envío falló: se puede seguir desde el detalle. */
            val projectId: Int? = null,
        ) : SaveState()
    }

    private val _saveState = MutableStateFlow<SaveState>(SaveState.Idle)
    val saveState: StateFlow<SaveState> = _saveState.asStateFlow()

    /** Propuesta precargada para edición (`NewProjectScreen` con projectId). */
    private val _proyectoEdicion = MutableStateFlow<Project?>(null)
    val proyectoEdicion: StateFlow<Project?> = _proyectoEdicion.asStateFlow()

    private val _cargandoEdicion = MutableStateFlow(false)
    val cargandoEdicion: StateFlow<Boolean> = _cargandoEdicion.asStateFlow()

    fun cargarParaEdicion(id: Int) {
        if (_proyectoEdicion.value?.id == id || _cargandoEdicion.value) return
        viewModelScope.launch {
            _cargandoEdicion.value = true
            projectsRepository.obtener(id).onSuccess { _proyectoEdicion.value = it }
            _cargandoEdicion.value = false
        }
    }

    fun resetEdicion() {
        _proyectoEdicion.value = null
    }

    fun resetSaveState() {
        _saveState.value = SaveState.Idle
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

    /**
     * Guarda la propuesta (crea o edita) y opcionalmente la envía a revisión.
     * El backend valida el contenido al enviar y ejecuta el motor de similitud.
     */
    fun guardar(
        draft: ProjectDraft,
        proyectoExistente: Int?,
        miembros: List<Int> = emptyList(),
        enviarDespues: Boolean = false,
    ) {
        if (_saveState.value is SaveState.Loading) return // doble toque
        viewModelScope.launch {
            _saveState.value = SaveState.Loading

            var fallidos: List<Int> = emptyList()
            val guardado: Result<Project> = if (proyectoExistente == null) {
                createProjectWithTeamUseCase(draft, miembros).fold(
                    onSuccess = { creada ->
                        fallidos = creada.miembrosFallidos
                        Result.success(creada.proyecto)
                    },
                    onFailure = { Result.failure(it) },
                )
            } else {
                projectsRepository.actualizar(proyectoExistente, draft)
            }

            guardado.fold(
                onSuccess = { proyecto ->
                    if (!enviarDespues) {
                        _saveState.value = SaveState.Success(proyecto.id, fallidos)
                        load()
                    } else {
                        projectsRepository.enviar(proyecto.id).fold(
                            onSuccess = { enviado ->
                                _saveState.value = SaveState.Success(enviado.id, fallidos)
                                load()
                            },
                            onFailure = { e ->
                                // La propuesta quedó guardada; el mensaje del backend
                                // (p. ej. campos faltantes) se muestra y se puede corregir.
                                _saveState.value = SaveState.Error(
                                    message = e.message ?: "No se pudo enviar la propuesta.",
                                    fieldErrors = (e as? ApiException.Http)?.fieldErrors.orEmpty(),
                                    projectId = proyecto.id,
                                )
                                load()
                            },
                        )
                    }
                },
                onFailure = { e ->
                    _saveState.value = SaveState.Error(
                        message = e.message ?: "No se pudo guardar la propuesta.",
                        fieldErrors = (e as? ApiException.Http)?.fieldErrors.orEmpty(),
                    )
                },
            )
        }
    }
}
