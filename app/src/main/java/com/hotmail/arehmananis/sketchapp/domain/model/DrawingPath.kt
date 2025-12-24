package com.hotmail.arehmananis.sketchapp.domain.model

/**
 * Represents a drawing path with its properties
 * Pure Kotlin - KMP Ready
 */
data class DrawingPath(
    val points: List<PathPoint>,
    val brush: BrushType,
    val color: Long, // ARGB as Long for KMP compatibility
    val strokeWidth: Float,
    val opacity: Float = 1f
)

/**
 * Represents a single point in a drawing path
 */
data class PathPoint(
    val x: Float,
    val y: Float,
    val pressure: Float = 1f,
    val timestamp: Long = System.currentTimeMillis()
)
