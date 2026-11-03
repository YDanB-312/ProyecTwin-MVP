package com.example.proyectwin.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.proyectwin.data.model.Ficha
import com.example.proyectwin.data.model.GeneralUser
import com.example.proyectwin.domain.repository.FichasRepository
import com.example.proyectwin.domain.repository.UsersRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class DetalleInstructorData(
    val instructor: GeneralUser,
    val fichas: List<Ficha> = emptyList(),
)

sealed class DetalleInstructorUiState {
    data object Loading : DetalleInstructorUiState()
    data class Success(val data: DetalleInstructorData) : DetalleInstructorUiState()
    data class Error(val message: String) : DetalleInstructorUiState()
}

/** Perfil del instructor y las fichas que tiene asignadas (vista del aprendiz). */
@HiltViewModel
class DetalleInstructorViewModel @Inject constructor(
    private val usersRepository: UsersRepository,
    private val fichasRepository: FichasRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow<DetalleInstructorUiState>(DetalleInstructorUiState.Loading)
    val uiState: StateFlow<DetalleInstructorUiState> = _uiState.asStateFlow()

    private var usuarioId: Int = 0

    fun load(id: Int) {
        usuarioId = id
        viewModelScope.launch {
            _uiState.value = DetalleInstructorUiState.Loading
            if (id <= 0) {
                _uiState.value = DetalleInstructorUiState.Success(
                    DetalleInstructorData(
                        instructor = GeneralUser(id = 0, name = "", email = "", role = "instructor"),
                    ),
                )
                return@launch
            }
            val perfil = usersRepository.perfil(id).getOrElse { e ->
                _uiState.value = DetalleInstructorUiState.Error(
                    e.message ?: "No se pudo cargar el instructor.",
                )
                return@launch
            }
            if (perfil.role != "instructor") {
                _uiState.value = DetalleInstructorUiState.Error("El usuario no es un instructor.")
                return@launch
            }
            val fichas = fichasRepository.listar().getOrDefault(emptyList())
                .filter { it.instructorUserId == id || (it.instructorUserId == null && it.instructorName == perfil.name) }
            _uiState.value = DetalleInstructorUiState.Success(
                DetalleInstructorData(instructor = perfil, fichas = fichas),
            )
        }
    }

    fun retry() {
        if (usuarioId != 0) load(usuarioId)
    }
}
