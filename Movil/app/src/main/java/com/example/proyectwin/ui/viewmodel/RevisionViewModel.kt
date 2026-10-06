package com.example.proyectwin.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.proyectwin.data.model.Project
import com.example.proyectwin.data.model.ProjectStatus
import com.example.proyectwin.domain.repository.ProjectsRepository
import com.example.proyectwin.domain.repository.SimilaritiesRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

/** Propuesta en revisión con su coincidencia máxima vigente. */
data class PropuestaRevision(
    val proyecto: Project,
    val maxSimilitud: Double,
)

sealed class RevisionUiState {
    data object Loading : RevisionUiState()
    data class Success(val propuestas: List<PropuestaRevision>) : RevisionUiState()
    data class Error(val message: String) : RevisionUiState()
}

/**
 * Revisión del instructor: propuestas de su alcance (el backend filtra) sin
 * borradores, con el % máximo de similitud vigente para priorizar.
 */
@HiltViewModel
class RevisionViewModel @Inject constructor(
    private val projectsRepository: ProjectsRepository,
    private val similaritiesRepository: SimilaritiesRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow<RevisionUiState>(RevisionUiState.Loading)
    val uiState: StateFlow<RevisionUiState> = _uiState.asStateFlow()

    fun load() = cargar(mostrarCarga = true)

    /**
     * Al volver del detalle de una propuesta la lista ya tiene datos: se
     * refresca en silencio para reflejar el nuevo estado sin perder la
     * posición ni mostrar un cargador que reemplace la lista.
     */
    fun iniciar() = cargar(mostrarCarga = _uiState.value !is RevisionUiState.Success)

    fun refrescar() = cargar(mostrarCarga = false)

    fun retry() = load()

    private fun cargar(mostrarCarga: Boolean) {
        viewModelScope.launch {
            val previas = (_uiState.value as? RevisionUiState.Success)?.propuestas
            if (mostrarCarga) _uiState.value = RevisionUiState.Loading

            val proyectos = projectsRepository.listar().getOrElse { e ->
                if (previas == null) {
                    _uiState.value = RevisionUiState.Error(e.message ?: "No se pudieron cargar las propuestas.")
                }
                return@launch
            }
            val pares = similaritiesRepository.listar().getOrDefault(emptyList())

            val propuestas = proyectos
                .filter { ProjectStatus.fromValue(it.estado) != ProjectStatus.BORRADOR }
                .map { proyecto ->
                    val max = pares
                        .filter { it.projectId1 == proyecto.id || it.projectId2 == proyecto.id }
                        .maxOfOrNull { it.similitud }
                        ?: 0.0
                    PropuestaRevision(proyecto, max)
                }

            _uiState.value = RevisionUiState.Success(propuestas)
        }
    }
}
