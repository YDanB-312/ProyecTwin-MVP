package com.example.proyectwin.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.proyectwin.data.model.Similarity
import com.example.proyectwin.domain.repository.SimilaritiesRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed class SimilitudesInstructorUiState {
    data object Loading : SimilitudesInstructorUiState()
    data class Success(val pares: List<Similarity>) : SimilitudesInstructorUiState()
    data class Error(val message: String) : SimilitudesInstructorUiState()
}

/** Pares vigentes dentro del alcance del instructor (fichas/programas). */
@HiltViewModel
class SimilitudesInstructorViewModel @Inject constructor(
    private val similaritiesRepository: SimilaritiesRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow<SimilitudesInstructorUiState>(SimilitudesInstructorUiState.Loading)
    val uiState: StateFlow<SimilitudesInstructorUiState> = _uiState.asStateFlow()

    fun load() {
        viewModelScope.launch {
            _uiState.value = SimilitudesInstructorUiState.Loading
            similaritiesRepository.listar().fold(
                onSuccess = { pares -> _uiState.value = SimilitudesInstructorUiState.Success(pares) },
                onFailure = { e ->
                    _uiState.value = SimilitudesInstructorUiState.Error(e.message ?: "No se pudieron cargar las similitudes.")
                },
            )
        }
    }

    fun retry() = load()
}
