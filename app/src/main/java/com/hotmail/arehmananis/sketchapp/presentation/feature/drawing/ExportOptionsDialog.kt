package com.hotmail.arehmananis.sketchapp.presentation.feature.drawing

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.hotmail.arehmananis.sketchapp.presentation.common.components.GradientButton

/**
 * Export options configuration
 */
data class ExportOptions(
    val transparentBackground: Boolean = false,
    val customWidth: Int? = null,
    val customHeight: Int? = null,
    val quality: Int = 100 // 1-100
)

/**
 * Dialog for configuring export options
 */
@Composable
fun ExportOptionsDialog(
    originalWidth: Int,
    originalHeight: Int,
    onExport: (ExportOptions) -> Unit,
    onDismiss: () -> Unit
) {
    var transparentBackground by remember { mutableStateOf(true) }
    var useCustomDimensions by remember { mutableStateOf(false) }
    var scaleFactor by remember { mutableStateOf(1f) }
    var quality by remember { mutableStateOf(100) }

    val customWidth = if (useCustomDimensions) {
        (originalWidth * scaleFactor).toInt()
    } else null

    val customHeight = if (useCustomDimensions) {
        (originalHeight * scaleFactor).toInt()
    } else null

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(24.dp),
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Title
                Text(
                    text = "Export Options",
                    style = MaterialTheme.typography.headlineSmall,
                    color = MaterialTheme.colorScheme.onSurface
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Transparent background toggle
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Transparent Background",
                            style = MaterialTheme.typography.bodyLarge
                        )
                        Text(
                            text = "Remove white background",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Switch(
                        checked = transparentBackground,
                        onCheckedChange = { transparentBackground = it },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = MaterialTheme.colorScheme.primary,
                            checkedTrackColor = MaterialTheme.colorScheme.primaryContainer
                        )
                    )
                }

                // Custom dimensions toggle
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Custom Dimensions",
                            style = MaterialTheme.typography.bodyLarge
                        )
                        Text(
                            text = "Original: ${originalWidth}x${originalHeight}px",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Switch(
                        checked = useCustomDimensions,
                        onCheckedChange = { useCustomDimensions = it },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = MaterialTheme.colorScheme.primary,
                            checkedTrackColor = MaterialTheme.colorScheme.primaryContainer
                        )
                    )
                }

                // Scale slider (only show when custom dimensions enabled)
                if (useCustomDimensions) {
                    Column {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "Scale",
                                style = MaterialTheme.typography.bodyMedium
                            )
                            Text(
                                text = "${customWidth}x${customHeight}px (${(scaleFactor * 100).toInt()}%)",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                        Slider(
                            value = scaleFactor,
                            onValueChange = { scaleFactor = it },
                            valueRange = 0.25f..4f,
                            colors = SliderDefaults.colors(
                                thumbColor = MaterialTheme.colorScheme.primary,
                                activeTrackColor = MaterialTheme.colorScheme.primary
                            )
                        )
                    }
                }

                // Quality slider
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "Export Quality",
                            style = MaterialTheme.typography.bodyMedium
                        )
                        Text(
                            text = "$quality%",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                    Slider(
                        value = quality.toFloat(),
                        onValueChange = { quality = it.toInt() },
                        valueRange = 50f..100f,
                        colors = SliderDefaults.colors(
                            thumbColor = MaterialTheme.colorScheme.primary,
                            activeTrackColor = MaterialTheme.colorScheme.primary
                        )
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Action buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    TextButton(
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Cancel")
                    }
                    GradientButton(
                        text = "Export",
                        onClick = {
                            onExport(
                                ExportOptions(
                                    transparentBackground = transparentBackground,
                                    customWidth = customWidth,
                                    customHeight = customHeight,
                                    quality = quality
                                )
                            )
                        },
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }
    }
}
