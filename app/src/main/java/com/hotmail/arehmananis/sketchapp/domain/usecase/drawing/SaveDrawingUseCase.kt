package com.hotmail.arehmananis.sketchapp.domain.usecase.drawing

import android.graphics.Bitmap
import com.hotmail.arehmananis.sketchapp.domain.repository.DrawingRepository

/**
 * Use case for saving a drawing bitmap to local storage or MediaStore
 * @param saveToMediaStore If true, saves to MediaStore (visible in Photos app)
 */
class SaveDrawingUseCase(
    private val drawingRepository: DrawingRepository
) {
    suspend operator fun invoke(bitmap: Bitmap, fileName: String, saveToMediaStore: Boolean = false): Result<String> {
        return drawingRepository.saveBitmapToLocal(bitmap, fileName, saveToMediaStore)
    }
}
