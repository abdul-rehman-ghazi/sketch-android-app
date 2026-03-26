package com.hotmail.arehmananis.sketchapp.presentation.feature.drawing

import android.graphics.Bitmap
import android.graphics.PorterDuff
import android.graphics.PorterDuffXfermode
import androidx.core.graphics.createBitmap
import com.hotmail.arehmananis.sketchapp.domain.model.BrushType
import com.hotmail.arehmananis.sketchapp.domain.model.DrawingPath
import com.hotmail.arehmananis.sketchapp.domain.model.EmojiElement
import com.hotmail.arehmananis.sketchapp.domain.model.ShapeTool
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin

fun createBitmapFromPaths(
    paths: List<DrawingPath>,
    emojiElements: List<EmojiElement> = emptyList(),
    originalWidth: Int,
    originalHeight: Int,
    targetWidth: Int,
    targetHeight: Int,
    transparentBackground: Boolean = false
): Bitmap {
    val scaleX = targetWidth.toFloat() / originalWidth.toFloat()
    val scaleY = targetHeight.toFloat() / originalHeight.toFloat()

    val bitmap = createBitmap(targetWidth, targetHeight, Bitmap.Config.ARGB_8888)
    val canvas = android.graphics.Canvas(bitmap)

    if (transparentBackground) {
        canvas.drawColor(android.graphics.Color.TRANSPARENT)
    } else {
        canvas.drawColor(android.graphics.Color.WHITE)
    }

    canvas.scale(scaleX, scaleY)

    val layerPaint = android.graphics.Paint()
    canvas.saveLayer(0f, 0f, originalWidth.toFloat(), originalHeight.toFloat(), layerPaint)

    paths.forEach { drawingPath ->
        if (drawingPath.shapeTool != ShapeTool.NONE && drawingPath.points.size >= 2) {
            drawShapeToBitmap(canvas, drawingPath)
        } else {
            val paint = android.graphics.Paint().apply {
                strokeWidth = drawingPath.strokeWidth
                style = android.graphics.Paint.Style.STROKE
                strokeCap = android.graphics.Paint.Cap.ROUND
                strokeJoin = android.graphics.Paint.Join.ROUND
                isAntiAlias = true

                if (drawingPath.brush == BrushType.ERASER) {
                    xfermode = PorterDuffXfermode(PorterDuff.Mode.CLEAR)
                } else {
                    color = drawingPath.color.toInt()
                    alpha = (drawingPath.opacity * 255).toInt()
                }
            }

            val path = android.graphics.Path()
            drawingPath.points.forEachIndexed { index, point ->
                if (index == 0) path.moveTo(point.x, point.y)
                else path.lineTo(point.x, point.y)
            }
            canvas.drawPath(path, paint)
        }
    }

    emojiElements.forEach { emoji ->
        val textPaint = android.graphics.Paint().apply {
            textSize = emoji.size * 0.6f
            isAntiAlias = true
            textAlign = android.graphics.Paint.Align.CENTER
        }
        canvas.save()
        if (emoji.rotation != 0f) canvas.rotate(emoji.rotation, emoji.x, emoji.y)
        canvas.drawText(emoji.emoji, emoji.x, emoji.y + emoji.size * 0.2f, textPaint)
        canvas.restore()
    }

    canvas.restore()
    return bitmap
}

private fun drawShapeToBitmap(
    canvas: android.graphics.Canvas,
    drawingPath: DrawingPath
) {
    val points = drawingPath.points
    if (points.size < 2) return

    val start = points.first()
    val end = points.last()

    val paint = android.graphics.Paint().apply {
        color = drawingPath.color.toInt()
        alpha = (drawingPath.opacity * 255).toInt()
        style = if (drawingPath.isFilled) android.graphics.Paint.Style.FILL
                else android.graphics.Paint.Style.STROKE
        strokeWidth = drawingPath.strokeWidth
        strokeCap = android.graphics.Paint.Cap.ROUND
        strokeJoin = android.graphics.Paint.Join.ROUND
        isAntiAlias = true
    }

    when (drawingPath.shapeTool) {
        ShapeTool.LINE -> {
            canvas.drawLine(start.x, start.y, end.x, end.y, paint)
        }
        ShapeTool.RECTANGLE -> {
            val rect = android.graphics.RectF(
                minOf(start.x, end.x), minOf(start.y, end.y),
                maxOf(start.x, end.x), maxOf(start.y, end.y)
            )
            canvas.drawRect(rect, paint)
        }
        ShapeTool.CIRCLE -> {
            val rect = android.graphics.RectF(
                minOf(start.x, end.x), minOf(start.y, end.y),
                maxOf(start.x, end.x), maxOf(start.y, end.y)
            )
            canvas.drawOval(rect, paint)
        }
        ShapeTool.ARROW -> {
            canvas.drawLine(start.x, start.y, end.x, end.y, paint)

            val angle = atan2((end.y - start.y).toDouble(), (end.x - start.x).toDouble())
            val arrowLength = drawingPath.strokeWidth * 3
            val arrowAngle = Math.PI / 6

            canvas.drawLine(
                end.x, end.y,
                (end.x - arrowLength * cos(angle - arrowAngle)).toFloat(),
                (end.y - arrowLength * sin(angle - arrowAngle)).toFloat(),
                paint
            )
            canvas.drawLine(
                end.x, end.y,
                (end.x - arrowLength * cos(angle + arrowAngle)).toFloat(),
                (end.y - arrowLength * sin(angle + arrowAngle)).toFloat(),
                paint
            )
        }
        ShapeTool.POLYGON -> {
            if (points.size < 3) return
            val path = android.graphics.Path()
            points.forEachIndexed { index, point ->
                if (index == 0) path.moveTo(point.x, point.y)
                else path.lineTo(point.x, point.y)
            }
            path.close()
            canvas.drawPath(path, paint)
        }
        ShapeTool.NONE -> {}
    }
}