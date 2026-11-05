package com.example.proyectwin.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.proyectwin.data.model.Ficha
import com.example.proyectwin.data.model.Project
import com.example.proyectwin.domain.repository.FichasRepository
import com.example.proyectwin.domain.repository.ProjectsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class DetalleFichaData(
    val ficha: Ficha,
    val propuestas: List<Project>,
)

sealed class DetalleFichaUiState {
    data object Loading : DetalleFichaUiState()
    data class Success(val data: DetalleFichaData) : DetalleFichaUiState()
    data class Error(val message: String) : DetalleFichaUiState()
}

/** Detalle de una ficha consultado por un aprendiz (propia o de un compañero). */
@HiltViewModel
class DetalleFichaViewModel @Inject constructor(
    private val fichasRepository: FichasRepository,
    private val projectsRepository: ProjectsRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow<DetalleFichaUiState>(DetalleFichaUiState.Loading)
    val uiState: StateFlow<DetalleFichaUiState> = _uiState.asStateFlow()

    private var fichaId: Int = 0

    fun load(id: Int) {
        fichaId = id
        viewModelScope.launch {
            _uiState.value = DetalleFichaUiState.Loading
            val resultado = coroutineScope {
                val fichaDeferred = async { fichasRepository.obtener(id) }
                val proyectosDeferred = async { projectsRepository.listar(fichaId = id) }
                fichaDeferred.await() to proyectosDeferred.await()
            }
            val ficha = resultado.first.getOrElse { e ->
                _uiState.value = DetalleFichaUiState.Error(e.message ?: "No se pudo cargar la ficha.")
                return@launch
            }
            val propuestas = resultado.second.getOrDefault(emptyList())
            _uiState.value = DetalleFichaUiState.Success(DetalleFichaData(ficha, propuestas))
        }
    }

    fun retry() {
        if (fichaId != 0) load(fichaId)
    }
}
