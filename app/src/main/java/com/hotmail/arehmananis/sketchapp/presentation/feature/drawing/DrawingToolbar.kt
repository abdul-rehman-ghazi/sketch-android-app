package com.hotmail.arehmananis.sketchapp.presentation.feature.drawing

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import com.hotmail.arehmananis.sketchapp.domain.model.BrushType
import com.hotmail.arehmananis.sketchapp.presentation.theme.DrawingColors
import com.hotmail.arehmananis.sketchapp.presentation.theme.VibrantIndigo
import com.hotmail.arehmananis.sketchapp.presentation.theme.VibrantPurple

/**
 * Modern toolbar for selecting brush, color, and stroke width
 * with expanded color palette and gradient accents
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
        modifier = modifier
            .fillMaxWidth(),
        shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 4.dp,
        shadowElevation = 8.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Brush selection
            Text(
                text = "Brush",
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.onSurface
            )
            BrushSelector(
                currentBrush = currentBrush,
                onBrushSelected = onBrushChange
            )

            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)

            // Color selection
            Text(
                text = "Color",
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.onSurface
            )
            ColorPalette(
                selectedColor = currentColor,
                onColorSelected = onColorChange
            )

            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)

            // Stroke width
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Stroke Size",
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "${strokeWidth.toInt()}px",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.primary
                )
            }
            Slider(
                value = strokeWidth,
                onValueChange = onStrokeWidthChange,
                valueRange = 1f..50f,
                modifier = Modifier.fillMaxWidth(),
                colors = SliderDefaults.colors(
                    thumbColor = MaterialTheme.colorScheme.primary,
                    activeTrackColor = MaterialTheme.colorScheme.primary,
                    inactiveTrackColor = MaterialTheme.colorScheme.surfaceVariant
                )
            )
        }
    }
}

/**
 * Brush selector with gradient background for selected brush
 */
@Composable
private fun BrushSelector(
    currentBrush: BrushType,
    onBrushSelected: (BrushType) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Spacer(modifier = Modifier.width(4.dp))

        BrushType.entries.forEach { brushType ->
            val iconData = brushType.toIconData()
            BrushButton(
                icon = iconData.icon,
                label = iconData.label,
                isSelected = currentBrush == brushType,
                onClick = { onBrushSelected(brushType) }
            )
        }

        Spacer(modifier = Modifier.width(4.dp))
    }
}

/**
 * Modern brush button with gradient background when selected
 */
@Composable
private fun BrushButton(
    icon: ImageVector,
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val scale by animateFloatAsState(
        targetValue = if (isSelected) 1.1f else 1f,
        label = "brush_scale"
    )

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .clickable(onClick = onClick)
            .padding(horizontal = 4.dp)
            .widthIn(min = 72.dp)
            .scale(scale)
    ) {
        Box(
            modifier = Modifier
                .size(48.dp)
                .then(
                    if (isSelected) {
                        Modifier.background(
                            brush = Brush.linearGradient(
                                colors = listOf(VibrantPurple, VibrantIndigo)
                            ),
                            shape = CircleShape
                        )
                    } else {
                        Modifier.background(
                            color = Color.Transparent,
                            shape = CircleShape
                        )
                    }
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                modifier = Modifier.size(28.dp),
                tint = if (isSelected) Color.White
                else MaterialTheme.colorScheme.onSurface
            )
        }
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = if (isSelected) MaterialTheme.colorScheme.primary
            else MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

/**
 * Compact color palette with all colors in a horizontally scrollable row
 */
@Composable
private fun ColorPalette(
    selectedColor: Color,
    onColorSelected: (Color) -> Unit
) {
    val allColors = remember {
        val vibrant = DrawingColors.vibrantColors
        val pastel = DrawingColors.pastelColors
        val neutral = DrawingColors.neutralColors
        vibrant + pastel + neutral
    }
    LazyRow(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        item {
            Spacer(modifier = Modifier.width(4.dp))
        }

        items(allColors) { color ->
            ColorCircle(
                color = color,
                isSelected = color == selectedColor,
                onClick = { onColorSelected(color) }
            )
        }

        item {
            Spacer(modifier = Modifier.width(4.dp))
        }
    }
}

/**
 * Compact color circle with modern selection styling
 */
@Composable
private fun ColorCircle(
    color: Color,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .size(48.dp)
            .clickable(onClick = onClick)
            .padding(4.dp),
        contentAlignment = Alignment.Center
    ) {
        // Color circle with border
        Box(
            modifier = Modifier
                .size(40.dp)
                .background(color = color, shape = CircleShape)
                .border(
                    width = if (isSelected) 3.dp else 1.dp,
                    color = if (isSelected) {
                        MaterialTheme.colorScheme.primary
                    } else {
                        MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
                    },
                    shape = CircleShape
                )
        )
    }
}
