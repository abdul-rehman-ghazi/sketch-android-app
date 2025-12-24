package com.hotmail.arehmananis.sketchapp.presentation.feature.drawing

import android.graphics.Bitmap
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hotmail.arehmananis.sketchapp.domain.model.*
import com.hotmail.arehmananis.sketchapp.domain.usecase.auth.GetCurrentAuthUserUseCase
import com.hotmail.arehmananis.sketchapp.domain.usecase.drawing.SaveDrawingUseCase
import com.hotmail.arehmananis.sketchapp.domain.usecase.sketch.CreateSketchUseCase
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

/**
 * ViewModel for drawing screen with undo/redo functionality
 */
class DrawingViewModel(
    private val getCurrentAuthUserUseCase: GetCurrentAuthUserUseCase,
    private val saveDrawingUseCase: SaveDrawingUseCase,
    private val createSketchUseCase: CreateSketchUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(DrawingUiState())
    val uiState: StateFlow<DrawingUiState> = _uiState.asStateFlow()

    // Undo/Redo stacks
    private val pathHistory = mutableListOf<DrawingPath>()
    private val redoStack = mutableListOf<DrawingPath>()

    fun onDrawStart(offset: Offset) {
        val point = PathPoint(offset.x, offset.y)
        val newPath = DrawingPath(
            points = listOf(point),
            brush = _uiState.value.currentBrush,
            color = _uiState.value.currentColor,
            strokeWidth = _uiState.value.strokeWidth,
            opacity = _uiState.value.opacity
        )
        _uiState.update { it.copy(currentPath = newPath) }
    }

    fun onDraw(offset: Offset) {
        val currentPath = _uiState.value.currentPath ?: return
        val point = PathPoint(offset.x, offset.y)
        val updatedPath = currentPath.copy(
            points = currentPath.points + point
        )
        _uiState.update { it.copy(currentPath = updatedPath) }
    }

    fun onDrawEnd() {
        val currentPath = _uiState.value.currentPath ?: return

        // Add to path history
        pathHistory.add(currentPath)
        redoStack.clear() // Clear redo stack when new path is added

        // Add to completed paths
        val updatedPaths = _uiState.value.paths + currentPath
        _uiState.update {
            it.copy(
                paths = updatedPaths,
                currentPath = null,
                canUndo = pathHistory.isNotEmpty(),
                canRedo = false
            )
        }
    }

    fun undo() {
        if (pathHistory.isEmpty()) return

        val lastPath = pathHistory.removeLastOrNull() ?: return
        redoStack.add(lastPath)

        _uiState.update {
            it.copy(
                paths = pathHistory.toList(),
                canUndo = pathHistory.isNotEmpty(),
                canRedo = redoStack.isNotEmpty()
            )
        }
    }

    fun redo() {
        if (redoStack.isEmpty()) return

        val pathToRedo = redoStack.removeLastOrNull() ?: return
        pathHistory.add(pathToRedo)

        _uiState.update {
            it.copy(
                paths = pathHistory.toList(),
                canUndo = pathHistory.isNotEmpty(),
                canRedo = redoStack.isNotEmpty()
            )
        }
    }

    fun setBrush(brush: BrushType) {
        val config = BrushConfig.getConfig(brush)
        _uiState.update {
            it.copy(
                currentBrush = brush,
                strokeWidth = config.defaultWidth,
                opacity = config.defaultOpacity
            )
        }
    }

    fun setColor(color: Color) {
        _uiState.update {
            it.copy(currentColor = color.toArgb().toLong())
        }
    }

    fun setStrokeWidth(width: Float) {
        _uiState.update { it.copy(strokeWidth = width) }
    }

    fun setOpacity(opacity: Float) {
        _uiState.update { it.copy(opacity = opacity) }
    }

    fun setShapeTool(tool: ShapeTool) {
        _uiState.update { it.copy(shapeTool = tool) }
    }

    fun clearCanvas() {
        pathHistory.clear()
        redoStack.clear()
        _uiState.update {
            it.copy(
                paths = emptyList(),
                currentPath = null,
                canUndo = false,
                canRedo = false
            )
        }
    }

    fun saveSketch(canvasWidth: Int, canvasHeight: Int, createBitmap: () -> Bitmap) {
        viewModelScope.launch {
            _uiState.update { it.copy(isSaving = true, saveError = null) }

            try {
                // Get current user
                val user = getCurrentAuthUserUseCase().first()
                    ?: throw Exception("User not authenticated")

                // Create bitmap from canvas
                val bitmap = createBitmap()

                // Generate filename
                val fileName = "sketch_${System.currentTimeMillis()}.png"

                // Save bitmap to local storage
                val saveResult = saveDrawingUseCase(bitmap, fileName)
                saveResult.fold(
                    onSuccess = { filePath ->
                        // Create sketch entity
                        val dateFormat = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
                        val sketch = Sketch(
                            id = UUID.randomUUID().toString(),
                            title = "Sketch ${dateFormat.format(Date())}",
                            userId = user.uid,
                            createdAt = System.currentTimeMillis(),
                            updatedAt = System.currentTimeMillis(),
                            localImagePath = filePath,
                            remoteImageUrl = null,
                            thumbnailUrl = null,
                            syncStatus = SyncStatus.PENDING_UPLOAD,
                            width = canvasWidth,
                            height = canvasHeight
                        )

                        // Save to database
                        createSketchUseCase(sketch).fold(
                            onSuccess = {
                                _uiState.update {
                                    it.copy(
                                        isSaving = false,
                                        saveSuccess = true
                                    )
                                }
                            },
                            onFailure = { error ->
                                _uiState.update {
                                    it.copy(
                                        isSaving = false,
                                        saveError = error.message
                                    )
                                }
                            }
                        )
                    },
                    onFailure = { error ->
                        _uiState.update {
                            it.copy(
                                isSaving = false,
                                saveError = error.message
                            )
                        }
                    }
                )
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isSaving = false,
                        saveError = e.message ?: "Failed to save sketch"
                    )
                }
            }
        }
    }

    fun clearSaveState() {
        _uiState.update {
            it.copy(saveSuccess = false, saveError = null)
        }
    }
}

/**
 * UI state for drawing screen
 */
data class DrawingUiState(
    val paths: List<DrawingPath> = emptyList(),
    val currentPath: DrawingPath? = null,
    val currentBrush: BrushType = BrushType.PEN,
    val currentColor: Long = Color.Black.toArgb().toLong(),
    val strokeWidth: Float = 3f,
    val opacity: Float = 1f,
    val shapeTool: ShapeTool = ShapeTool.NONE,
    val canUndo: Boolean = false,
    val canRedo: Boolean = false,
    val isSaving: Boolean = false,
    val saveSuccess: Boolean = false,
    val saveError: String? = null
)
