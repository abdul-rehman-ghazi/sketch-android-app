package com.hotmail.arehmananis.sketchapp.presentation.feature.gallery

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hotmail.arehmananis.sketchapp.domain.model.Sketch
import com.hotmail.arehmananis.sketchapp.domain.usecase.auth.GetCurrentAuthUserUseCase
import com.hotmail.arehmananis.sketchapp.domain.usecase.sketch.GetUserSketchesUseCase
import com.hotmail.arehmananis.sketchapp.domain.usecase.sketch.SyncSketchesUseCase
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

/**
 * ViewModel for sketch gallery screen
 */
class GalleryViewModel(
    private val getCurrentAuthUserUseCase: GetCurrentAuthUserUseCase,
    private val getUserSketchesUseCase: GetUserSketchesUseCase,
    private val syncSketchesUseCase: SyncSketchesUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow<GalleryUiState>(GalleryUiState.Loading)
    val uiState: StateFlow<GalleryUiState> = _uiState.asStateFlow()

    init {
        loadSketches()
    }

    private fun loadSketches() {
        viewModelScope.launch {
            getCurrentAuthUserUseCase().collectLatest { authUser ->
                if (authUser != null) {
                    // Trigger background sync (don't wait for it)
                    launch {
                        syncSketchesUseCase(authUser.uid)
                    }

                    // Load sketches from Room (reactive)
                    getUserSketchesUseCase(authUser.uid).collect { sketches ->
                        _uiState.value = GalleryUiState.Success(sketches)
                    }
                } else {
                    _uiState.value = GalleryUiState.Error("User not authenticated")
                }
            }
        }
    }

    fun refresh() {
        viewModelScope.launch {
            getCurrentAuthUserUseCase().first()?.let { user ->
                _uiState.value = GalleryUiState.Loading
                syncSketchesUseCase(user.uid).fold(
                    onSuccess = {
                        // Success - Room will update automatically
                    },
                    onFailure = { error ->
                        // Show error but keep existing data
                        val currentState = _uiState.value
                        if (currentState is GalleryUiState.Success) {
                            // Keep showing sketches even if sync failed
                        } else {
                            _uiState.value = GalleryUiState.Error(
                                error.message ?: "Sync failed"
                            )
                        }
                    }
                )
            }
        }
    }
}

/**
 * UI state for gallery screen
 */
sealed interface GalleryUiState {
    object Loading : GalleryUiState
    data class Success(val sketches: List<Sketch>) : GalleryUiState
    data class Error(val message: String) : GalleryUiState
}
