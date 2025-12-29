package com.hotmail.arehmananis.sketchapp.presentation.feature.drawing

import android.graphics.Bitmap
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
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

    // Initialize - load existing sketch or start fresh
    LaunchedEffect(sketchId) {
        viewModel.initialize(sketchId)
    }

    // Get canvas dimensions
    val configuration = LocalConfiguration.current
    val density = LocalDensity.current
    val canvasWidth = with(density) { configuration.screenWidthDp.dp.toPx().toInt() }
    val canvasHeight = with(density) { configuration.screenHeightDp.dp.toPx().toInt() }

    // Show save success/error
    LaunchedEffect(uiState.saveSuccess, uiState.saveError) {
        if (uiState.saveSuccess) {
            // Navigate back on success
            onBack()
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
                            imageVector = Icons.Default.ArrowBack,
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
                            imageVector = Icons.Default.Undo,
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
                            imageVector = Icons.Default.Redo,
                            contentDescription = "Redo",
                            tint = if (uiState.canRedo) {
                                MaterialTheme.colorScheme.onSurface
                            } else {
                                MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f)
                            }
                        )
                    }
                    // Save button
                    IconButton(
                        onClick = {
                            // Save sketch - create bitmap from current paths
                            viewModel.saveSketch(canvasWidth, canvasHeight) {
                                createBitmapFromPaths(
                                    paths = uiState.paths,
                                    width = canvasWidth,
                                    height = canvasHeight
                                )
                            }
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
                currentColor = Color(uiState.currentColor.toULong()),
                strokeWidth = uiState.strokeWidth,
                onBrushChange = viewModel::setBrush,
                onColorChange = viewModel::setColor,
                onStrokeWidthChange = viewModel::setStrokeWidth
            )
        }
    ) { padding ->
        Box(modifier = Modifier.padding(padding)) {
            DrawingCanvas(
                paths = uiState.paths,
                currentPath = uiState.currentPath,
                onDrawStart = viewModel::onDrawStart,
                onDraw = viewModel::onDraw,
                onDrawEnd = viewModel::onDrawEnd,
                modifier = Modifier.fillMaxSize()
            )

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
        }
    }
}

/**
 * Helper function to create a bitmap from drawing paths
 * Renders all paths to an Android Bitmap
 */
private fun createBitmapFromPaths(
    paths: List<com.hotmail.arehmananis.sketchapp.domain.model.DrawingPath>,
    width: Int,
    height: Int
): Bitmap {
    val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
    val canvas = android.graphics.Canvas(bitmap)

    // Fill with white background (on canvas, beneath the layer)
    canvas.drawColor(android.graphics.Color.WHITE)

    // Save layer to support blend modes like CLEAR
    val layerPaint = android.graphics.Paint()
    canvas.saveLayer(0f, 0f, width.toFloat(), height.toFloat(), layerPaint)

    // Draw each path
    paths.forEach { drawingPath ->
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

    // Restore layer
    canvas.restore()

    return bitmap
}
