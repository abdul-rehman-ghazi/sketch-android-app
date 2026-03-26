package com.hotmail.arehmananis.sketchapp.presentation.feature.drawing

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.core.content.FileProvider
import java.io.File

fun shareImage(context: Context, imagePath: String) {
    try {
        val imageUri = if (imagePath.startsWith("content://")) {
            Uri.parse(imagePath)
        } else {
            FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                File(imagePath)
            )
        }

        val shareIntent = Intent().apply {
            action = Intent.ACTION_SEND
            putExtra(Intent.EXTRA_STREAM, imageUri)
            type = "image/png"
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }

        context.startActivity(Intent.createChooser(shareIntent, "Share Sketch"))
    } catch (e: Exception) {
        e.printStackTrace()
    }
}