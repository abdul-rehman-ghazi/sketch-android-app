package com.hotmail.arehmananis.sketchapp.domain.model

import kotlinx.serialization.Serializable

/**
 * Represents an imported image element placed on the canvas
 * Pure Kotlin - KMP Ready (image data will be platform-specific)
 */
@Serializable
data class ImageElement(
    val id: String,
    val imagePath: String, // Local file path to the imported image
    val x: Float,
    val y: Float,
    val width: Float,
    val height: Float,
    val rotation: Float = 0f,
    val scaleX: Float = 1f,
    val scaleY: Float = 1f,
    val alpha: Float = 1f,
    val layer: Int = 0
)
