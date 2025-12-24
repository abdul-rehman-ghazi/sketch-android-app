package com.hotmail.arehmananis.sketchapp.data.repository

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import com.hotmail.arehmananis.sketchapp.domain.repository.DrawingRepository
import java.io.File
import java.io.FileOutputStream

/**
 * Implementation of DrawingRepository for saving/loading bitmaps
 */
class DrawingRepositoryImpl(
    private val context: Context
) : DrawingRepository {

    override suspend fun saveBitmapToLocal(bitmap: Bitmap, fileName: String): Result<String> {
        return try {
            val file = File(context.filesDir, fileName)
            FileOutputStream(file).use { out ->
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, out)
            }
            Result.success(file.absolutePath)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun loadBitmapFromLocal(path: String): Result<Bitmap> {
        return try {
            val bitmap = BitmapFactory.decodeFile(path)
                ?: return Result.failure(Exception("Failed to decode bitmap at path: $path"))
            Result.success(bitmap)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
