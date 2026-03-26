package com.hotmail.arehmananis.sketchapp.data.local.serializer

import android.util.Log
import com.hotmail.arehmananis.sketchapp.domain.model.EmojiElement
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

/**
 * Utility for serializing/deserializing EmojiElement objects to/from JSON
 * Uses kotlinx.serialization (KMP-compatible)
 */
object EmojiSerializer {
    private const val TAG = "EmojiSerializer"

    private val json = Json {
        ignoreUnknownKeys = true
        prettyPrint = false
    }

    fun toJson(emojis: List<EmojiElement>): String? {
        return try {
            json.encodeToString(emojis)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to serialize emojis to JSON", e)
            null
        }
    }

    fun fromJson(jsonString: String): List<EmojiElement>? {
        return try {
            json.decodeFromString<List<EmojiElement>>(jsonString)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to deserialize JSON to emojis", e)
            null
        }
    }
}