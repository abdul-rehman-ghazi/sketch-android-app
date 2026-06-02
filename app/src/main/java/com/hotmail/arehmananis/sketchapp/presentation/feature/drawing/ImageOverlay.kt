package com.hotmail.arehmananis.sketchapp.presentation.feature.drawing

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import com.hotmail.arehmananis.sketchapp.domain.model.ImageElement

@Composable
fun ImageOverlay(
    image: ImageElement,
    isSelected: Boolean,
    onDrag: (Float, Float) -> Unit,
    onDelete: () -> Unit
) {
    val density = LocalDensity.current
    var dragOffset by remember { mutableStateOf(Offset.Zero) }

    if (!isSelected) return

    Box(
        modifier = Modifier
            .offset(
                x = with(density) { (image.x + dragOffset.x - image.width / 2).toDp() },
                y = with(density) { (image.y + dragOffset.y - image.height / 2).toDp() }
            )
            .size(
                width = with(density) { image.width.toDp() },
                height = with(density) { image.height.toDp() }
            )
            .rotate(image.rotation)
            .border(
                width = 2.dp,
                color = MaterialTheme.colorScheme.primary,
                shape = RoundedCornerShape(4.dp)
            )
            .pointerInput(image.id) {
                detectDragGestures(
                    onDrag = { change, dragAmount ->
                        change.consume()
                        dragOffset += Offset(dragAmount.x, dragAmount.y)
                    },
                    onDragEnd = {
                        onDrag(dragOffset.x, dragOffset.y)
                        dragOffset = Offset.Zero
                    }
                )
            }
    ) {
        IconButton(
            onClick = onDelete,
            modifier = Modifier
                .align(Alignment.TopEnd)
                .offset(x = 10.dp, y = (-10).dp)
                .size(24.dp)
                .background(color = MaterialTheme.colorScheme.error, shape = CircleShape)
        ) {
            Icon(
                imageVector = Icons.Default.Close,
                contentDescription = "Delete image",
                tint = MaterialTheme.colorScheme.onError,
                modifier = Modifier.size(14.dp)
            )
        }
    }
}
