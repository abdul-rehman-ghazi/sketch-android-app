package com.hotmail.arehmananis.sketchapp.presentation.feature.drawing

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.ui.graphics.vector.ImageVector
import com.hotmail.arehmananis.sketchapp.domain.model.BrushType

/**
 * Maps BrushType to Material Icons
 * Platform-specific presentation concern (icons are from Android Material library)
 */
data class BrushIconData(
    val icon: ImageVector,
    val label: String
)

/**
 * Extension function to get icon and label data for each brush type
 */
fun BrushType.toIconData(): BrushIconData = when (this) {
    BrushType.PEN -> BrushIconData(Icons.Default.Edit, "Pen")
    BrushType.PENCIL -> BrushIconData(Icons.Default.Create, "Pencil")
    BrushType.ERASER -> BrushIconData(Icons.Default.Delete, "Eraser")
    BrushType.MARKER -> BrushIconData(Icons.Default.Brush, "Marker")
    BrushType.HIGHLIGHTER -> BrushIconData(Icons.Default.Highlight, "Highlighter")
    BrushType.AIRBRUSH -> BrushIconData(Icons.Default.Brush, "Airbrush")
    BrushType.CALLIGRAPHY -> BrushIconData(Icons.Default.FormatColorText, "Calligraphy")
}
