package com.example.proyectwin.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.proyectwin.data.model.BugReport
import com.example.proyectwin.data.model.GeneralUser
import com.example.proyectwin.data.model.Project
import com.example.proyectwin.data.model.Similarity
import com.example.proyectwin.domain.repository.BugReportsRepository
import com.example.proyectwin.domain.repository.ProjectsRepository
import com.example.proyectwin.domain.repository.SimilaritiesRepository
import com.example.proyectwin.domain.repository.UsersRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed class AdminUiState {
    data object Loading : AdminUiState()
    data class Success(
        val users: List<GeneralUser>,
        val projects: List<Project>,
        val bugReports: List<BugReport>,
        val similarities: List<Similarity>
    ) : AdminUiState()
    data class Error(val message: String) : AdminUiState()
}

@HiltViewModel
class AdminViewModel @Inject constructor(
    private val usersRepository: UsersRepository,
    private val projectsRepository: ProjectsRepository,
    private val bugReportsRepository: BugReportsRepository,
    private val similaritiesRepository: SimilaritiesRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow<AdminUiState>(AdminUiState.Loading)
    val uiState: StateFlow<AdminUiState> = _uiState.asStateFlow()

    init {
        loadAll()
    }

    fun loadAll() {
        viewModelScope.launch {
            _uiState.value = AdminUiState.Loading
            val usuarios = async { usersRepository.listar() }
            val proyectos = async { projectsRepository.listar() }
            val reportes = async { bugReportsRepository.listar() }
            val similitudes = async { similaritiesRepository.listar() }

            val u = usuarios.await()
            val p = proyectos.await()
            val b = reportes.await()
            val s = similitudes.await()

            val fallo = listOf(u, p, b, s).firstOrNull { it.isFailure }
            if (fallo != null) {
                _uiState.value = AdminUiState.Error(
                    fallo.exceptionOrNull()?.message ?: "Error al cargar los datos",
                )
                return@launch
            }
            _uiState.value = AdminUiState.Success(
                users = u.getOrThrow(),
                projects = p.getOrThrow(),
                bugReports = b.getOrThrow(),
                similarities = s.getOrThrow(),
            )
        }
    }

    fun refresh() {
        loadAll()
    }

    fun createUser(name: String, email: String, rol: String, onResult: (Boolean) -> Unit = {}) {
        viewModelScope.launch {
            val (nombre, apellido) = com.example.proyectwin.data.mapper.dividirNombre(name)
            usersRepository.crear(
                nombre = nombre,
                apellido = apellido,
                correo = email,
                password = "Password123!",
                rol = rol
            ).fold(
                onSuccess = {
                    refresh()
                    onResult(true)
                },
                onFailure = {
                    onResult(false)
                }
            )
        }
    }

    fun deleteUser(id: Int, onResult: (Boolean) -> Unit = {}) {
        viewModelScope.launch {
            usersRepository.eliminar(id).fold(
                onSuccess = {
                    refresh()
                    onResult(true)
                },
                onFailure = {
                    onResult(false)
                }
            )
        }
    }

    fun clearError() {
        if (_uiState.value is AdminUiState.Error) {
            _uiState.value = AdminUiState.Loading
        }
    }
}
