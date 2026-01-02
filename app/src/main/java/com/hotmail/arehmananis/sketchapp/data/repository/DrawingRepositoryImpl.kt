package com.hotmail.arehmananis.sketchapp.data.repository

import android.content.ContentValues
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import com.hotmail.arehmananis.sketchapp.domain.repository.DrawingRepository
import java.io.File
import java.io.FileOutputStream


/**
 * Implementation of DrawingRepository for saving/loading bitmaps
 */
class DrawingRepositoryImpl(
    private val context: Context
) : DrawingRepository {

    override suspend fun saveBitmapToLocal(bitmap: Bitmap, fileName: String, saveToMediaStore: Boolean): Result<String> {
        return try {
            if (saveToMediaStore) {
                // Save to MediaStore (Photos app) for export/sharing
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    // Android 10+ (API 29+) - use MediaStore with scoped storage
                    val contentValues = ContentValues().apply {
                        put(MediaStore.Images.Media.DISPLAY_NAME, fileName)
                        put(MediaStore.Images.Media.MIME_TYPE, "image/png")
                        put(MediaStore.Images.Media.RELATIVE_PATH, Environment.DIRECTORY_PICTURES + "/SketchApp")
                    }

                    val uri = context.contentResolver.insert(
                        MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                        contentValues
                    ) ?: throw Exception("Failed to create MediaStore entry")

                    context.contentResolver.openOutputStream(uri)?.use { out ->
                        bitmap.compress(Bitmap.CompressFormat.PNG, 100, out)
                    } ?: throw Exception("Failed to open output stream")

                    Result.success(uri.toString())
                } else {
                    // Android 9 (API 28) - use legacy external storage
                    val picturesDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES)
                    val appDir = File(picturesDir, "SketchApp")
                    if (!appDir.exists()) {
                        appDir.mkdirs()
                    }

                    val file = File(appDir, fileName)
                    FileOutputStream(file).use { out ->
                        bitmap.compress(Bitmap.CompressFormat.PNG, 100, out)
                    }

                    // Notify MediaStore about the new file
                    val contentValues = ContentValues().apply {
                        put(MediaStore.Images.Media.DATA, file.absolutePath)
                        put(MediaStore.Images.Media.MIME_TYPE, "image/png")
                    }
                    context.contentResolver.insert(
                        MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                        contentValues
                    )

                    Result.success(file.absolutePath)
                }
            } else {
                // Save to app-private storage for internal use (database saves)
                val dir: File? = context.getExternalFilesDir(Environment.DIRECTORY_PICTURES)
                val file = File(dir, fileName)
                FileOutputStream(file).use { out ->
                    bitmap.compress(Bitmap.CompressFormat.PNG, 100, out)
                }
                Result.success(file.absolutePath)
            }
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
