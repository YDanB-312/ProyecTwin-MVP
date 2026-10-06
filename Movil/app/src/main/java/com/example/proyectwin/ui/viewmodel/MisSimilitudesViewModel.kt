package com.example.proyectwin.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.proyectwin.data.local.SessionManager
import com.example.proyectwin.data.model.Project
import com.example.proyectwin.data.model.Similarity
import com.example.proyectwin.domain.repository.ProjectsRepository
import com.example.proyectwin.domain.repository.SimilaritiesRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

/** Una propuesta propia con sus coincidencias vigentes. */
data class SimilitudesDeProyecto(
    val proyecto: Project,
    val pares: List<Similarity>,
)

sealed class MisSimilitudesUiState {
    data object Loading : MisSimilitudesUiState()
    data class Success(val filas: List<SimilitudesDeProyecto>, val totalPares: Int) : MisSimilitudesUiState()
    data class Error(val message: String) : MisSimilitudesUiState()
}

/**
 * Similitudes del aprendiz (solo vigentes: el backend ya aplica la regla de
 * perspectiva). Con [load] de un proyecto se limita a ese par.
 */
@HiltViewModel
class MisSimilitudesViewModel @Inject constructor(
    private val similaritiesRepository: SimilaritiesRepository,
    private val projectsRepository: ProjectsRepository,
    private val sessionManager: SessionManager,
) : ViewModel() {

    private val _uiState = MutableStateFlow<MisSimilitudesUiState>(MisSimilitudesUiState.Loading)
    val uiState: StateFlow<MisSimilitudesUiState> = _uiState.asStateFlow()

    private var proyectoFiltro: Int? = null

    fun load(proyectoId: Int? = null) {
        proyectoFiltro = proyectoId
        viewModelScope.launch {
            _uiState.value = MisSimilitudesUiState.Loading
            val usuarioId = sessionManager.currentUser.first()?.id

            val pares = similaritiesRepository.listar(proyectoId = proyectoId).getOrElse { e ->
                _uiState.value = MisSimilitudesUiState.Error(e.message ?: "No se pudieron cargar las similitudes.")
                return@launch
            }

            val proyectos = projectsRepository.listar().getOrDefault(emptyList())
                .filter { proyecto ->
                    proyecto.studentId == usuarioId || proyecto.equipo.any { it.id == usuarioId }
                }

            val filas = proyectos.mapNotNull { proyecto ->
                val suyos = pares.filter { it.projectId1 == proyecto.id || it.projectId2 == proyecto.id }
                if (suyos.isEmpty()) null else SimilitudesDeProyecto(proyecto, suyos)
            }

            _uiState.value = MisSimilitudesUiState.Success(
                filas = filas,
                totalPares = pares.size,
            )
        }
    }

    fun retry() = load(proyectoFiltro)
}
