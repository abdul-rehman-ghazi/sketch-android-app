package com.hotmail.arehmananis.sketchapp.presentation.feature.drawing

import android.graphics.Bitmap
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hotmail.arehmananis.sketchapp.domain.model.BrushConfig
import com.hotmail.arehmananis.sketchapp.domain.model.BrushType
import com.hotmail.arehmananis.sketchapp.domain.model.DrawingPath
import com.hotmail.arehmananis.sketchapp.domain.model.PathPoint
import com.hotmail.arehmananis.sketchapp.domain.model.ShapeTool
import com.hotmail.arehmananis.sketchapp.domain.model.Sketch
import com.hotmail.arehmananis.sketchapp.domain.model.SyncStatus
import com.hotmail.arehmananis.sketchapp.domain.usecase.auth.GetCurrentAuthUserUseCase
import com.hotmail.arehmananis.sketchapp.domain.usecase.drawing.SaveDrawingUseCase
import com.hotmail.arehmananis.sketchapp.domain.usecase.sketch.CreateSketchUseCase
import com.hotmail.arehmananis.sketchapp.domain.usecase.sketch.GetSketchByIdUseCase
import com.hotmail.arehmananis.sketchapp.domain.usecase.sketch.TriggerSketchSyncUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID

/**
 * ViewModel for drawing screen with undo/redo functionality
 */
class DrawingViewModel(
    private val getCurrentAuthUserUseCase: GetCurrentAuthUserUseCase,
    private val saveDrawingUseCase: SaveDrawingUseCase,
    private val createSketchUseCase: CreateSketchUseCase,
    private val getSketchByIdUseCase: GetSketchByIdUseCase,
    private val triggerSketchSyncUseCase: TriggerSketchSyncUseCase
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
            color = _uiState.value.currentColor.toLong(),
            strokeWidth = _uiState.value.strokeWidth,
            opacity = _uiState.value.opacity,
            shapeTool = _uiState.value.shapeTool,
            isFilled = _uiState.value.isFilled
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
                opacity = config.defaultOpacity,
                shapeTool = ShapeTool.NONE // Reset shape tool when brush is selected
            )
        }
    }

    fun setColor(color: Color) {
        _uiState.update {
            it.copy(currentColor = color.toArgb())
        }
    }

    fun setStrokeWidth(width: Float) {
        _uiState.update { it.copy(strokeWidth = width) }
    }

    fun setOpacity(opacity: Float) {
        _uiState.update { it.copy(opacity = opacity) }
    }

    fun setShapeTool(tool: ShapeTool) {
        _uiState.update {
            it.copy(
                shapeTool = tool,
                // When selecting NONE, don't change anything else
                // When selecting a shape, the shape takes precedence over freehand brush drawing
            )
        }
    }

    fun setIsFilled(filled: Boolean) {
        _uiState.update { it.copy(isFilled = filled) }
    }

    fun clearCanvas() {
        pathHistory.clear()
        redoStack.clear()
        _uiState.update {
            it.copy(
                paths = emptyList(),
                currentPath = null,
                canUndo = false,
                canRedo = false,
                loadedSketchId = null
            )
        }
    }

    /**
     * Initialize drawing screen - load existing sketch or start fresh
     * @param sketchId The sketch ID to load, or null for a new sketch
     */
    fun initialize(sketchId: String?) {
        if (sketchId == null) {
            // New sketch - clear canvas
            clearCanvas()
        } else {
            // Load existing sketch
            loadSketch(sketchId)
        }
    }

    /**
     * Load an existing sketch by ID
     * Downloads drawing paths from Cloudinary if needed
     * @param sketchId The ID of the sketch to load
     */
    private fun loadSketch(sketchId: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, loadError = null) }

            try {
                // Get sketch with paths (will download from Cloudinary if needed)
                val result = getSketchByIdUseCase(sketchId, downloadPaths = true)

                result.fold(
                    onSuccess = { sketch ->
                        // Load drawing paths
                        val paths = sketch.drawingPaths ?: emptyList()
                        val emojis = sketch.emojiElements ?: emptyList()

                        // Initialize path history for undo/redo
                        pathHistory.clear()
                        pathHistory.addAll(paths)
                        redoStack.clear()

                        // Update UI state
                        _uiState.update {
                            it.copy(
                                isLoading = false,
                                loadedSketchId = sketchId,
                                paths = paths,
                                emojiElements = emojis,
                                canUndo = paths.isNotEmpty(),
                                canRedo = false
                            )
                        }
                    },
                    onFailure = { error ->
                        _uiState.update {
                            it.copy(
                                isLoading = false,
                                loadError = error.message ?: "Failed to load sketch"
                            )
                        }
                    }
                )
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        loadError = e.message ?: "Failed to load sketch"
                    )
                }
            }
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
                        val currentState = _uiState.value

                        val sketch = Sketch(
                            id = currentState.loadedSketchId ?: UUID.randomUUID().toString(),
                            title = "Sketch ${dateFormat.format(Date())}",
                            userId = user.uid,
                            createdAt = System.currentTimeMillis(),
                            updatedAt = System.currentTimeMillis(),
                            localImagePath = filePath,
                            remoteImageUrl = null,
                            thumbnailUrl = null,
                            syncStatus = SyncStatus.PENDING_UPLOAD,
                            width = canvasWidth,
                            height = canvasHeight,
                            drawingPaths = currentState.paths,
                            emojiElements = currentState.emojiElements
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

                                // Trigger immediate sync to upload to cloud
                                triggerSketchSyncUseCase()
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

    // Emoji-related methods
    fun toggleEmojiPicker() {
        _uiState.update { it.copy(showEmojiPicker = !it.showEmojiPicker) }
    }

    fun addEmoji(emoji: String, x: Float, y: Float) {
        val newEmoji = com.hotmail.arehmananis.sketchapp.domain.model.EmojiElement(
            id = UUID.randomUUID().toString(),
            emoji = emoji,
            x = x,
            y = y,
            size = 48f
        )
        _uiState.update {
            it.copy(
                emojiElements = it.emojiElements + newEmoji,
                showEmojiPicker = false
            )
        }
    }

    fun selectEmoji(emojiId: String?) {
        _uiState.update { it.copy(selectedEmojiId = emojiId) }
    }

    fun moveEmoji(emojiId: String, deltaX: Float, deltaY: Float) {
        val updatedEmojis = _uiState.value.emojiElements.map { emoji ->
            if (emoji.id == emojiId) {
                emoji.copy(x = emoji.x + deltaX, y = emoji.y + deltaY)
            } else {
                emoji
            }
        }
        _uiState.update { it.copy(emojiElements = updatedEmojis) }
    }

    fun resizeEmoji(emojiId: String, newSize: Float) {
        val updatedEmojis = _uiState.value.emojiElements.map { emoji ->
            if (emoji.id == emojiId) {
                emoji.copy(size = newSize.coerceIn(24f, 200f))
            } else {
                emoji
            }
        }
        _uiState.update { it.copy(emojiElements = updatedEmojis) }
    }

    fun deleteEmoji(emojiId: String) {
        val updatedEmojis = _uiState.value.emojiElements.filter { it.id != emojiId }
        _uiState.update {
            it.copy(
                emojiElements = updatedEmojis,
                selectedEmojiId = null
            )
        }
    }

    fun rotateEmoji(emojiId: String, rotation: Float) {
        val updatedEmojis = _uiState.value.emojiElements.map { emoji ->
            if (emoji.id == emojiId) {
                emoji.copy(rotation = rotation)
            } else {
                emoji
            }
        }
        _uiState.update { it.copy(emojiElements = updatedEmojis) }
    }

    // Share options dialog
    fun toggleShareDialog() {
        _uiState.update { it.copy(showShareDialog = !it.showShareDialog) }
    }

    /**
     * Share sketch - exports to MediaStore (Photos app) without saving to database
     */
    fun shareSketch(
        canvasWidth: Int,
        canvasHeight: Int,
        createBitmap: (Int, Int, Boolean) -> Bitmap,
        exportOptions: com.hotmail.arehmananis.sketchapp.presentation.feature.drawing.ExportOptions
    ) {
        viewModelScope.launch {
            _uiState.update { it.copy(isSaving = true, saveError = null, showShareDialog = false) }

            try {
                val width = exportOptions.customWidth ?: canvasWidth
                val height = exportOptions.customHeight ?: canvasHeight

                val bitmap = createBitmap(width, height, exportOptions.transparentBackground)

                val fileName = "sketch_export_${System.currentTimeMillis()}.png"
                // Save to MediaStore (Photos app) instead of private storage
                val saveResult = saveDrawingUseCase(bitmap, fileName, saveToMediaStore = true)

                saveResult.fold(
                    onSuccess = { filePath ->
                        // Only set the path for sharing, don't save to database
                        _uiState.update {
                            it.copy(
                                isSaving = false,
                                lastExportedPath = filePath
                            )
                        }
                    },
                    onFailure = { error ->
                        _uiState.update {
                            it.copy(isSaving = false, saveError = error.message)
                        }
                    }
                )
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(isSaving = false, saveError = e.message ?: "Failed to share sketch")
                }
            }
        }
    }

    // Image-related methods
    fun addImage(imagePath: String, x: Float, y: Float, width: Float, height: Float) {
        val newImage = com.hotmail.arehmananis.sketchapp.domain.model.ImageElement(
            id = UUID.randomUUID().toString(),
            imagePath = imagePath,
            x = x,
            y = y,
            width = width,
            height = height
        )
        _uiState.update {
            it.copy(imageElements = it.imageElements + newImage)
        }
    }

    fun selectImage(imageId: String?) {
        _uiState.update { it.copy(selectedImageId = imageId) }
    }

    fun moveImage(imageId: String, deltaX: Float, deltaY: Float) {
        val updatedImages = _uiState.value.imageElements.map { image ->
            if (image.id == imageId) {
                image.copy(x = image.x + deltaX, y = image.y + deltaY)
            } else {
                image
            }
        }
        _uiState.update { it.copy(imageElements = updatedImages) }
    }

    fun resizeImage(imageId: String, newWidth: Float, newHeight: Float) {
        val updatedImages = _uiState.value.imageElements.map { image ->
            if (image.id == imageId) {
                image.copy(
                    width = newWidth.coerceIn(50f, 2000f),
                    height = newHeight.coerceIn(50f, 2000f)
                )
            } else {
                image
            }
        }
        _uiState.update { it.copy(imageElements = updatedImages) }
    }

    fun deleteImage(imageId: String) {
        val updatedImages = _uiState.value.imageElements.filter { it.id != imageId }
        _uiState.update {
            it.copy(
                imageElements = updatedImages,
                selectedImageId = null
            )
        }
    }

    fun rotateImage(imageId: String, rotation: Float) {
        val updatedImages = _uiState.value.imageElements.map { image ->
            if (image.id == imageId) {
                image.copy(rotation = rotation)
            } else {
                image
            }
        }
        _uiState.update { it.copy(imageElements = updatedImages) }
    }
}

/**
 * UI state for drawing screen
 */
data class DrawingUiState(
    val isLoading: Boolean = false,
    val loadError: String? = null,
    val loadedSketchId: String? = null,
    val paths: List<DrawingPath> = emptyList(),
    val currentPath: DrawingPath? = null,
    val currentBrush: BrushType = BrushType.PEN,
    val currentColor: Int = android.graphics.Color.BLACK, // ARGB as Int
    val strokeWidth: Float = 3f,
    val opacity: Float = 1f,
    val shapeTool: ShapeTool = ShapeTool.NONE,
    val isFilled: Boolean = false,
    val emojiElements: List<com.hotmail.arehmananis.sketchapp.domain.model.EmojiElement> = emptyList(),
    val selectedEmojiId: String? = null,
    val imageElements: List<com.hotmail.arehmananis.sketchapp.domain.model.ImageElement> = emptyList(),
    val selectedImageId: String? = null,
    val showEmojiPicker: Boolean = false,
    val showShareDialog: Boolean = false,
    val lastExportedPath: String? = null,
    val canUndo: Boolean = false,
    val canRedo: Boolean = false,
    val isSaving: Boolean = false,
    val saveSuccess: Boolean = false,
    val saveError: String? = null
)
