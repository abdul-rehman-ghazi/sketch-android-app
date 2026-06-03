package com.hotmail.arehmananis.sketchapp.presentation.feature.drawing

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.Redo
import androidx.compose.material.icons.automirrored.filled.Undo
import androidx.compose.material.icons.filled.AddPhotoAlternate
import androidx.compose.material.icons.filled.Circle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DeleteForever
import androidx.compose.material.icons.filled.EmojiEmotions
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Pentagon
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Square
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import com.hotmail.arehmananis.sketchapp.domain.model.BrushType
import com.hotmail.arehmananis.sketchapp.domain.model.ShapeTool
import com.hotmail.arehmananis.sketchapp.presentation.theme.DrawingColors
import com.hotmail.arehmananis.sketchapp.presentation.theme.VibrantIndigo
import com.hotmail.arehmananis.sketchapp.presentation.theme.VibrantPurple

@Composable
fun DrawingPanel(
    currentBrush: BrushType,
    currentColor: Color,
    strokeWidth: Float,
    canUndo: Boolean,
    canRedo: Boolean,
    isSaving: Boolean,
    hasContent: Boolean,
    onBack: () -> Unit,
    onUndo: () -> Unit,
    onRedo: () -> Unit,
    onImportImage: () -> Unit,
    onShare: () -> Unit,
    onSave: () -> Unit,
    onClear: () -> Unit,
    onToggleEmoji: () -> Unit,
    currentShapeTool: ShapeTool = ShapeTool.NONE,
    isFilled: Boolean = false,
    onBrushChange: (BrushType) -> Unit,
    onColorChange: (Color) -> Unit,
    onStrokeWidthChange: (Float) -> Unit,
    onShapeToolChange: (ShapeTool) -> Unit = {},
    onFilledChange: (Boolean) -> Unit = {},
    modifier: Modifier = Modifier
) {
    var isExpanded by remember { mutableStateOf(false) }
    val arrowRotation by animateFloatAsState(
        targetValue = if (isExpanded) 0f else 180f,
        label = "arrow_rotation"
    )

    val panelShape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp)
    val panelColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.85f)
    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(panelShape)
            .border(
                width = 1.dp,
                color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 1f),
                shape = panelShape
            )
            .background(color = panelColor)
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            // Handle / toggle row
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { isExpanded = !isExpanded }
                    .padding(vertical = 8.dp),
                contentAlignment = Alignment.Center
            ) {
                // Drag handle pill
                Box(
                    modifier = Modifier
                        .size(width = 40.dp, height = 4.dp)
                        .background(
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.3f),
                            shape = CircleShape
                        )
                )
                // Arrow indicator
                Icon(
                    imageVector = Icons.Default.KeyboardArrowUp,
                    contentDescription = if (isExpanded) "Collapse toolbar" else "Expand toolbar",
                    modifier = Modifier
                        .align(Alignment.CenterEnd)
                        .padding(end = 16.dp)
                        .size(20.dp)
                        .rotate(arrowRotation),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            // Action strip — always visible
            ActionStrip(
                canUndo = canUndo,
                canRedo = canRedo,
                isSaving = isSaving,
                hasContent = hasContent,
                onBack = onBack,
                onUndo = onUndo,
                onRedo = onRedo,
                onImportImage = onImportImage,
                onShare = onShare,
                onSave = onSave,
                onClear = onClear,
                onToggleEmoji = onToggleEmoji
            )

            AnimatedVisibility(
                visible = isExpanded,
                enter = expandVertically(),
                exit = shrinkVertically()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .verticalScroll(rememberScrollState())
                        .padding(start = 16.dp, end = 16.dp, bottom = 16.dp),
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

            // Shape tool selection
            Text(
                text = "Shapes",
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.onSurface
            )
            ShapeToolSelector(
                currentShapeTool = currentShapeTool,
                onShapeToolSelected = onShapeToolChange
            )

            // Fill option (only show when a shape is selected)
            if (currentShapeTool != ShapeTool.NONE && currentShapeTool != ShapeTool.LINE && currentShapeTool != ShapeTool.ARROW) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Fill Shape",
                        style = MaterialTheme.typography.titleSmall,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Switch(
                        checked = isFilled,
                        onCheckedChange = onFilledChange,
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = MaterialTheme.colorScheme.primary,
                            checkedTrackColor = MaterialTheme.colorScheme.primaryContainer
                        )
                    )
                }
            }

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
            var isDragging by remember { mutableStateOf(false) }
            BoxWithConstraints(modifier = Modifier.fillMaxWidth()) {
                val density = LocalDensity.current
                val fraction = (strokeWidth - 1f) / (50f - 1f)
                val thumbOffsetXDp = with(density) {
                    val thumbPaddingPx = 10.dp.toPx()
                    val trackWidthPx = maxWidth.toPx() - 2 * thumbPaddingPx
                    val thumbOffsetXPx = thumbPaddingPx + fraction * trackWidthPx
                    (thumbOffsetXPx - 32.dp.toPx()).toDp()
                }

                Slider(
                    value = strokeWidth,
                    onValueChange = {
                        isDragging = true
                        onStrokeWidthChange(it)
                    },
                    onValueChangeFinished = { isDragging = false },
                    valueRange = 1f..50f,
                    modifier = Modifier.fillMaxWidth(),
                    colors = SliderDefaults.colors(
                        thumbColor = MaterialTheme.colorScheme.primary,
                        activeTrackColor = MaterialTheme.colorScheme.primary,
                        inactiveTrackColor = MaterialTheme.colorScheme.surfaceVariant
                    )
                )

                val bubbleAlpha by animateFloatAsState(
                    targetValue = if (isDragging) 1f else 0f,
                    animationSpec = tween(150),
                    label = "bubble_alpha"
                )
                Box(
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .offset(x = thumbOffsetXDp, y = (-72).dp)
                        .alpha(bubbleAlpha)
                ) {
                    StrokePreviewBubble(strokeWidth = strokeWidth, color = currentColor)
                }
            }
                } // Column (animated content)
            } // AnimatedVisibility
        } // outer Column
    } // Surface
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
                isSelected = color.toArgb() == selectedColor.toArgb(),
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
    val scale by animateFloatAsState(
        targetValue = if (isSelected) 1.15f else 1f,
        label = "color_scale"
    )

    Box(
        modifier = Modifier
            .size(48.dp)
            .clickable(onClick = onClick)
            .padding(4.dp)
            .scale(scale),
        contentAlignment = Alignment.Center
    ) {
        // Outer glow ring for selected color
        if (isSelected) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .background(
                        brush = Brush.radialGradient(
                            colors = listOf(
                                MaterialTheme.colorScheme.primary.copy(alpha = 0.4f),
                                Color.Transparent
                            )
                        ),
                        shape = CircleShape
                    )
            )
        }

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

/**
 * Shape tool selector with gradient background for selected shape
 */
@Composable
private fun ShapeToolSelector(
    currentShapeTool: ShapeTool,
    onShapeToolSelected: (ShapeTool) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Spacer(modifier = Modifier.width(4.dp))

        ShapeTool.entries.forEach { shapeTool ->
            val iconData = shapeTool.toIconData()
            BrushButton(
                icon = iconData.icon,
                label = iconData.label,
                isSelected = currentShapeTool == shapeTool,
                onClick = { onShapeToolSelected(shapeTool) }
            )
        }

        Spacer(modifier = Modifier.width(4.dp))
    }
}

/**
 * Extension function to map ShapeTool to icon and label
 */
private fun ShapeTool.toIconData(): IconData {
    return when (this) {
        ShapeTool.NONE -> IconData(Icons.Default.Close, "None")
        ShapeTool.LINE -> IconData(Icons.Default.Remove, "Line")
        ShapeTool.RECTANGLE -> IconData(Icons.Default.Square, "Rectangle")
        ShapeTool.CIRCLE -> IconData(Icons.Default.Circle, "Circle")
        ShapeTool.ARROW -> IconData(Icons.AutoMirrored.Filled.ArrowForward, "Arrow")
        ShapeTool.POLYGON -> IconData(Icons.Default.Pentagon, "Polygon")
    }
}

/**
 * Data class to hold icon and label
 */
private data class IconData(
    val icon: ImageVector,
    val label: String
)

@Composable
private fun ActionStrip(
    canUndo: Boolean,
    canRedo: Boolean,
    isSaving: Boolean,
    hasContent: Boolean,
    onBack: () -> Unit,
    onUndo: () -> Unit,
    onRedo: () -> Unit,
    onImportImage: () -> Unit,
    onShare: () -> Unit,
    onSave: () -> Unit,
    onClear: () -> Unit,
    onToggleEmoji: () -> Unit
) {
    val actionEnabled = !isSaving && hasContent
    val disabledTint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f)

    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(onClick = onBack) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = "Back",
                tint = MaterialTheme.colorScheme.onSurface
            )
        }
        IconButton(onClick = onUndo, enabled = canUndo) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.Undo,
                contentDescription = "Undo",
                tint = if (canUndo) MaterialTheme.colorScheme.onSurface else disabledTint
            )
        }
        IconButton(onClick = onRedo, enabled = canRedo) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.Redo,
                contentDescription = "Redo",
                tint = if (canRedo) MaterialTheme.colorScheme.onSurface else disabledTint
            )
        }
        Spacer(modifier = Modifier.weight(1f))
        IconButton(onClick = onToggleEmoji) {
            Icon(
                imageVector = Icons.Default.EmojiEmotions,
                contentDescription = "Add Emoji",
                tint = MaterialTheme.colorScheme.onSurface
            )
        }
        IconButton(onClick = onImportImage, enabled = !isSaving) {
            Icon(
                imageVector = Icons.Default.AddPhotoAlternate,
                contentDescription = "Import Image",
                tint = if (!isSaving) MaterialTheme.colorScheme.onSurface else disabledTint
            )
        }
        IconButton(onClick = onShare, enabled = actionEnabled) {
            Icon(
                imageVector = Icons.Default.Share,
                contentDescription = "Share",
                tint = if (actionEnabled) MaterialTheme.colorScheme.onSurface else disabledTint
            )
        }
        IconButton(onClick = onSave, enabled = actionEnabled) {
            Icon(
                imageVector = Icons.Default.Save,
                contentDescription = "Save",
                tint = if (actionEnabled) MaterialTheme.colorScheme.primary else disabledTint
            )
        }
        IconButton(onClick = onClear) {
            Icon(
                imageVector = Icons.Default.DeleteForever,
                contentDescription = "Clear",
                tint = MaterialTheme.colorScheme.error
            )
        }
    }
}

@Composable
private fun StrokePreviewBubble(strokeWidth: Float, color: Color) {
    Surface(
        shape = RoundedCornerShape(12.dp),
        tonalElevation = 6.dp,
        shadowElevation = 4.dp,
        modifier = Modifier.width(64.dp)
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(vertical = 8.dp, horizontal = 8.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            val dotSize = strokeWidth.dp.coerceIn(4.dp, 36.dp)
            Box(
                modifier = Modifier
                    .size(dotSize)
                    .background(color = color, shape = CircleShape)
            )
            Text(
                text = "${strokeWidth.toInt()}px",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.primary
            )
        }
    }
}
