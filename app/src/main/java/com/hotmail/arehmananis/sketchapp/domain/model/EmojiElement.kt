package com.hotmail.arehmananis.sketchapp.domain.model

import kotlinx.serialization.Serializable

/**
 * Represents an emoji element placed on the canvas
 * Pure Kotlin - KMP Ready
 */
@Serializable
data class EmojiElement(
    val id: String,
    val emoji: String,
    val x: Float,
    val y: Float,
    val size: Float = 48f,
    val rotation: Float = 0f,
    val layer: Int = 0
)
