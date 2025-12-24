package com.hotmail.arehmananis.sketchapp.presentation.feature.drawing

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import com.hotmail.arehmananis.sketchapp.domain.model.BrushType

/**
 * Toolbar for selecting brush, color, and stroke width
 */
@Composable
fun DrawingToolbar(
    currentBrush: BrushType,
    currentColor: Color,
    strokeWidth: Float,
    onBrushChange: (BrushType) -> Unit,
    onColorChange: (Color) -> Unit,
    onStrokeWidthChange: (Float) -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 3.dp
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Brush selection
            Text(
                text = "Brush",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            BrushSelector(
                currentBrush = currentBrush,
                onBrushSelected = onBrushChange
            )

            Divider()

            // Color selection
            Text(
                text = "Color",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            ColorPalette(
                selectedColor = currentColor,
                onColorSelected = onColorChange
            )

            Divider()

            // Stroke width
            Text(
                text = "Size: ${strokeWidth.toInt()}",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Slider(
                value = strokeWidth,
                onValueChange = onStrokeWidthChange,
                valueRange = 1f..50f,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

@Composable
private fun BrushSelector(
    currentBrush: BrushType,
    onBrushSelected: (BrushType) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceEvenly
    ) {
        BrushButton(
            icon = Icons.Default.Edit,
            label = "Pen",
            isSelected = currentBrush == BrushType.PEN,
            onClick = { onBrushSelected(BrushType.PEN) }
        )
        BrushButton(
            icon = Icons.Default.Create,
            label = "Pencil",
            isSelected = currentBrush == BrushType.PENCIL,
            onClick = { onBrushSelected(BrushType.PENCIL) }
        )
        BrushButton(
            icon = Icons.Default.Delete,
            label = "Eraser",
            isSelected = currentBrush == BrushType.ERASER,
            onClick = { onBrushSelected(BrushType.ERASER) }
        )
        BrushButton(
            icon = Icons.Default.Brush,
            label = "Marker",
            isSelected = currentBrush == BrushType.MARKER,
            onClick = { onBrushSelected(BrushType.MARKER) }
        )
    }
}

@Composable
private fun BrushButton(
    icon: ImageVector,
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.clickable(onClick = onClick)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = label,
            modifier = Modifier
                .size(36.dp)
                .background(
                    color = if (isSelected) MaterialTheme.colorScheme.primaryContainer
                    else Color.Transparent,
                    shape = CircleShape
                )
                .padding(6.dp),
            tint = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer
            else MaterialTheme.colorScheme.onSurface
        )
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = if (isSelected) MaterialTheme.colorScheme.primary
            else MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun ColorPalette(
    selectedColor: Color,
    onColorSelected: (Color) -> Unit
) {
    val colors = listOf(
        Color.Black,
        Color.Red,
        Color.Blue,
        Color.Green,
        Color.Yellow,
        Color.Magenta,
        Color.Cyan,
        Color.Gray,
        Color.White
    )

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceEvenly
    ) {
        colors.forEach { color ->
            ColorCircle(
                color = color,
                isSelected = color == selectedColor,
                onClick = { onColorSelected(color) }
            )
        }
    }
}

@Composable
private fun ColorCircle(
    color: Color,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .size(40.dp)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .size(32.dp)
                .background(color = color, shape = CircleShape)
                .border(
                    width = if (isSelected) 3.dp else 1.dp,
                    color = if (isSelected) MaterialTheme.colorScheme.primary
                    else Color.Gray,
                    shape = CircleShape
                )
        )
    }
}
