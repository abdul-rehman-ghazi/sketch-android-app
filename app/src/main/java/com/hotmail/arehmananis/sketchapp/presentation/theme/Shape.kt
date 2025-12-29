package com.hotmail.arehmananis.sketchapp.presentation.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Shapes
import androidx.compose.ui.unit.dp

/**
 * Modern shape system with generous rounded corners
 * for a contemporary, friendly aesthetic
 */
val AppShapes = Shapes(
    // Small components (chips, small cards)
    extraSmall = RoundedCornerShape(8.dp),
    small = RoundedCornerShape(12.dp),

    // Medium components (cards, dialogs)
    medium = RoundedCornerShape(16.dp),

    // Large components (bottom sheets, large cards)
    large = RoundedCornerShape(24.dp),
    extraLarge = RoundedCornerShape(32.dp)
)
