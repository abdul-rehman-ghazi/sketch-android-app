package com.hotmail.arehmananis.sketchapp.presentation.feature.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hotmail.arehmananis.sketchapp.domain.model.User
import com.hotmail.arehmananis.sketchapp.domain.usecase.GetCurrentUserUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class HomeViewModel(
    private val getCurrentUserUseCase: GetCurrentUserUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow<HomeUiState>(HomeUiState.Loading)
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    init {
        loadUser()
    }

    private fun loadUser() {
        viewModelScope.launch {
            getCurrentUserUseCase().collect { user ->
                _uiState.value = if (user != null) {
                    HomeUiState.Success(user)
                } else {
                    HomeUiState.Error("User not found")
                }
            }
        }
    }

    fun refresh() {
        loadUser()
    }
}

sealed interface HomeUiState {
    object Loading : HomeUiState
    data class Success(val user: User) : HomeUiState
    data class Error(val message: String) : HomeUiState
}
