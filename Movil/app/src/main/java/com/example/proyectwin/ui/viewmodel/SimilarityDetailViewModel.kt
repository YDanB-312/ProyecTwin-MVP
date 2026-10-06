package com.example.proyectwin.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.proyectwin.data.local.SessionManager
import com.example.proyectwin.data.model.Project
import com.example.proyectwin.data.model.Similarity
import com.example.proyectwin.domain.repository.SimilaritiesRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

/** Detalle de un par: la propuesta propia y la contraparte (solo lectura). */
data class SimilarityDetailData(
    val similarity: Similarity,
    val relacionadas: List<Similarity> = emptyList(),
    val miProyecto: Project? = null,
    val contraparte: Project? = null,
    val ambasMias: Boolean = false,
)

sealed class SimilarityDetailUiState {
    data object Loading : SimilarityDetailUiState()
    data class Success(val data: SimilarityDetailData) : SimilarityDetailUiState()
    data class Error(val message: String) : SimilarityDetailUiState()
}

@HiltViewModel
class SimilarityDetailViewModel @Inject constructor(
    private val similaritiesRepository: SimilaritiesRepository,
    private val sessionManager: SessionManager,
) : ViewModel() {

    private val _uiState = MutableStateFlow<SimilarityDetailUiState>(SimilarityDetailUiState.Loading)
    val uiState: StateFlow<SimilarityDetailUiState> = _uiState.asStateFlow()

    private var idActual: Int = 0

    fun load(id: Int) {
        idActual = id
        viewModelScope.launch {
            _uiState.value = SimilarityDetailUiState.Loading

            // Autorización real: el backend responde 403 si no puede ver el par.
            val par = similaritiesRepository.obtener(id).getOrElse { e ->
                _uiState.value = SimilarityDetailUiState.Error(e.message ?: "No se pudo cargar la similitud.")
                return@launch
            }

            val relacionadas = similaritiesRepository
                .listar(relatedTo = "${par.projectId1},${par.projectId2}")
                .getOrDefault(emptyList())
                .filter { it.id != par.id }

            val usuarioId = sessionManager.currentUser.first()?.id
            val mio1 = esPropia(par.project1, usuarioId)
            val mio2 = esPropia(par.project2, usuarioId)

            _uiState.value = SimilarityDetailUiState.Success(
                SimilarityDetailData(
                    similarity = par,
                    relacionadas = relacionadas,
                    miProyecto = if (mio1) par.project1 else if (mio2) par.project2 else null,
                    contraparte = if (mio1) par.project2 else if (mio2) par.project1 else null,
                    ambasMias = mio1 && mio2,
                ),
            )
        }
    }

    fun retry() {
        if (idActual != 0) load(idActual)
    }

    private fun esPropia(proyecto: Project?, usuarioId: Int?): Boolean {
        if (proyecto == null || usuarioId == null) return false
        return proyecto.studentId == usuarioId || proyecto.equipo.any { it.id == usuarioId }
    }
}
