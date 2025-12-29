package com.hotmail.arehmananis.sketchapp.presentation.common.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.hotmail.arehmananis.sketchapp.presentation.theme.VibrantIndigo
import com.hotmail.arehmananis.sketchapp.presentation.theme.VibrantPurple

/**
 * Modern gradient button with smooth press animation
 *
 * @param text Button text
 * @param onClick Click handler
 * @param modifier Modifier for the button
 * @param enabled Whether the button is enabled
 * @param gradient Custom gradient brush (defaults to purple-indigo gradient)
 */
@Composable
fun GradientButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    gradient: Brush = Brush.horizontalGradient(
        colors = listOf(VibrantPurple, VibrantIndigo)
    )
) {
    var isPressed by remember { mutableStateOf(false) }
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.95f else 1f,
        label = "button_scale"
    )

    Box(
        modifier = modifier
            .scale(scale)
            .defaultMinSize(minHeight = 48.dp)
            .background(
                brush = if (enabled) gradient else Brush.horizontalGradient(
                    colors = listOf(Color.Gray, Color.Gray)
                ),
                shape = RoundedCornerShape(16.dp)
            ),
        contentAlignment = Alignment.Center
    ) {
        Button(
            onClick = {
                isPressed = true
                onClick()
                isPressed = false
            },
            enabled = enabled,
            colors = ButtonDefaults.buttonColors(
                containerColor = Color.Transparent,
                contentColor = Color.White,
                disabledContainerColor = Color.Transparent,
                disabledContentColor = Color.White.copy(alpha = 0.6f)
            ),
            contentPadding = PaddingValues(horizontal = 24.dp, vertical = 16.dp),
            elevation = ButtonDefaults.buttonElevation(
                defaultElevation = 0.dp,
                pressedElevation = 0.dp,
                disabledElevation = 0.dp
            ),
            modifier = Modifier.matchParentSize()
        ) {
            Text(
                text = text,
                style = MaterialTheme.typography.labelLarge
            )
        }
    }
}

/**
 * Outlined gradient button with gradient border
 *
 * @param text Button text
 * @param onClick Click handler
 * @param modifier Modifier for the button
 * @param enabled Whether the button is enabled
 * @param gradient Custom gradient brush for border
 */
@Composable
fun GradientOutlinedButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    gradient: Brush = Brush.horizontalGradient(
        colors = listOf(VibrantPurple, VibrantIndigo)
    )
) {
    var isPressed by remember { mutableStateOf(false) }
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.95f else 1f,
        label = "button_scale"
    )

    Button(
        onClick = {
            isPressed = true
            onClick()
            isPressed = false
        },
        enabled = enabled,
        colors = ButtonDefaults.outlinedButtonColors(
            containerColor = Color.Transparent,
            contentColor = MaterialTheme.colorScheme.primary
        ),
        contentPadding = PaddingValues(horizontal = 24.dp, vertical = 16.dp),
        modifier = modifier
            .scale(scale)
            .defaultMinSize(minHeight = 48.dp)
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelLarge
        )
    }
}
