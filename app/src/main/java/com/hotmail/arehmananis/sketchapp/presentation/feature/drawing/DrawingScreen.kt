package com.hotmail.arehmananis.sketchapp.presentation.feature.drawing

import android.Manifest
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.unit.sp
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Redo
import androidx.compose.material.icons.automirrored.filled.Undo
import androidx.compose.material.icons.filled.DeleteForever
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.EmojiEmotions
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.FloatingActionButton
import android.content.Intent
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.foundation.background
import androidx.compose.ui.draw.rotate
import androidx.compose.foundation.border
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Snackbar
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import androidx.core.graphics.createBitmap
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.hotmail.arehmananis.sketchapp.presentation.common.components.GradientButton
import org.koin.androidx.compose.koinViewModel

/**
 * Drawing screen with canvas and toolbar
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DrawingScreen(
    sketchId: String? = null,
    viewModel: DrawingViewModel = koinViewModel(),
    onBack: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val context = androidx.compose.ui.platform.LocalContext.current

    // Initialize - load existing sketch or start fresh
    LaunchedEffect(sketchId) {
        viewModel.initialize(sketchId)
    }

    // Track actual canvas dimensions (will be updated by DrawingCanvas)
    var canvasWidth by remember { mutableStateOf(0) }
    var canvasHeight by remember { mutableStateOf(0) }

    // Show save success/error
    LaunchedEffect(uiState.saveSuccess, uiState.saveError) {
        if (uiState.saveSuccess) {
            // Navigate back on success
            onBack()
        }
    }

    // Auto-trigger share sheet when lastExportedPath is set from share action
    var previousExportPath by remember { mutableStateOf<String?>(null) }
    LaunchedEffect(uiState.lastExportedPath) {
        uiState.lastExportedPath?.let { path ->
            if (path != previousExportPath) {
                previousExportPath = path
                shareImage(context, path)
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Drawing",
                        style = MaterialTheme.typography.titleLarge
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                },
                actions = {
                    // Undo button
                    IconButton(
                        onClick = { viewModel.undo() },
                        enabled = uiState.canUndo
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.Undo,
                            contentDescription = "Undo",
                            tint = if (uiState.canUndo) {
                                MaterialTheme.colorScheme.onSurface
                            } else {
                                MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f)
                            }
                        )
                    }
                    // Redo button
                    IconButton(
                        onClick = { viewModel.redo() },
                        enabled = uiState.canRedo
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.Redo,
                            contentDescription = "Redo",
                            tint = if (uiState.canRedo) {
                                MaterialTheme.colorScheme.onSurface
                            } else {
                                MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f)
                            }
                        )
                    }
                    // Share button
                    IconButton(
                        onClick = { viewModel.toggleShareDialog() },
                        enabled = !uiState.isSaving && uiState.paths.isNotEmpty()
                    ) {
                        Icon(
                            imageVector = Icons.Default.Share,
                            contentDescription = "Share",
                            tint = if (!uiState.isSaving && uiState.paths.isNotEmpty()) {
                                MaterialTheme.colorScheme.onSurface
                            } else {
                                MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f)
                            }
                        )
                    }
                    // Save button
                    IconButton(
                        onClick = {
                            viewModel.saveSketch(
                                canvasWidth = canvasWidth,
                                canvasHeight = canvasHeight,
                                createBitmap = {
                                    createBitmapFromPaths(
                                        paths = uiState.paths,
                                        emojiElements = uiState.emojiElements,
                                        originalWidth = canvasWidth,
                                        originalHeight = canvasHeight,
                                        targetWidth = canvasWidth,
                                        targetHeight = canvasHeight,
                                        transparentBackground = false
                                    )
                                }
                            )
                        },
                        enabled = !uiState.isSaving && uiState.paths.isNotEmpty()
                    ) {
                        Icon(
                            imageVector = Icons.Default.Save,
                            contentDescription = "Save",
                            tint = if (!uiState.isSaving && uiState.paths.isNotEmpty()) {
                                MaterialTheme.colorScheme.primary
                            } else {
                                MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f)
                            }
                        )
                    }
                    // Clear button
                    IconButton(onClick = { viewModel.clearCanvas() }) {
                        Icon(
                            imageVector = Icons.Default.DeleteForever,
                            contentDescription = "Clear",
                            tint = MaterialTheme.colorScheme.error
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    titleContentColor = MaterialTheme.colorScheme.onSurface
                )
            )
        },
        bottomBar = {
            DrawingToolbar(
                currentBrush = uiState.currentBrush,
                currentColor = Color(uiState.currentColor),
                strokeWidth = uiState.strokeWidth,
                currentShapeTool = uiState.shapeTool,
                isFilled = uiState.isFilled,
                onBrushChange = viewModel::setBrush,
                onColorChange = viewModel::setColor,
                onStrokeWidthChange = viewModel::setStrokeWidth,
                onShapeToolChange = viewModel::setShapeTool,
                onFilledChange = viewModel::setIsFilled
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { viewModel.toggleEmojiPicker() },
                containerColor = MaterialTheme.colorScheme.primaryContainer
            ) {
                Icon(
                    imageVector = Icons.Default.EmojiEmotions,
                    contentDescription = "Add Emoji",
                    tint = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }
        }
    ) { padding ->
        Box(modifier = Modifier.padding(padding)) {
            DrawingCanvas(
                paths = uiState.paths,
                currentPath = uiState.currentPath,
                emojiElements = uiState.emojiElements,
                selectedEmojiId = uiState.selectedEmojiId,
                onDrawStart = viewModel::onDrawStart,
                onDraw = viewModel::onDraw,
                onDrawEnd = viewModel::onDrawEnd,
                onEmojiTap = viewModel::selectEmoji,
                onEmojiDrag = viewModel::moveEmoji,
                modifier = Modifier.fillMaxSize(),
                onCanvasSizeChanged = { width, height ->
                    canvasWidth = width
                    canvasHeight = height
                }
            )

            // Render emojis as text overlays
            uiState.emojiElements.forEach { emoji ->
                EmojiOverlay(
                    emoji = emoji,
                    isSelected = emoji.id == uiState.selectedEmojiId,
                    onTap = { viewModel.selectEmoji(emoji.id) },
                    onDrag = { deltaX, deltaY ->
                        viewModel.moveEmoji(emoji.id, deltaX, deltaY)
                    },
                    onDelete = { viewModel.deleteEmoji(emoji.id) }
                )
            }

            // Loading indicator
            if (uiState.isLoading) {
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

            // Saving indicator - modern gradient
            if (uiState.isSaving) {
                LinearProgressIndicator(
                    modifier = Modifier.fillMaxWidth(),
                    color = MaterialTheme.colorScheme.primary,
                    trackColor = MaterialTheme.colorScheme.surfaceVariant
                )
            }

            // Load error snackbar - modern styled
            uiState.loadError?.let { error ->
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    contentAlignment = Alignment.BottomCenter
                ) {
                    Snackbar(
                        modifier = Modifier.padding(bottom = 16.dp),
                        action = {
                            GradientButton(
                                text = "Go Back",
                                onClick = onBack
                            )
                        },
                        containerColor = MaterialTheme.colorScheme.errorContainer,
                        contentColor = MaterialTheme.colorScheme.onErrorContainer,
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text(
                            text = "Error: $error",
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
            }

            // Save error snackbar - modern styled
            uiState.saveError?.let { error ->
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    contentAlignment = Alignment.BottomCenter
                ) {
                    Snackbar(
                        modifier = Modifier.padding(bottom = 16.dp),
                        action = {
                            TextButton(
                                onClick = { viewModel.clearSaveState() }
                            ) {
                                Text(
                                    text = "Dismiss",
                                    style = MaterialTheme.typography.labelLarge
                                )
                            }
                        },
                        containerColor = MaterialTheme.colorScheme.errorContainer,
                        contentColor = MaterialTheme.colorScheme.onErrorContainer,
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text(
                            text = error,
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
            }

            // Show emoji picker dialog
            if (uiState.showEmojiPicker) {
                EmojiPickerDialog(
                    onEmojiSelected = { emoji ->
                        // Place emoji at center of canvas
                        viewModel.addEmoji(emoji, canvasWidth / 2f, canvasHeight / 2f)
                    },
                    onDismiss = { viewModel.toggleEmojiPicker() }
                )
            }

            // Show share options dialog
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
                    onDismiss = { viewModel.toggleShareDialog() }
                )
            }
        }
    }
}

/**
 * Emoji overlay component that can be moved and selected
 */
@Composable
private fun EmojiOverlay(
    emoji: com.hotmail.arehmananis.sketchapp.domain.model.EmojiElement,
    isSelected: Boolean,
    onTap: () -> Unit,
    onDrag: (Float, Float) -> Unit,
    onDelete: () -> Unit
) {
    val density = LocalDensity.current
    var dragOffset by remember { mutableStateOf(Offset.Zero) }

    Box(
        modifier = Modifier
            .offset(
                // Position from center by subtracting half the size
                x = with(density) { (emoji.x + dragOffset.x - emoji.size / 2).toDp() },
                y = with(density) { (emoji.y + dragOffset.y - emoji.size / 2).toDp() }
            )
            .size(with(density) { emoji.size.toDp() })
            .then(
                if (isSelected) {
                    Modifier
                        .background(
                            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.2f),
                            shape = CircleShape
                        )
                        .border(
                            width = 2.dp,
                            color = MaterialTheme.colorScheme.primary,
                            shape = CircleShape
                        )
                } else {
                    Modifier
                }
            )
            .pointerInput(Unit) {
                detectTapGestures(
                    onTap = { onTap() }
                )
            }
            .pointerInput(Unit) {
                detectDragGestures(
                    onDragStart = { onTap() },
                    onDrag = { change, dragAmount ->
                        change.consume()
                        dragOffset += Offset(dragAmount.x, dragAmount.y)
                    },
                    onDragEnd = {
                        onDrag(dragOffset.x, dragOffset.y)
                        dragOffset = Offset.Zero
                    }
                )
            }
            .rotate(emoji.rotation),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = emoji.emoji,
            fontSize = (emoji.size * 0.6f).sp,
            fontWeight = FontWeight.Bold
        )
    }
}

/**
 * Helper function to create a bitmap from drawing paths with scaling support
 * Renders all paths and emojis to an Android Bitmap
 */
private fun createBitmapFromPaths(
    paths: List<com.hotmail.arehmananis.sketchapp.domain.model.DrawingPath>,
    emojiElements: List<com.hotmail.arehmananis.sketchapp.domain.model.EmojiElement> = emptyList(),
    originalWidth: Int,
    originalHeight: Int,
    targetWidth: Int,
    targetHeight: Int,
    transparentBackground: Boolean = false
): Bitmap {
    // Calculate scale factors
    val scaleX = targetWidth.toFloat() / originalWidth.toFloat()
    val scaleY = targetHeight.toFloat() / originalHeight.toFloat()

    // Create bitmap with target size
    val bitmap = createBitmap(targetWidth, targetHeight, Bitmap.Config.ARGB_8888)
    val canvas = android.graphics.Canvas(bitmap)

    // Fill with white or transparent background
    if (transparentBackground) {
        canvas.drawColor(android.graphics.Color.TRANSPARENT)
    } else {
        canvas.drawColor(android.graphics.Color.WHITE)
    }

    // Scale the canvas
    canvas.scale(scaleX, scaleY)

    // Save layer to support blend modes like CLEAR
    val layerPaint = android.graphics.Paint()
    canvas.saveLayer(0f, 0f, originalWidth.toFloat(), originalHeight.toFloat(), layerPaint)

    // Draw each path
    paths.forEach { drawingPath ->
        // Check if this is a shape
        if (drawingPath.shapeTool != com.hotmail.arehmananis.sketchapp.domain.model.ShapeTool.NONE &&
            drawingPath.points.size >= 2) {
            drawShapeToBitmap(canvas, drawingPath)
        } else {
            // Regular freehand drawing
            val paint = android.graphics.Paint().apply {
                strokeWidth = drawingPath.strokeWidth
                style = android.graphics.Paint.Style.STROKE
                strokeCap = android.graphics.Paint.Cap.ROUND
                strokeJoin = android.graphics.Paint.Join.ROUND
                isAntiAlias = true

                // Handle eraser with CLEAR blend mode
                if (drawingPath.brush == com.hotmail.arehmananis.sketchapp.domain.model.BrushType.ERASER) {
                    xfermode = android.graphics.PorterDuffXfermode(android.graphics.PorterDuff.Mode.CLEAR)
                } else {
                    color = drawingPath.color.toInt()
                    alpha = (drawingPath.opacity * 255).toInt()
                }
            }

            val path = android.graphics.Path()
            drawingPath.points.forEachIndexed { index, point ->
                if (index == 0) {
                    path.moveTo(point.x, point.y)
                } else {
                    path.lineTo(point.x, point.y)
                }
            }

            canvas.drawPath(path, paint)
        }
    }

    // Draw emojis
    emojiElements.forEach { emoji ->
        val textPaint = android.graphics.Paint().apply {
            textSize = emoji.size * 0.6f
            isAntiAlias = true
            textAlign = android.graphics.Paint.Align.CENTER
        }

        // Save canvas state
        canvas.save()

        // Rotate if needed
        if (emoji.rotation != 0f) {
            canvas.rotate(emoji.rotation, emoji.x, emoji.y)
        }

        // Draw emoji
        canvas.drawText(emoji.emoji, emoji.x, emoji.y + emoji.size * 0.2f, textPaint)

        // Restore canvas state
        canvas.restore()
    }

    // Restore layer
    canvas.restore()

    return bitmap
}

/**
 * Draw shapes to Android Canvas for bitmap export
 */
private fun drawShapeToBitmap(
    canvas: android.graphics.Canvas,
    drawingPath: com.hotmail.arehmananis.sketchapp.domain.model.DrawingPath
) {
    val points = drawingPath.points
    if (points.size < 2) return

    val start = points.first()
    val end = points.last()

    val paint = android.graphics.Paint().apply {
        color = drawingPath.color.toInt()
        alpha = (drawingPath.opacity * 255).toInt()
        style = if (drawingPath.isFilled) {
            android.graphics.Paint.Style.FILL
        } else {
            android.graphics.Paint.Style.STROKE
        }
        strokeWidth = drawingPath.strokeWidth
        strokeCap = android.graphics.Paint.Cap.ROUND
        strokeJoin = android.graphics.Paint.Join.ROUND
        isAntiAlias = true
    }

    when (drawingPath.shapeTool) {
        com.hotmail.arehmananis.sketchapp.domain.model.ShapeTool.LINE -> {
            canvas.drawLine(start.x, start.y, end.x, end.y, paint)
        }
        com.hotmail.arehmananis.sketchapp.domain.model.ShapeTool.RECTANGLE -> {
            val rect = android.graphics.RectF(
                minOf(start.x, end.x),
                minOf(start.y, end.y),
                maxOf(start.x, end.x),
                maxOf(start.y, end.y)
            )
            canvas.drawRect(rect, paint)
        }
        com.hotmail.arehmananis.sketchapp.domain.model.ShapeTool.CIRCLE -> {
            val rect = android.graphics.RectF(
                minOf(start.x, end.x),
                minOf(start.y, end.y),
                maxOf(start.x, end.x),
                maxOf(start.y, end.y)
            )
            canvas.drawOval(rect, paint)
        }
        com.hotmail.arehmananis.sketchapp.domain.model.ShapeTool.ARROW -> {
            // Draw line
            canvas.drawLine(start.x, start.y, end.x, end.y, paint)

            // Draw arrow head
            val angle = kotlin.math.atan2((end.y - start.y).toDouble(), (end.x - start.x).toDouble())
            val arrowLength = drawingPath.strokeWidth * 3
            val arrowAngle = Math.PI / 6

            val arrowPoint1X = (end.x - arrowLength * kotlin.math.cos(angle - arrowAngle)).toFloat()
            val arrowPoint1Y = (end.y - arrowLength * kotlin.math.sin(angle - arrowAngle)).toFloat()
            val arrowPoint2X = (end.x - arrowLength * kotlin.math.cos(angle + arrowAngle)).toFloat()
            val arrowPoint2Y = (end.y - arrowLength * kotlin.math.sin(angle + arrowAngle)).toFloat()

            canvas.drawLine(end.x, end.y, arrowPoint1X, arrowPoint1Y, paint)
            canvas.drawLine(end.x, end.y, arrowPoint2X, arrowPoint2Y, paint)
        }
        com.hotmail.arehmananis.sketchapp.domain.model.ShapeTool.POLYGON -> {
            if (points.size < 3) return

            val path = android.graphics.Path()
            points.forEachIndexed { index, point ->
                if (index == 0) {
                    path.moveTo(point.x, point.y)
                } else {
                    path.lineTo(point.x, point.y)
                }
            }
            path.close()
            canvas.drawPath(path, paint)
        }
        com.hotmail.arehmananis.sketchapp.domain.model.ShapeTool.NONE -> {}
    }
}

/**
 * Share image using Android share sheet
 */
private fun shareImage(context: android.content.Context, imagePath: String) {
    try {
        // Determine if this is a content URI (Android 10+) or file path (Android 9)
        val imageUri = if (imagePath.startsWith("content://")) {
            // Already a content URI from MediaStore
            android.net.Uri.parse(imagePath)
        } else {
            // File path - use FileProvider
            val imageFile = java.io.File(imagePath)
            FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                imageFile
            )
        }

        val shareIntent = Intent().apply {
            action = Intent.ACTION_SEND
            putExtra(Intent.EXTRA_STREAM, imageUri)
            type = "image/png"
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }

        context.startActivity(Intent.createChooser(shareIntent, "Share Sketch"))
    } catch (e: Exception) {
        e.printStackTrace()
    }
}
