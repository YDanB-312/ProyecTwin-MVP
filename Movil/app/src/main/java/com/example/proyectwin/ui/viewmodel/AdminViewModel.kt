package com.example.proyectwin.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.proyectwin.data.mock.MockDataProvider
import com.example.proyectwin.data.model.BugReport
import com.example.proyectwin.data.model.GeneralUser
import com.example.proyectwin.data.model.Project
import com.example.proyectwin.data.model.Similarity
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

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

class AdminViewModel : ViewModel() {
    private val _uiState = MutableStateFlow<AdminUiState>(AdminUiState.Loading)
    val uiState: StateFlow<AdminUiState> = _uiState.asStateFlow()

    init {
        loadAll()
    }

    fun loadAll() {
        viewModelScope.launch {
            _uiState.value = AdminUiState.Loading
            _uiState.value = AdminUiState.Success(
                users = MockDataProvider.users,
                projects = MockDataProvider.getAllProjects(),
                bugReports = MockDataProvider.getAllBugReports(),
                similarities = MockDataProvider.getAllSimilarities()
            )
        }
    }

    fun refresh() {
        loadAll()
    }

    fun clearError() {
        if (_uiState.value is AdminUiState.Error) {
            _uiState.value = AdminUiState.Loading
        }
    }
}
