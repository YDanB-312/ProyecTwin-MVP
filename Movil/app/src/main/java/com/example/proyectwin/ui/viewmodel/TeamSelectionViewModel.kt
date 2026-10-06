package com.example.proyectwin.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.proyectwin.data.local.SessionManager
import com.example.proyectwin.data.model.ApprenticeProfile
import com.example.proyectwin.domain.repository.MembersRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * Compañeros de la ficha para el equipo de una propuesta. Se usa
 * `GET /apprentices` (accesible al aprendiz), no `/general-users` (solo admin).
 * Los ids seleccionados son ids de APRENDIZ (`apprentices.id`), que es lo que
 * exige `POST /apprentice-projects`.
 */
sealed class TeamSelectionUiState {
    data object Loading : TeamSelectionUiState()
    data class Success(
        val members: List<ApprenticeProfile>,
        val selectedIds: Set<Int>,
    ) : TeamSelectionUiState()
    data class Error(val message: String) : TeamSelectionUiState()
}

@HiltViewModel
class TeamSelectionViewModel @Inject constructor(
    private val membersRepository: MembersRepository,
    private val sessionManager: SessionManager,
) : ViewModel() {

    private val _uiState = MutableStateFlow<TeamSelectionUiState>(TeamSelectionUiState.Loading)
    val uiState: StateFlow<TeamSelectionUiState> = _uiState.asStateFlow()

    fun loadTeam() {
        viewModelScope.launch {
            _uiState.value = TeamSelectionUiState.Loading
            val usuario = sessionManager.currentUser.firstOrNull()
            val fichaId = usuario?.fichaId
            if (fichaId == null || usuario.id == 0) {
                _uiState.value = TeamSelectionUiState.Error(
                    "No se pudo obtener tu ficha. Únete a una ficha primero.",
                )
                return@launch
            }

            membersRepository.listarAprendices().fold(
                onSuccess = { lista ->
                    val companeros = lista.filter {
                        it.idClassGroup == fichaId && it.idUsuario != usuario.id
                    }
                    _uiState.value = TeamSelectionUiState.Success(
                        members = companeros,
                        selectedIds = emptySet(),
                    )
                },
                onFailure = { e ->
                    _uiState.value = TeamSelectionUiState.Error(
                        e.message ?: "Error al cargar los compañeros",
                    )
                },
            )
        }
    }

    fun toggleMember(idAprendiz: Int) {
        val current = _uiState.value
        if (current is TeamSelectionUiState.Success) {
            val selected = current.selectedIds.toMutableSet()
            if (selected.contains(idAprendiz)) selected.remove(idAprendiz) else selected.add(idAprendiz)
            _uiState.value = current.copy(selectedIds = selected)
        }
    }

    fun removeSelected(idAprendiz: Int) {
        val current = _uiState.value
        if (current is TeamSelectionUiState.Success) {
            _uiState.value = current.copy(selectedIds = current.selectedIds - idAprendiz)
        }
    }
}
