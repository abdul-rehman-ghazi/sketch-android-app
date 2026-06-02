package com.hotmail.arehmananis.sketchapp.data.local.serializer

import android.util.Log
import com.hotmail.arehmananis.sketchapp.domain.model.ImageElement
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

object ImageSerializer {
    private const val TAG = "ImageSerializer"

    private val json = Json {
        ignoreUnknownKeys = true
        prettyPrint = false
    }

    fun toJson(images: List<ImageElement>): String? {
        return try {
            json.encodeToString(images)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to serialize images to JSON", e)
            null
        }
    }

    fun fromJson(jsonString: String): List<ImageElement>? {
        return try {
            json.decodeFromString<List<ImageElement>>(jsonString)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to deserialize JSON to images", e)
            null
        }
    }
}
