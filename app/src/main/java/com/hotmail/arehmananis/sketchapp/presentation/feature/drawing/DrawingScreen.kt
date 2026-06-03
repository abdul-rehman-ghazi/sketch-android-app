package com.hotmail.arehmananis.sketchapp.presentation.feature.drawing

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Snackbar
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import org.koin.androidx.compose.koinViewModel

@Composable
fun DrawingScreen(
    sketchId: String? = null,
    viewModel: DrawingViewModel = koinViewModel(),
    onBack: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current

    var canvasWidth by remember { mutableIntStateOf(0) }
    var canvasHeight by remember { mutableIntStateOf(0) }

    LaunchedEffect(sketchId) {
        viewModel.initialize(sketchId)
    }

    LaunchedEffect(uiState.saveSuccess, uiState.saveError) {
        if (uiState.saveSuccess) onBack()
    }

    var previousExportPath by remember { mutableStateOf<String?>(null) }
    LaunchedEffect(uiState.lastExportedPath) {
        uiState.lastExportedPath?.let { path ->
            if (path != previousExportPath) {
                previousExportPath = path
                shareImage(context, path)
            }
        }
    }

    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        uri?.let { viewModel.onImagePicked(it, context, canvasWidth, canvasHeight) }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        DrawingCanvas(
                paths = uiState.paths,
                currentPath = uiState.currentPath,
                emojiElements = uiState.emojiElements,
                imageElements = uiState.imageElements,
                selectedEmojiId = uiState.selectedEmojiId,
                selectedImageId = uiState.selectedImageId,
                onDrawStart = viewModel::onDrawStart,
                onDraw = viewModel::onDraw,
                onDrawEnd = viewModel::onDrawEnd,
                onEmojiPinchStart = viewModel::onEmojiPinchStart,
                onEmojiPinchUpdate = { zoom, rotation -> viewModel.onEmojiPinchUpdate(zoom, rotation) },
                onEmojiPinchEnd = viewModel::onEmojiPinchEnd,
                onImagePinchStart = viewModel::onImagePinchStart,
                onImagePinchUpdate = { zoom, rotDelta -> viewModel.onImagePinchUpdate(zoom, rotDelta) },
                onImagePinchEnd = viewModel::onImagePinchEnd,
                onCanvasTap = { offset ->
                    val hitImage = uiState.imageElements.firstOrNull { img ->
                        val halfW = img.width / 2f
                        val halfH = img.height / 2f
                        offset.x >= img.x - halfW && offset.x <= img.x + halfW &&
                        offset.y >= img.y - halfH && offset.y <= img.y + halfH
                    }
                    if (hitImage != null) {
                        viewModel.selectImage(hitImage.id)
                        viewModel.selectEmoji(null)
                    } else {
                        viewModel.selectEmoji(null)
                        viewModel.selectImage(null)
                    }
                },
                modifier = Modifier.fillMaxSize(),
                onCanvasSizeChanged = { width, height ->
                    canvasWidth = width
                    canvasHeight = height
                }
            )

            uiState.imageElements.forEach { image ->
                ImageOverlay(
                    image = image,
                    isSelected = image.id == uiState.selectedImageId,
                    onDrag = { dx, dy -> viewModel.moveImage(image.id, dx, dy) },
                    onDelete = { viewModel.deleteImage(image.id) }
                )
            }

            uiState.emojiElements.forEach { emoji ->
                EmojiOverlay(
                    emoji = emoji,
                    isSelected = emoji.id == uiState.selectedEmojiId,
                    onTap = { viewModel.selectEmoji(emoji.id) },
                    onDrag = { dx, dy -> viewModel.moveEmoji(emoji.id, dx, dy) },
                    onDelete = { viewModel.deleteEmoji(emoji.id) }
                )
            }

            if (uiState.isLoading) {
                LoadingOverlay()
            }

            if (uiState.isSaving) {
                LinearProgressIndicator(
                    modifier = Modifier.fillMaxWidth(),
                    color = MaterialTheme.colorScheme.primary,
                    trackColor = MaterialTheme.colorScheme.surfaceVariant
                )
            }

            uiState.loadError?.let { error ->
                ErrorSnackbar(
                    message = "Error: $error",
                    actionLabel = "Go Back",
                    onAction = onBack
                )
            }

            uiState.saveError?.let { error ->
                ErrorSnackbar(
                    message = error,
                    actionLabel = "Dismiss",
                    onAction = viewModel::clearSaveState
                )
            }

            if (uiState.showEmojiPicker) {
                EmojiPickerDialog(
                    onEmojiSelected = { emoji ->
                        viewModel.addEmoji(emoji, canvasWidth / 2f, canvasHeight / 2f)
                    },
                    onDismiss = viewModel::toggleEmojiPicker
                )
            }

            if (uiState.showShareDialog) {
                ExportOptionsDialog(
                    originalWidth = canvasWidth,
                    originalHeight = canvasHeight,
                    onExport = { exportOptions ->
                        viewModel.shareSketch(
                            canvasWidth = canvasWidth,
                            canvasHeight = canvasHeight,
                            createBitmap = { width, height, transparent ->
                                createBitmapFromPaths(
                                    paths = uiState.paths,
                                    emojiElements = uiState.emojiElements,
                                    imageElements = uiState.imageElements,
                                    originalWidth = canvasWidth,
                                    originalHeight = canvasHeight,
                                    targetWidth = width,
                                    targetHeight = height,
                                    transparentBackground = transparent
                                )
                            },
                            exportOptions = exportOptions
                        )
                    },
                    onDismiss = viewModel::toggleShareDialog
                )
            }

        DrawingPanel(
            currentBrush = uiState.currentBrush,
            currentColor = Color(uiState.currentColor),
            strokeWidth = uiState.strokeWidth,
            currentShapeTool = uiState.shapeTool,
            isFilled = uiState.isFilled,
            canUndo = uiState.canUndo,
            canRedo = uiState.canRedo,
            isSaving = uiState.isSaving,
            hasContent = uiState.paths.isNotEmpty() || uiState.imageElements.isNotEmpty() || uiState.emojiElements.isNotEmpty(),
            onBack = onBack,
            onUndo = viewModel::undo,
            onRedo = viewModel::redo,
            onImportImage = {
                imagePickerLauncher.launch(
                    androidx.activity.result.PickVisualMediaRequest(
                        ActivityResultContracts.PickVisualMedia.ImageOnly
                    )
                )
            },
            onShare = viewModel::toggleShareDialog,
            onSave = {
                viewModel.saveSketch(
                    canvasWidth = canvasWidth,
                    canvasHeight = canvasHeight,
                    createBitmap = {
                        createBitmapFromPaths(
                            paths = uiState.paths,
                            emojiElements = uiState.emojiElements,
                            imageElements = uiState.imageElements,
                            originalWidth = canvasWidth,
                            originalHeight = canvasHeight,
                            targetWidth = canvasWidth,
                            targetHeight = canvasHeight,
                            transparentBackground = false
                        )
                    }
                )
            },
            onClear = viewModel::clearCanvas,
            onToggleEmoji = viewModel::toggleEmojiPicker,
            onBrushChange = viewModel::setBrush,
            onColorChange = viewModel::setColor,
            onStrokeWidthChange = viewModel::setStrokeWidth,
            onShapeToolChange = viewModel::setShapeTool,
            onFilledChange = viewModel::setIsFilled,
            modifier = Modifier.align(Alignment.BottomCenter)
        )
    }
}

@Composable
private fun LoadingOverlay() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            CircularProgressIndicator()
            Spacer(modifier = Modifier.height(16.dp))
            Text("Loading sketch...")
        }
    }
}

@Composable
private fun ErrorSnackbar(
    message: String,
    actionLabel: String,
    onAction: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        contentAlignment = Alignment.BottomCenter
    ) {
        Snackbar(
            modifier = Modifier.padding(bottom = 16.dp),
            action = {
                TextButton(onClick = onAction) {
                    Text(
                        text = actionLabel,
                        style = MaterialTheme.typography.labelLarge
                    )
                }
            },
            containerColor = MaterialTheme.colorScheme.errorContainer,
            contentColor = MaterialTheme.colorScheme.onErrorContainer,
            shape = RoundedCornerShape(12.dp)
        ) {
            Text(
                text = message,
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}