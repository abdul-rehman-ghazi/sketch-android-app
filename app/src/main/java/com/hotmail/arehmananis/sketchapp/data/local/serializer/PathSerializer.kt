package com.hotmail.arehmananis.sketchapp.data.local.serializer

import android.util.Log
import com.hotmail.arehmananis.sketchapp.domain.model.DrawingPath
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

/**
 * Utility for serializing/deserializing DrawingPath objects to/from JSON
 * Uses kotlinx.serialization (KMP-compatible)
 */
object PathSerializer {
    private const val TAG = "PathSerializer"

    private val json = Json {
        ignoreUnknownKeys = true
        prettyPrint = false
    }

    /**
     * Serialize list of DrawingPath to JSON string
     * @param paths List of drawing paths to serialize
     * @return JSON string representation, or null if serialization fails
     */
    fun toJson(paths: List<DrawingPath>): String? {
        return try {
            json.encodeToString(paths)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to serialize paths to JSON", e)
            null
        }
    }

    /**
     * Deserialize JSON string to list of DrawingPath
     * @param json JSON string to deserialize
     * @return List of DrawingPath objects, or null if deserialization fails
     */
    fun fromJson(jsonString: String): List<DrawingPath>? {
        return try {
            json.decodeFromString<List<DrawingPath>>(jsonString)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to deserialize JSON to paths", e)
            null
        }
    }
}
