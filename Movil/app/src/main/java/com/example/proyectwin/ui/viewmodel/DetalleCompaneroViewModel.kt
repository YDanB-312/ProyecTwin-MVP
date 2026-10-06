package com.example.proyectwin.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.proyectwin.data.local.SessionManager
import com.example.proyectwin.data.model.Ficha
import com.example.proyectwin.data.model.GeneralUser
import com.example.proyectwin.data.model.Project
import com.example.proyectwin.domain.repository.FichasRepository
import com.example.proyectwin.domain.repository.ProjectsRepository
import com.example.proyectwin.domain.repository.UsersRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

/** Vista de un compañero/instructor con lo que el backend autoriza mostrar. */
data class CompaneroData(
    val usuario: GeneralUser,
    val ficha: Ficha? = null,
    val proyectos: List<Project> = emptyList(),
)

sealed class CompaneroUiState {
    data object Loading : CompaneroUiState()
    data class Success(val data: CompaneroData) : CompaneroUiState()
    data class Error(val message: String) : CompaneroUiState()
}

@HiltViewModel
class DetalleCompaneroViewModel @Inject constructor(
    private val usersRepository: UsersRepository,
    private val fichasRepository: FichasRepository,
    private val projectsRepository: ProjectsRepository,
    private val sessionManager: SessionManager,
) : ViewModel() {

    private val _uiState = MutableStateFlow<CompaneroUiState>(CompaneroUiState.Loading)
    val uiState: StateFlow<CompaneroUiState> = _uiState.asStateFlow()

    private var idUsuario: Int = 0

    fun load(id: Int) {
        idUsuario = id
        viewModelScope.launch {
            _uiState.value = CompaneroUiState.Loading

            // Perfil público: el backend valida el alcance (misma ficha, etc.).
            val perfil = usersRepository.perfil(id).getOrElse { e ->
                _uiState.value = CompaneroUiState.Error(e.message ?: "No se pudo cargar el perfil.")
                return@launch
            }

            val fichaId = sessionManager.currentUser.first()?.fichaId
            val ficha = fichaId?.let { fichasRepository.obtener(it).getOrNull() }

            // Solo proyectos visibles para el solicitante; se filtra por autor/equipo.
            val proyectos = projectsRepository.listar().getOrDefault(emptyList())
                .filter { proyecto ->
                    proyecto.studentId == id || proyecto.equipo.any { miembro -> miembro.id == id }
                }

            _uiState.value = CompaneroUiState.Success(
                CompaneroData(usuario = perfil, ficha = ficha, proyectos = proyectos),
            )
        }
    }

    fun retry() {
        if (idUsuario != 0) load(idUsuario)
    }
}
