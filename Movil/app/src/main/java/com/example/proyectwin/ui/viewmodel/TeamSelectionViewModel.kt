package com.example.proyectwin.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.proyectwin.data.local.SessionManager
import com.example.proyectwin.data.model.GeneralUser
import com.example.proyectwin.data.model.UserRole
import com.example.proyectwin.domain.repository.UsersRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed class TeamSelectionUiState {
    data object Loading : TeamSelectionUiState()
    data class Success(val members: List<GeneralUser>, val selectedIds: Set<Int>) : TeamSelectionUiState()
    data class Error(val message: String) : TeamSelectionUiState()
}

@HiltViewModel
class TeamSelectionViewModel @Inject constructor(
    private val usersRepository: UsersRepository,
    private val sessionManager: SessionManager,
) : ViewModel() {

    private val _uiState = MutableStateFlow<TeamSelectionUiState>(TeamSelectionUiState.Loading)
    val uiState: StateFlow<TeamSelectionUiState> = _uiState.asStateFlow()

    fun loadTeam() {
        viewModelScope.launch {
            _uiState.value = TeamSelectionUiState.Loading
            val user = sessionManager.currentUser.firstOrNull()
            val fichaId = user?.fichaId
            val currentUserId = user?.id

            if (fichaId == null || currentUserId == null) {
                _uiState.value = TeamSelectionUiState.Error("No se pudo obtener tu ficha. Únete a una ficha primero.")
                return@launch
            }

            usersRepository.listar(role = UserRole.APRENDIZ.value, fichaId = fichaId).fold(
                onSuccess = { list ->
                    val peers = list.filter { it.id != currentUserId }
                    _uiState.value = TeamSelectionUiState.Success(members = peers, selectedIds = emptySet())
                },
                onFailure = { e ->
                    _uiState.value = TeamSelectionUiState.Error(e.message ?: "Error al cargar los compañeros")
                },
            )
        }
    }

    fun toggleMember(id: Int) {
        val current = _uiState.value
        if (current is TeamSelectionUiState.Success) {
            val selected = current.selectedIds.toMutableSet()
            if (selected.contains(id)) selected.remove(id) else selected.add(id)
            _uiState.value = current.copy(selectedIds = selected)
        }
    }

    fun removeSelected(id: Int) {
        val current = _uiState.value
        if (current is TeamSelectionUiState.Success) {
            _uiState.value = current.copy(selectedIds = current.selectedIds - id)
        }
    }
}
