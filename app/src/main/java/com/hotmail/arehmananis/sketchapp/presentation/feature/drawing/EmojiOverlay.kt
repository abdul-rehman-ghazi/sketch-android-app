package com.hotmail.arehmananis.sketchapp.presentation.feature.drawing

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
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
import androidx.compose.foundation.layout.offset
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.hotmail.arehmananis.sketchapp.domain.model.EmojiElement

@Composable
fun EmojiOverlay(
    emoji: EmojiElement,
    isSelected: Boolean,
    onTap: () -> Unit,
    onDrag: (Float, Float) -> Unit,
    onDelete: () -> Unit
) {
    val density = LocalDensity.current
    var dragOffset by remember { mutableStateOf(Offset.Zero) }

    val containerSize = emoji.size * 1.2f

    Box(
        modifier = Modifier
            .offset(
                x = with(density) { (emoji.x + dragOffset.x - containerSize / 2).toDp() },
                y = with(density) { (emoji.y + dragOffset.y - containerSize / 2).toDp() }
            )
            .size(with(density) { containerSize.toDp() })
            .then(
                if (isSelected) {
                    Modifier
                        .background(
                            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.2f),
                            shape = CircleShape
                        )
                        .border(
                            width = 2.dp,
                            color = MaterialTheme.colorScheme.primary,
                            shape = CircleShape
                        )
                } else Modifier
            )
            .pointerInput(Unit) {
                detectTapGestures(onTap = { onTap() })
            }
            .pointerInput(Unit) {
                detectDragGestures(
                    onDragStart = { onTap() },
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
            .rotate(emoji.rotation),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = emoji.emoji,
            fontSize = with(density) { (emoji.size * 0.6f).toSp() },
            fontWeight = FontWeight.Bold
        )

        if (isSelected) {
            IconButton(
                onClick = onDelete,
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .offset(x = 10.dp, y = (-10).dp)
                    .size(24.dp)
                    .background(
                        color = MaterialTheme.colorScheme.error,
                        shape = CircleShape
                    )
            ) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "Delete emoji",
                    tint = MaterialTheme.colorScheme.onError,
                    modifier = Modifier.size(14.dp)
                )
            }
        }
    }
}