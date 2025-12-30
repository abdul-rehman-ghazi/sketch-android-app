package com.hotmail.arehmananis.sketchapp.presentation.feature.drawing

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.CompositingStrategy
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import com.hotmail.arehmananis.sketchapp.domain.model.BrushType
import com.hotmail.arehmananis.sketchapp.domain.model.DrawingPath

/**
 * Canvas composable for drawing
 * Handles touch input and renders all drawing paths
 */
@Composable
fun DrawingCanvas(
    paths: List<DrawingPath>,
    currentPath: DrawingPath?,
    onDrawStart: (Offset) -> Unit,
    onDraw: (Offset) -> Unit,
    onDrawEnd: () -> Unit,
    modifier: Modifier = Modifier,
    backgroundColor: Color = Color.White,
    onCanvasSizeChanged: ((width: Int, height: Int) -> Unit)? = null
) {
    Canvas(
        modifier = modifier
            .fillMaxSize()
            .graphicsLayer(
                alpha = 0.99f,
                compositingStrategy = CompositingStrategy.Offscreen
            )
            .pointerInput(Unit) {
                detectDragGestures(
                    onDragStart = { offset ->
                        onDrawStart(offset)
                    },
                    onDrag = { change, _ ->
                        onDraw(change.position)
                    },
                    onDragEnd = {
                        onDrawEnd()
                    }
                )
            }
    ) {
        // Report canvas size when it changes
        onCanvasSizeChanged?.invoke(size.width.toInt(), size.height.toInt())
        // Draw background
        drawRect(color = backgroundColor)

        // Draw all completed paths
        paths.forEach { path ->
            drawPath(path)
        }

        // Draw current path being drawn
        currentPath?.let { path ->
            drawPath(path)
        }
    }
}

/**
 * Draw a single drawing path with brush-specific rendering
 */
private fun DrawScope.drawPath(drawingPath: DrawingPath) {
    if (drawingPath.points.size < 2) return

    // Convert points to Compose Path
    val path = Path()
    drawingPath.points.forEachIndexed { index, point ->
        if (index == 0) {
            path.moveTo(point.x, point.y)
        } else {
            path.lineTo(point.x, point.y)
        }
    }

    // Apply brush-specific rendering
    when (drawingPath.brush) {
        BrushType.PEN -> drawPen(path, drawingPath)
        BrushType.PENCIL -> drawPencil(path, drawingPath)
        BrushType.ERASER -> drawEraser(path, drawingPath)
        BrushType.MARKER -> drawMarker(path, drawingPath)
        BrushType.HIGHLIGHTER -> drawHighlighter(path, drawingPath)
        BrushType.AIRBRUSH -> drawAirbrush(path, drawingPath)
        BrushType.CALLIGRAPHY -> drawCalligraphy(path, drawingPath)
    }
}

/**
 * Pen: Solid stroke with full opacity
 */
private fun DrawScope.drawPen(path: Path, config: DrawingPath) {
    drawPath(
        path = path,
        color = Color(config.color),
        alpha = config.opacity,
        style = Stroke(
            width = config.strokeWidth,
            cap = StrokeCap.Round,
            join = StrokeJoin.Round
        )
    )
}

/**
 * Pencil: Semi-transparent, textured appearance
 */
private fun DrawScope.drawPencil(path: Path, config: DrawingPath) {
    val color = Color(config.color)

    // Draw multiple semi-transparent strokes for texture
    for (i in 0..2) {
        drawPath(
            path = path,
            color = color,
            alpha = 0.3f * config.opacity,
            style = Stroke(
                width = config.strokeWidth + i * 0.5f,
                cap = StrokeCap.Round,
                join = StrokeJoin.Round
            )
        )
    }
}

/**
 * Eraser: Remove existing strokes using blend mode
 */
private fun DrawScope.drawEraser(path: Path, config: DrawingPath) {
    drawPath(
        path = path,
        color = Color.Transparent,
        blendMode = BlendMode.Clear,
        style = Stroke(
            width = config.strokeWidth,
            cap = StrokeCap.Round,
            join = StrokeJoin.Round
        )
    )
}

/**
 * Marker: Semi-transparent with saturation
 */
private fun DrawScope.drawMarker(path: Path, config: DrawingPath) {
    drawPath(
        path = path,
        color = Color(config.color),
        alpha = 0.6f * config.opacity,
        style = Stroke(
            width = config.strokeWidth,
            cap = StrokeCap.Square,
            join = StrokeJoin.Miter
        )
    )
}

/**
 * Highlighter: Very transparent, wide strokes
 */
private fun DrawScope.drawHighlighter(path: Path, config: DrawingPath) {
    drawPath(
        path = path,
        color = Color(config.color),
        alpha = 0.3f * config.opacity,
        style = Stroke(
            width = config.strokeWidth,
            cap = StrokeCap.Butt,
            join = StrokeJoin.Miter
        )
    )
}

/**
 * Airbrush: Multiple semi-transparent strokes with blur effect
 */
private fun DrawScope.drawAirbrush(path: Path, config: DrawingPath) {
    val color = Color(config.color)

    // Draw multiple expanding strokes for airbrush effect
    for (i in 0..5) {
        drawPath(
            path = path,
            color = color,
            alpha = 0.05f * config.opacity,
            style = Stroke(
                width = config.strokeWidth + i * 2f,
                cap = StrokeCap.Round,
                join = StrokeJoin.Round
            )
        )
    }
}

/**
 * Calligraphy: Cut-marker style with flat chisel tip
 */
private fun DrawScope.drawCalligraphy(path: Path, config: DrawingPath) {
    drawPath(
        path = path,
        color = Color(config.color),
        alpha = config.opacity,
        style = Stroke(
            width = config.strokeWidth,
            cap = StrokeCap.Square,
            join = StrokeJoin.Miter,
        )
    )
}
