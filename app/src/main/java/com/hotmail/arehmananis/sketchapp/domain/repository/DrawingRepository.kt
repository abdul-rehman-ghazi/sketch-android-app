package com.hotmail.arehmananis.sketchapp.domain.repository

import android.graphics.Bitmap

/**
 * Repository interface for drawing bitmap operations
 * Note: Uses Android Bitmap - will need expect/actual for KMP
 */
interface DrawingRepository {
    /**
     * Save a bitmap to local storage
     * @param bitmap The bitmap to save
     * @param fileName The file name (e.g., "sketch_123.png")
     * @return Result with file path on success
     */
    suspend fun saveBitmapToLocal(bitmap: Bitmap, fileName: String): Result<String>

    /**
     * Load a bitmap from local storage
     * @param path The file path to load from
     * @return Result with Bitmap on success
     */
    suspend fun loadBitmapFromLocal(path: String): Result<Bitmap>
}
