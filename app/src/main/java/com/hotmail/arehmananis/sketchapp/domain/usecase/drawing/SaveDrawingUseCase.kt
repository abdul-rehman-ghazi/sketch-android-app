package com.hotmail.arehmananis.sketchapp.domain.usecase.drawing

import android.graphics.Bitmap
import com.hotmail.arehmananis.sketchapp.domain.repository.DrawingRepository

/**
 * Use case for saving a drawing bitmap to local storage
 */
class SaveDrawingUseCase(
    private val drawingRepository: DrawingRepository
) {
    suspend operator fun invoke(bitmap: Bitmap, fileName: String): Result<String> {
        return drawingRepository.saveBitmapToLocal(bitmap, fileName)
    }
}
