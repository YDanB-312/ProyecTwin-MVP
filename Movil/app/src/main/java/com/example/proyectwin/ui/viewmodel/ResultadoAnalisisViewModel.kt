package com.example.proyectwin.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.proyectwin.data.local.SessionManager
import com.example.proyectwin.data.model.Project
import com.example.proyectwin.data.model.Similarity
import com.example.proyectwin.domain.repository.MotorRepository
import com.example.proyectwin.domain.repository.ProjectsRepository
import com.example.proyectwin.domain.repository.SimilaritiesRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlin.math.roundToInt
import javax.inject.Inject

data class ResultadoAnalisisData(
    val proyecto: Project,
    val pares: List<Similarity>,
    val umbralPct: Int,
    val esMia: Boolean,
) {
    val maximo: Similarity? get() = pares.maxByOrNull { it.similitud }
    val pctMaximo: Int get() = ((maximo?.similitud ?: 0.0) * 100).roundToInt().coerceIn(0, 100)

    /** Misma escala que la web: A >= umbral, B >= umbral/2, C el resto. */
    val grado: String
        get() = when {
            pctMaximo >= umbralPct -> "A"
            pctMaximo >= umbralPct / 2.0 -> "B"
            else -> "C"
        }

    val nivel: String
        get() = when (grado) {
            "A" -> "alta"
            "B" -> "media"
            else -> "baja"
        }
}

sealed class ResultadoAnalisisUiState {
    data object Loading : ResultadoAnalisisUiState()
    data class Success(val data: ResultadoAnalisisData) : ResultadoAnalisisUiState()
    data class Error(val message: String) : ResultadoAnalisisUiState()
}

/** Resultado real del motor para una propuesta del aprendiz autenticado. */
@HiltViewModel
class ResultadoAnalisisViewModel @Inject constructor(
    private val projectsRepository: ProjectsRepository,
    private val similaritiesRepository: SimilaritiesRepository,
    private val motorRepository: MotorRepository,
    private val sessionManager: SessionManager,
) : ViewModel() {

    private val _uiState = MutableStateFlow<ResultadoAnalisisUiState>(ResultadoAnalisisUiState.Loading)
    val uiState: StateFlow<ResultadoAnalisisUiState> = _uiState.asStateFlow()

    private var proyectoId: Int = 0

    fun load(id: Int) {
        proyectoId = id
        viewModelScope.launch {
            _uiState.value = ResultadoAnalisisUiState.Loading
            val proyecto = projectsRepository.obtener(id).getOrElse { e ->
                _uiState.value = ResultadoAnalisisUiState.Error(
                    e.message ?: "No se pudo cargar la propuesta.",
                )
                return@launch
            }
            val pares = similaritiesRepository.listar(proyectoId = id).getOrElse { e ->
                _uiState.value = ResultadoAnalisisUiState.Error(
                    e.message ?: "No se pudo consultar el análisis.",
                )
                return@launch
            }
            val umbral = motorRepository.obtenerConfig().getOrNull()?.umbral
                ?.let { (it * 100).roundToInt() } ?: 30
            val usuario = sessionManager.currentUser.first()
            val equipo = projectsRepository.equipo(id).getOrDefault(emptyList())
            val esMia = usuario != null && (
                proyecto.studentId == usuario.id ||
                    proyecto.equipo.any { it.id == usuario.id } ||
                    equipo.any { it.usuario.id == usuario.id }
                )
            _uiState.value = ResultadoAnalisisUiState.Success(
                ResultadoAnalisisData(
                    proyecto = proyecto,
                    pares = pares.sortedByDescending { it.similitud },
                    umbralPct = umbral.coerceIn(0, 100),
                    esMia = esMia,
                ),
            )
        }
    }

    fun retry() {
        if (proyectoId != 0) load(proyectoId)
    }
}
