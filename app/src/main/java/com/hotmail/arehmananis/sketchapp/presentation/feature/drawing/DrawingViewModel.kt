package com.hotmail.arehmananis.sketchapp.presentation.feature.drawing

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hotmail.arehmananis.sketchapp.domain.model.BrushConfig
import com.hotmail.arehmananis.sketchapp.domain.model.BrushType
import com.hotmail.arehmananis.sketchapp.domain.model.DrawingPath
import com.hotmail.arehmananis.sketchapp.domain.model.ImageElement
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
import java.io.File
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

    // Unified undo/redo stacks
    private sealed class UndoAction {
        data class PathAdded(val path: DrawingPath) : UndoAction()
        data class EmojiAdded(val emoji: com.hotmail.arehmananis.sketchapp.domain.model.EmojiElement) :
            UndoAction()

        data class EmojiMoved(
            val emojiId: String,
            val prevX: Float,
            val prevY: Float,
            val newX: Float,
            val newY: Float
        ) : UndoAction()

        data class EmojiResized(val emojiId: String, val prevSize: Float, val newSize: Float) :
            UndoAction()

        data class EmojiRotated(
            val emojiId: String,
            val prevRotation: Float,
            val newRotation: Float
        ) : UndoAction()

        data class ImageAdded(val image: ImageElement) : UndoAction()
        data class ImageMoved(
            val imageId: String,
            val prevX: Float, val prevY: Float,
            val newX: Float, val newY: Float
        ) : UndoAction()
        data class ImageResized(
            val imageId: String,
            val prevWidth: Float, val prevHeight: Float,
            val newWidth: Float, val newHeight: Float
        ) : UndoAction()
        data class ImageDeleted(val image: ImageElement) : UndoAction()
    }

    private val undoStack = mutableListOf<UndoAction>()
    private val redoStack = mutableListOf<UndoAction>()

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

        undoStack.add(UndoAction.PathAdded(currentPath))
        redoStack.clear()

        val updatedPaths = _uiState.value.paths + currentPath
        _uiState.update {
            it.copy(
                paths = updatedPaths,
                currentPath = null,
                canUndo = true,
                canRedo = false
            )
        }
    }

    fun undo() {
        val action = undoStack.removeLastOrNull() ?: return
        redoStack.add(action)

        when (action) {
            is UndoAction.PathAdded -> {
                val paths = undoStack.filterIsInstance<UndoAction.PathAdded>().map { it.path }
                _uiState.update {
                    it.copy(
                        paths = paths,
                        canUndo = undoStack.isNotEmpty(),
                        canRedo = true
                    )
                }
            }

            is UndoAction.EmojiAdded -> {
                _uiState.update {
                    it.copy(
                        emojiElements = it.emojiElements.filter { e -> e.id != action.emoji.id },
                        canUndo = undoStack.isNotEmpty(),
                        canRedo = true
                    )
                }
            }

            is UndoAction.EmojiMoved -> {
                _uiState.update {
                    it.copy(
                        emojiElements = it.emojiElements.map { e ->
                            if (e.id == action.emojiId) e.copy(
                                x = action.prevX,
                                y = action.prevY
                            ) else e
                        },
                        canUndo = undoStack.isNotEmpty(),
                        canRedo = true
                    )
                }
            }

            is UndoAction.EmojiResized -> {
                _uiState.update {
                    it.copy(
                        emojiElements = it.emojiElements.map { e ->
                            if (e.id == action.emojiId) e.copy(size = action.prevSize) else e
                        },
                        canUndo = undoStack.isNotEmpty(),
                        canRedo = true
                    )
                }
            }

            is UndoAction.EmojiRotated -> {
                _uiState.update {
                    it.copy(
                        emojiElements = it.emojiElements.map { e ->
                            if (e.id == action.emojiId) e.copy(rotation = action.prevRotation) else e
                        },
                        canUndo = undoStack.isNotEmpty(),
                        canRedo = true
                    )
                }
            }

            is UndoAction.ImageAdded -> {
                _uiState.update {
                    it.copy(
                        imageElements = it.imageElements.filter { img -> img.id != action.image.id },
                        canUndo = undoStack.isNotEmpty(),
                        canRedo = true
                    )
                }
            }

            is UndoAction.ImageMoved -> {
                _uiState.update {
                    it.copy(
                        imageElements = it.imageElements.map { img ->
                            if (img.id == action.imageId) img.copy(x = action.prevX, y = action.prevY) else img
                        },
                        canUndo = undoStack.isNotEmpty(),
                        canRedo = true
                    )
                }
            }

            is UndoAction.ImageResized -> {
                _uiState.update {
                    it.copy(
                        imageElements = it.imageElements.map { img ->
                            if (img.id == action.imageId) img.copy(width = action.prevWidth, height = action.prevHeight) else img
                        },
                        canUndo = undoStack.isNotEmpty(),
                        canRedo = true
                    )
                }
            }

            is UndoAction.ImageDeleted -> {
                _uiState.update {
                    it.copy(
                        imageElements = it.imageElements + action.image,
                        canUndo = undoStack.isNotEmpty(),
                        canRedo = true
                    )
                }
            }
        }
    }

    fun redo() {
        val action = redoStack.removeLastOrNull() ?: return
        undoStack.add(action)

        when (action) {
            is UndoAction.PathAdded -> {
                val paths = undoStack.filterIsInstance<UndoAction.PathAdded>().map { it.path }
                _uiState.update {
                    it.copy(
                        paths = paths,
                        canUndo = true,
                        canRedo = redoStack.isNotEmpty()
                    )
                }
            }

            is UndoAction.EmojiAdded -> {
                _uiState.update {
                    it.copy(
                        emojiElements = it.emojiElements + action.emoji,
                        canUndo = true,
                        canRedo = redoStack.isNotEmpty()
                    )
                }
            }

            is UndoAction.EmojiMoved -> {
                _uiState.update {
                    it.copy(
                        emojiElements = it.emojiElements.map { e ->
                            if (e.id == action.emojiId) e.copy(
                                x = action.newX,
                                y = action.newY
                            ) else e
                        },
                        canUndo = true,
                        canRedo = redoStack.isNotEmpty()
                    )
                }
            }

            is UndoAction.EmojiResized -> {
                _uiState.update {
                    it.copy(
                        emojiElements = it.emojiElements.map { e ->
                            if (e.id == action.emojiId) e.copy(size = action.newSize) else e
                        },
                        canUndo = true,
                        canRedo = redoStack.isNotEmpty()
                    )
                }
            }

            is UndoAction.EmojiRotated -> {
                _uiState.update {
                    it.copy(
                        emojiElements = it.emojiElements.map { e ->
                            if (e.id == action.emojiId) e.copy(rotation = action.newRotation) else e
                        },
                        canUndo = true,
                        canRedo = redoStack.isNotEmpty()
                    )
                }
            }

            is UndoAction.ImageAdded -> {
                _uiState.update {
                    it.copy(
                        imageElements = it.imageElements + action.image,
                        canUndo = true,
                        canRedo = redoStack.isNotEmpty()
                    )
                }
            }

            is UndoAction.ImageMoved -> {
                _uiState.update {
                    it.copy(
                        imageElements = it.imageElements.map { img ->
                            if (img.id == action.imageId) img.copy(x = action.newX, y = action.newY) else img
                        },
                        canUndo = true,
                        canRedo = redoStack.isNotEmpty()
                    )
                }
            }

            is UndoAction.ImageResized -> {
                _uiState.update {
                    it.copy(
                        imageElements = it.imageElements.map { img ->
                            if (img.id == action.imageId) img.copy(width = action.newWidth, height = action.newHeight) else img
                        },
                        canUndo = true,
                        canRedo = redoStack.isNotEmpty()
                    )
                }
            }

            is UndoAction.ImageDeleted -> {
                _uiState.update {
                    it.copy(
                        imageElements = it.imageElements.filter { img -> img.id != action.image.id },
                        canUndo = true,
                        canRedo = redoStack.isNotEmpty()
                    )
                }
            }
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
        undoStack.clear()
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
                        val paths = sketch.drawingPaths ?: emptyList()
                        val emojis = sketch.emojiElements ?: emptyList()
                        val images = sketch.imageElements ?: emptyList()

                        undoStack.clear()
                        paths.forEach { undoStack.add(UndoAction.PathAdded(it)) }
                        redoStack.clear()

                        _uiState.update {
                            it.copy(
                                isLoading = false,
                                loadedSketchId = sketchId,
                                paths = paths,
                                emojiElements = emojis,
                                imageElements = images,
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
                            emojiElements = currentState.emojiElements,
                            imageElements = currentState.imageElements
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
            size = 64f
        )
        undoStack.add(UndoAction.EmojiAdded(newEmoji))
        redoStack.clear()
        _uiState.update {
            it.copy(
                emojiElements = it.emojiElements + newEmoji,
                showEmojiPicker = false,
                canUndo = true,
                canRedo = false
            )
        }
    }

    fun selectEmoji(emojiId: String?) {
        _uiState.update { it.copy(selectedEmojiId = emojiId) }
    }

    fun moveEmoji(emojiId: String, deltaX: Float, deltaY: Float) {
        val emoji = _uiState.value.emojiElements.find { it.id == emojiId } ?: return
        undoStack.add(
            UndoAction.EmojiMoved(
                emojiId,
                emoji.x,
                emoji.y,
                emoji.x + deltaX,
                emoji.y + deltaY
            )
        )
        redoStack.clear()
        _uiState.update {
            it.copy(
                emojiElements = it.emojiElements.map { e ->
                    if (e.id == emojiId) e.copy(x = emoji.x + deltaX, y = emoji.y + deltaY) else e
                },
                canUndo = true,
                canRedo = false
            )
        }
    }

    // Pinch-to-zoom + rotate for selected emoji — one undo entry per gesture
    private var pinchBaseSize: Float? = null
    private var pinchBaseRotation: Float? = null

    fun onEmojiPinchStart() {
        val emojiId = _uiState.value.selectedEmojiId ?: return
        val emoji = _uiState.value.emojiElements.find { it.id == emojiId } ?: return
        pinchBaseSize = emoji.size
        pinchBaseRotation = emoji.rotation
    }

    fun onEmojiPinchUpdate(cumulativeZoom: Float, totalRotationDelta: Float) {
        val emojiId = _uiState.value.selectedEmojiId ?: return
        val baseSize = pinchBaseSize ?: return
        val baseRotation = pinchBaseRotation ?: return
        val newSize = (baseSize * cumulativeZoom).coerceIn(24f, 240f)
        val newRotation = baseRotation + totalRotationDelta
        _uiState.update {
            it.copy(emojiElements = it.emojiElements.map { e ->
                if (e.id == emojiId) e.copy(size = newSize, rotation = newRotation) else e
            })
        }
    }

    fun onEmojiPinchEnd() {
        val emojiId = _uiState.value.selectedEmojiId ?: return
        val baseSize = pinchBaseSize ?: return
        val baseRotation = pinchBaseRotation ?: return
        val emoji = _uiState.value.emojiElements.find { it.id == emojiId } ?: return
        if (baseSize != emoji.size) {
            undoStack.add(UndoAction.EmojiResized(emojiId, baseSize, emoji.size))
            redoStack.clear()
        }
        if (baseRotation != emoji.rotation) {
            undoStack.add(UndoAction.EmojiRotated(emojiId, baseRotation, emoji.rotation))
            redoStack.clear()
        }
        if (baseSize != emoji.size || baseRotation != emoji.rotation) {
            _uiState.update { it.copy(canUndo = true, canRedo = false) }
        }
        pinchBaseSize = null
        pinchBaseRotation = null
    }

    fun deleteEmoji(emojiId: String) {
        _uiState.update {
            it.copy(
                emojiElements = it.emojiElements.filter { e -> e.id != emojiId },
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
    fun onImagePicked(uri: Uri, context: Context, canvasWidth: Int, canvasHeight: Int) {
        viewModelScope.launch {
            try {
                val opts = BitmapFactory.Options().apply { inJustDecodeBounds = true }
                context.contentResolver.openInputStream(uri)?.use {
                    BitmapFactory.decodeStream(it, null, opts)
                }
                val srcWidth = opts.outWidth.takeIf { it > 0 } ?: 512
                val srcHeight = opts.outHeight.takeIf { it > 0 } ?: 512

                val imagesDir = File(context.filesDir, "images").also { it.mkdirs() }
                val localFile = File(imagesDir, "${UUID.randomUUID()}.jpg")
                context.contentResolver.openInputStream(uri)?.use { input ->
                    localFile.outputStream().use { output -> input.copyTo(output) }
                }

                val maxDim = minOf(canvasWidth, canvasHeight) * 0.6f
                val scale = minOf(maxDim / srcWidth, maxDim / srcHeight, 1f)
                val w = (srcWidth * scale).coerceAtLeast(50f)
                val h = (srcHeight * scale).coerceAtLeast(50f)

                addImage(localFile.absolutePath, canvasWidth / 2f, canvasHeight / 2f, w, h)
            } catch (e: Exception) {
                _uiState.update { it.copy(saveError = "Failed to import image: ${e.message}") }
            }
        }
    }

    fun addImage(imagePath: String, x: Float, y: Float, width: Float, height: Float) {
        val newImage = ImageElement(
            id = UUID.randomUUID().toString(),
            imagePath = imagePath,
            x = x,
            y = y,
            width = width,
            height = height
        )
        undoStack.add(UndoAction.ImageAdded(newImage))
        redoStack.clear()
        _uiState.update {
            it.copy(
                imageElements = it.imageElements + newImage,
                canUndo = true,
                canRedo = false
            )
        }
    }

    fun selectImage(imageId: String?) {
        _uiState.update { it.copy(selectedImageId = imageId) }
    }

    fun moveImage(imageId: String, deltaX: Float, deltaY: Float) {
        val image = _uiState.value.imageElements.find { it.id == imageId } ?: return
        undoStack.add(UndoAction.ImageMoved(imageId, image.x, image.y, image.x + deltaX, image.y + deltaY))
        redoStack.clear()
        _uiState.update {
            it.copy(
                imageElements = it.imageElements.map { img ->
                    if (img.id == imageId) img.copy(x = image.x + deltaX, y = image.y + deltaY) else img
                },
                canUndo = true,
                canRedo = false
            )
        }
    }

    fun resizeImage(imageId: String, newWidth: Float, newHeight: Float) {
        val image = _uiState.value.imageElements.find { it.id == imageId } ?: return
        val w = newWidth.coerceIn(50f, 2000f)
        val h = newHeight.coerceIn(50f, 2000f)
        undoStack.add(UndoAction.ImageResized(imageId, image.width, image.height, w, h))
        redoStack.clear()
        _uiState.update {
            it.copy(
                imageElements = it.imageElements.map { img ->
                    if (img.id == imageId) img.copy(width = w, height = h) else img
                },
                canUndo = true,
                canRedo = false
            )
        }
    }

    fun deleteImage(imageId: String) {
        val image = _uiState.value.imageElements.find { it.id == imageId } ?: return
        undoStack.add(UndoAction.ImageDeleted(image))
        redoStack.clear()
        _uiState.update {
            it.copy(
                imageElements = it.imageElements.filter { img -> img.id != imageId },
                selectedImageId = null,
                canUndo = true,
                canRedo = false
            )
        }
    }

    fun rotateImage(imageId: String, rotation: Float) {
        _uiState.update {
            it.copy(imageElements = it.imageElements.map { img ->
                if (img.id == imageId) img.copy(rotation = rotation) else img
            })
        }
    }

    private var imagePinchBaseRotation: Float? = null
    private var imagePinchBaseWidth: Float? = null
    private var imagePinchBaseHeight: Float? = null

    fun onImagePinchStart() {
        val imageId = _uiState.value.selectedImageId ?: return
        val image = _uiState.value.imageElements.find { it.id == imageId } ?: return
        imagePinchBaseRotation = image.rotation
        imagePinchBaseWidth = image.width
        imagePinchBaseHeight = image.height
    }

    fun onImagePinchUpdate(cumulativeZoom: Float, totalRotationDelta: Float) {
        val imageId = _uiState.value.selectedImageId ?: return
        val baseRotation = imagePinchBaseRotation ?: return
        val baseWidth = imagePinchBaseWidth ?: return
        val baseHeight = imagePinchBaseHeight ?: return
        val newWidth = (baseWidth * cumulativeZoom).coerceIn(50f, 2000f)
        val newHeight = (baseHeight * cumulativeZoom).coerceIn(50f, 2000f)
        _uiState.update {
            it.copy(imageElements = it.imageElements.map { img ->
                if (img.id == imageId) img.copy(
                    width = newWidth,
                    height = newHeight,
                    rotation = baseRotation + totalRotationDelta
                ) else img
            })
        }
    }

    fun onImagePinchEnd() {
        val imageId = _uiState.value.selectedImageId ?: return
        val baseWidth = imagePinchBaseWidth ?: return
        val baseHeight = imagePinchBaseHeight ?: return
        val image = _uiState.value.imageElements.find { it.id == imageId } ?: return
        if (baseWidth != image.width || baseHeight != image.height) {
            undoStack.add(UndoAction.ImageResized(imageId, baseWidth, baseHeight, image.width, image.height))
            redoStack.clear()
            _uiState.update { it.copy(canUndo = true, canRedo = false) }
        }
        imagePinchBaseRotation = null
        imagePinchBaseWidth = null
        imagePinchBaseHeight = null
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
