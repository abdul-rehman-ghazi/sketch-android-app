package com.hotmail.arehmananis.sketchapp.presentation.feature.drawing

import android.graphics.Bitmap
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
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
                title = { Text("Drawing") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, "Back")
                    }
                },
                actions = {
                    IconButton(
                        onClick = { viewModel.undo() },
                        enabled = uiState.canUndo
                    ) {
                        Icon(Icons.Default.Undo, "Undo")
                    }
                    IconButton(
                        onClick = { viewModel.redo() },
                        enabled = uiState.canRedo
                    ) {
                        Icon(Icons.Default.Redo, "Redo")
                    }
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
                        Icon(Icons.Default.Save, "Save")
                    }
                    IconButton(onClick = { viewModel.clearCanvas() }) {
                        Icon(Icons.Default.DeleteForever, "Clear")
                    }
                }
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

            // Saving indicator
            if (uiState.isSaving) {
                LinearProgressIndicator(
                    modifier = Modifier.fillMaxWidth()
                )
            }

            // Error snackbar
            uiState.saveError?.let { error ->
                Snackbar(
                    modifier = Modifier.padding(16.dp),
                    action = {
                        TextButton(onClick = { viewModel.clearSaveState() }) {
                            Text("Dismiss")
                        }
                    }
                ) {
                    Text(error)
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

    // Fill with white background
    canvas.drawColor(android.graphics.Color.WHITE)

    // Draw each path
    paths.forEach { drawingPath ->
        val paint = android.graphics.Paint().apply {
            color = drawingPath.color.toInt()
            strokeWidth = drawingPath.strokeWidth
            style = android.graphics.Paint.Style.STROKE
            strokeCap = android.graphics.Paint.Cap.ROUND
            strokeJoin = android.graphics.Paint.Join.ROUND
            isAntiAlias = true
            alpha = (drawingPath.opacity * 255).toInt()
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

    return bitmap
}
