package com.hotmail.arehmananis.sketchapp.presentation.feature.gallery

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hotmail.arehmananis.sketchapp.domain.model.Sketch
import com.hotmail.arehmananis.sketchapp.domain.usecase.auth.GetCurrentAuthUserUseCase
import com.hotmail.arehmananis.sketchapp.domain.usecase.sketch.DeleteSketchUseCase
import com.hotmail.arehmananis.sketchapp.domain.usecase.sketch.GetUserSketchesUseCase
import com.hotmail.arehmananis.sketchapp.domain.usecase.sketch.SyncSketchesUseCase
import com.hotmail.arehmananis.sketchapp.domain.usecase.sketch.UpdateSketchUseCase
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

/**
 * ViewModel for sketch gallery screen
 */
class GalleryViewModel(
    private val getCurrentAuthUserUseCase: GetCurrentAuthUserUseCase,
    private val getUserSketchesUseCase: GetUserSketchesUseCase,
    private val syncSketchesUseCase: SyncSketchesUseCase,
    private val deleteSketchUseCase: DeleteSketchUseCase,
    private val updateSketchUseCase: UpdateSketchUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow<GalleryUiState>(GalleryUiState.Loading)
    val uiState: StateFlow<GalleryUiState> = _uiState.asStateFlow()

    // Dialog states
    private val _showDeleteDialog = MutableStateFlow<String?>(null)
    val showDeleteDialog: StateFlow<String?> = _showDeleteDialog.asStateFlow()

    private val _showRenameDialog = MutableStateFlow<Sketch?>(null)
    val showRenameDialog: StateFlow<Sketch?> = _showRenameDialog.asStateFlow()

    // Operation states
    private val _deleteInProgress = MutableStateFlow(false)
    val deleteInProgress: StateFlow<Boolean> = _deleteInProgress.asStateFlow()

    private val _renameInProgress = MutableStateFlow(false)
    val renameInProgress: StateFlow<Boolean> = _renameInProgress.asStateFlow()

    private val _operationError = MutableStateFlow<String?>(null)
    val operationError: StateFlow<String?> = _operationError.asStateFlow()

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

    /**
     * Show delete confirmation dialog
     */
    fun showDeleteConfirmation(sketchId: String) {
        _showDeleteDialog.value = sketchId
    }

    /**
     * Hide delete dialog
     */
    fun dismissDeleteDialog() {
        _showDeleteDialog.value = null
        _operationError.value = null
    }

    /**
     * Confirm deletion
     */
    fun confirmDelete(sketchId: String) {
        viewModelScope.launch {
            _deleteInProgress.value = true
            _operationError.value = null

            deleteSketchUseCase(sketchId).fold(
                onSuccess = {
                    _showDeleteDialog.value = null
                    _deleteInProgress.value = false
                },
                onFailure = { error ->
                    _operationError.value = error.message ?: "Failed to delete sketch"
                    _deleteInProgress.value = false
                }
            )
        }
    }

    /**
     * Show rename dialog
     */
    fun showRenameDialog(sketch: Sketch) {
        _showRenameDialog.value = sketch
    }

    /**
     * Hide rename dialog
     */
    fun dismissRenameDialog() {
        _showRenameDialog.value = null
        _operationError.value = null
    }

    /**
     * Confirm rename
     */
    fun confirmRename(sketch: Sketch, newTitle: String) {
        viewModelScope.launch {
            val trimmed = newTitle.trim()

            // Validation
            if (trimmed.isEmpty()) {
                _operationError.value = "Title cannot be empty"
                return@launch
            }
            if (trimmed.length > 100) {
                _operationError.value = "Title too long (max 100 chars)"
                return@launch
            }

            _renameInProgress.value = true
            _operationError.value = null

            val updated = sketch.copy(title = trimmed)
            updateSketchUseCase(updated).fold(
                onSuccess = {
                    _showRenameDialog.value = null
                    _renameInProgress.value = false
                },
                onFailure = { error ->
                    _operationError.value = error.message ?: "Failed to rename sketch"
                    _renameInProgress.value = false
                }
            )
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
