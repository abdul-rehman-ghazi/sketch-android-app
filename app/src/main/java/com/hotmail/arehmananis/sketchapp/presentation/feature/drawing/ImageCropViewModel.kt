package com.hotmail.arehmananis.sketchapp.presentation.feature.drawing

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.io.File
import java.util.UUID

enum class CropHandle {
    TOP_LEFT, TOP_CENTER, TOP_RIGHT,
    MIDDLE_LEFT, MIDDLE_RIGHT,
    BOTTOM_LEFT, BOTTOM_CENTER, BOTTOM_RIGHT
}

data class CropRect(
    val left: Float = 0.1f,
    val top: Float = 0.1f,
    val right: Float = 0.9f,
    val bottom: Float = 0.9f
)

data class CropResult(
    val filePath: String,
    val width: Int,
    val height: Int
)

class ImageCropViewModel : ViewModel() {

    private val _cropRect = MutableStateFlow(CropRect())
    val cropRect: StateFlow<CropRect> = _cropRect.asStateFlow()

    private val _isProcessing = MutableStateFlow(false)
    val isProcessing: StateFlow<Boolean> = _isProcessing.asStateFlow()

    private val _cropResult = MutableSharedFlow<CropResult>(extraBufferCapacity = 1)
    val cropResult: SharedFlow<CropResult> = _cropResult.asSharedFlow()

    private val _error = MutableSharedFlow<String>(extraBufferCapacity = 1)
    val error: SharedFlow<String> = _error.asSharedFlow()

    private var imageWidth: Int = 0
    private var imageHeight: Int = 0

    fun setImageDimensions(width: Int, height: Int) {
        imageWidth = width
        imageHeight = height
    }

    fun onHandleDrag(handle: CropHandle, fracDx: Float, fracDy: Float) {
        val r = _cropRect.value
        val minW = if (imageWidth > 0) 50f / imageWidth else 0.05f
        val minH = if (imageHeight > 0) 50f / imageHeight else 0.05f

        _cropRect.value = when (handle) {
            CropHandle.TOP_LEFT -> r.copy(
                left = (r.left + fracDx).coerceIn(0f, r.right - minW),
                top = (r.top + fracDy).coerceIn(0f, r.bottom - minH)
            )
            CropHandle.TOP_CENTER -> r.copy(
                top = (r.top + fracDy).coerceIn(0f, r.bottom - minH)
            )
            CropHandle.TOP_RIGHT -> r.copy(
                right = (r.right + fracDx).coerceIn(r.left + minW, 1f),
                top = (r.top + fracDy).coerceIn(0f, r.bottom - minH)
            )
            CropHandle.MIDDLE_LEFT -> r.copy(
                left = (r.left + fracDx).coerceIn(0f, r.right - minW)
            )
            CropHandle.MIDDLE_RIGHT -> r.copy(
                right = (r.right + fracDx).coerceIn(r.left + minW, 1f)
            )
            CropHandle.BOTTOM_LEFT -> r.copy(
                left = (r.left + fracDx).coerceIn(0f, r.right - minW),
                bottom = (r.bottom + fracDy).coerceIn(r.top + minH, 1f)
            )
            CropHandle.BOTTOM_CENTER -> r.copy(
                bottom = (r.bottom + fracDy).coerceIn(r.top + minH, 1f)
            )
            CropHandle.BOTTOM_RIGHT -> r.copy(
                right = (r.right + fracDx).coerceIn(r.left + minW, 1f),
                bottom = (r.bottom + fracDy).coerceIn(r.top + minH, 1f)
            )
        }
    }

    fun confirmCrop(uri: Uri, context: Context) {
        viewModelScope.launch(Dispatchers.IO) {
            _isProcessing.value = true
            try {
                val source = context.contentResolver.openInputStream(uri)
                    ?.use { BitmapFactory.decodeStream(it) }
                    ?: throw Exception("Could not read image")

                val rect = _cropRect.value
                val x = (rect.left * source.width).toInt().coerceIn(0, source.width - 1)
                val y = (rect.top * source.height).toInt().coerceIn(0, source.height - 1)
                val w = ((rect.right - rect.left) * source.width).toInt()
                    .coerceAtLeast(50).coerceAtMost(source.width - x)
                val h = ((rect.bottom - rect.top) * source.height).toInt()
                    .coerceAtLeast(50).coerceAtMost(source.height - y)

                val cropped = Bitmap.createBitmap(source, x, y, w, h)
                source.recycle()

                val imagesDir = File(context.filesDir, "images").also { it.mkdirs() }
                val outFile = File(imagesDir, "${UUID.randomUUID()}.jpg")
                outFile.outputStream().use { cropped.compress(Bitmap.CompressFormat.JPEG, 95, it) }
                cropped.recycle()

                _cropResult.emit(CropResult(outFile.absolutePath, w, h))
            } catch (e: Exception) {
                _error.emit("Failed to crop image: ${e.message}")
            } finally {
                _isProcessing.value = false
            }
        }
    }
}
