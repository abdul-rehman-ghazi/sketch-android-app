package com.hotmail.arehmananis.sketchapp.domain.model

import kotlinx.serialization.Serializable

/**
 * Types of brushes available for drawing
 * Pure Kotlin - KMP Ready
 */
@Serializable
enum class BrushType {
    PEN,
    PENCIL,
    ERASER,
    MARKER,
    HIGHLIGHTER,
    AIRBRUSH,
    CALLIGRAPHY
}

/**
 * Configuration for each brush type
 */
data class BrushConfig(
    val type: BrushType,
    val minWidth: Float,
    val maxWidth: Float,
    val defaultWidth: Float,
    val supportsOpacity: Boolean,
    val defaultOpacity: Float
) {
    companion object {
        fun getConfig(type: BrushType): BrushConfig = when (type) {
            BrushType.PEN -> BrushConfig(
                type = type,
                minWidth = 1f,
                maxWidth = 20f,
                defaultWidth = 3f,
                supportsOpacity = true,
                defaultOpacity = 1f
            )

            BrushType.PENCIL -> BrushConfig(
                type = type,
                minWidth = 1f,
                maxWidth = 15f,
                defaultWidth = 2f,
                supportsOpacity = true,
                defaultOpacity = 0.7f
            )

            BrushType.ERASER -> BrushConfig(
                type = type,
                minWidth = 5f,
                maxWidth = 50f,
                defaultWidth = 20f,
                supportsOpacity = false,
                defaultOpacity = 1f
            )

            BrushType.MARKER -> BrushConfig(
                type = type,
                minWidth = 5f,
                maxWidth = 30f,
                defaultWidth = 10f,
                supportsOpacity = true,
                defaultOpacity = 0.6f
            )

            BrushType.HIGHLIGHTER -> BrushConfig(
                type = type,
                minWidth = 10f,
                maxWidth = 40f,
                defaultWidth = 20f,
                supportsOpacity = true,
                defaultOpacity = 0.3f
            )

            BrushType.AIRBRUSH -> BrushConfig(
                type = type,
                minWidth = 10f,
                maxWidth = 60f,
                defaultWidth = 25f,
                supportsOpacity = true,
                defaultOpacity = 0.2f
            )

            BrushType.CALLIGRAPHY -> BrushConfig(
                type = type,
                minWidth = 2f,
                maxWidth = 25f,
                defaultWidth = 8f,
                supportsOpacity = true,
                defaultOpacity = 1f
            )
        }
    }
}
