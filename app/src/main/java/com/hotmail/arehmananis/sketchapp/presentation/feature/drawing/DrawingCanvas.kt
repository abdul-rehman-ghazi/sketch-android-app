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
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import com.hotmail.arehmananis.sketchapp.domain.model.BrushType
import com.hotmail.arehmananis.sketchapp.domain.model.DrawingPath
import com.hotmail.arehmananis.sketchapp.domain.model.ShapeTool
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt

/**
 * Canvas composable for drawing
 * Handles touch input and renders all drawing paths and emoji elements
 */
@Composable
fun DrawingCanvas(
    paths: List<DrawingPath>,
    currentPath: DrawingPath?,
    emojiElements: List<com.hotmail.arehmananis.sketchapp.domain.model.EmojiElement> = emptyList(),
    selectedEmojiId: String? = null,
    onDrawStart: (Offset) -> Unit,
    onDraw: (Offset) -> Unit,
    onDrawEnd: () -> Unit,
    onEmojiTap: (String) -> Unit = {},
    onEmojiDrag: (String, Float, Float) -> Unit = { _, _, _ -> },
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

        // Draw emoji elements
        emojiElements.forEach { emoji ->
            drawEmoji(emoji, selectedEmojiId == emoji.id)
        }
    }
}

/**
 * Draw emoji element on canvas
 */
private fun DrawScope.drawEmoji(
    emoji: com.hotmail.arehmananis.sketchapp.domain.model.EmojiElement,
    isSelected: Boolean
) {
    // Draw selection indicator
    if (isSelected) {
        drawCircle(
            color = Color(0xFF6200EE),
            radius = emoji.size / 2 + 8f,
            center = Offset(emoji.x, emoji.y),
            alpha = 0.3f
        )
    }

    // Note: Drawing text on Canvas requires Android Paint
    // For simplicity, emojis will be drawn as overlay boxes in the actual implementation
    // This is a placeholder - actual emoji rendering happens via Text composables overlaid on canvas
}

/**
 * Draw a single drawing path with brush-specific rendering or shape
 */
private fun DrawScope.drawPath(drawingPath: DrawingPath) {
    if (drawingPath.points.isEmpty()) return

    // Check if this is a shape
    if (drawingPath.shapeTool != ShapeTool.NONE && drawingPath.points.size >= 2) {
        drawShape(drawingPath)
        return
    }

    // Regular freehand drawing
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

/**
 * Draw shapes based on shape tool type
 */
private fun DrawScope.drawShape(drawingPath: DrawingPath) {
    val points = drawingPath.points
    if (points.size < 2) return

    val start = points.first()
    val end = points.last()
    val color = Color(drawingPath.color)
    val style = if (drawingPath.isFilled) Fill else Stroke(
        width = drawingPath.strokeWidth,
        cap = StrokeCap.Round,
        join = StrokeJoin.Round
    )

    when (drawingPath.shapeTool) {
        ShapeTool.LINE -> drawLine(
            color = color,
            start = Offset(start.x, start.y),
            end = Offset(end.x, end.y),
            strokeWidth = drawingPath.strokeWidth,
            cap = StrokeCap.Round,
            alpha = drawingPath.opacity
        )
        ShapeTool.RECTANGLE -> drawRectangleShape(start, end, color, style, drawingPath.opacity)
        ShapeTool.CIRCLE -> drawCircleShape(start, end, color, style, drawingPath.opacity)
        ShapeTool.ARROW -> drawArrowShape(start, end, color, drawingPath.strokeWidth, drawingPath.opacity)
        ShapeTool.POLYGON -> drawPolygonShape(points, color, style, drawingPath.opacity)
        ShapeTool.NONE -> {} // Do nothing
    }
}

/**
 * Draw rectangle from start to end point
 */
private fun DrawScope.drawRectangleShape(
    start: com.hotmail.arehmananis.sketchapp.domain.model.PathPoint,
    end: com.hotmail.arehmananis.sketchapp.domain.model.PathPoint,
    color: Color,
    style: androidx.compose.ui.graphics.drawscope.DrawStyle,
    alpha: Float
) {
    val topLeft = Offset(
        minOf(start.x, end.x),
        minOf(start.y, end.y)
    )
    val size = Size(
        kotlin.math.abs(end.x - start.x),
        kotlin.math.abs(end.y - start.y)
    )

    drawRect(
        color = color,
        topLeft = topLeft,
        size = size,
        alpha = alpha,
        style = style
    )
}

/**
 * Draw circle/ellipse from start to end point
 */
private fun DrawScope.drawCircleShape(
    start: com.hotmail.arehmananis.sketchapp.domain.model.PathPoint,
    end: com.hotmail.arehmananis.sketchapp.domain.model.PathPoint,
    color: Color,
    style: androidx.compose.ui.graphics.drawscope.DrawStyle,
    alpha: Float
) {
    val center = Offset(
        (start.x + end.x) / 2,
        (start.y + end.y) / 2
    )
    val radiusX = kotlin.math.abs(end.x - start.x) / 2
    val radiusY = kotlin.math.abs(end.y - start.y) / 2

    drawOval(
        color = color,
        topLeft = Offset(center.x - radiusX, center.y - radiusY),
        size = Size(radiusX * 2, radiusY * 2),
        alpha = alpha,
        style = style
    )
}

/**
 * Draw arrow from start to end point
 */
private fun DrawScope.drawArrowShape(
    start: com.hotmail.arehmananis.sketchapp.domain.model.PathPoint,
    end: com.hotmail.arehmananis.sketchapp.domain.model.PathPoint,
    color: Color,
    strokeWidth: Float,
    alpha: Float
) {
    // Draw line
    drawLine(
        color = color,
        start = Offset(start.x, start.y),
        end = Offset(end.x, end.y),
        strokeWidth = strokeWidth,
        cap = StrokeCap.Round,
        alpha = alpha
    )

    // Calculate arrow head
    val angle = atan2((end.y - start.y).toDouble(), (end.x - start.x).toDouble())
    val arrowLength = strokeWidth * 3
    val arrowAngle = Math.PI / 6 // 30 degrees

    val arrowPoint1 = Offset(
        (end.x - arrowLength * cos(angle - arrowAngle)).toFloat(),
        (end.y - arrowLength * sin(angle - arrowAngle)).toFloat()
    )
    val arrowPoint2 = Offset(
        (end.x - arrowLength * cos(angle + arrowAngle)).toFloat(),
        (end.y - arrowLength * sin(angle + arrowAngle)).toFloat()
    )

    // Draw arrow head lines
    drawLine(
        color = color,
        start = Offset(end.x, end.y),
        end = arrowPoint1,
        strokeWidth = strokeWidth,
        cap = StrokeCap.Round,
        alpha = alpha
    )
    drawLine(
        color = color,
        start = Offset(end.x, end.y),
        end = arrowPoint2,
        strokeWidth = strokeWidth,
        cap = StrokeCap.Round,
        alpha = alpha
    )
}

/**
 * Draw polygon from multiple points
 */
private fun DrawScope.drawPolygonShape(
    points: List<com.hotmail.arehmananis.sketchapp.domain.model.PathPoint>,
    color: Color,
    style: androidx.compose.ui.graphics.drawscope.DrawStyle,
    alpha: Float
) {
    if (points.size < 3) return

    val path = Path()
    points.forEachIndexed { index, point ->
        if (index == 0) {
            path.moveTo(point.x, point.y)
        } else {
            path.lineTo(point.x, point.y)
        }
    }
    path.close()

    drawPath(
        path = path,
        color = color,
        alpha = alpha,
        style = style
    )
}
