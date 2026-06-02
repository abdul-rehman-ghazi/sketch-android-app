package com.hotmail.arehmananis.sketchapp.presentation.feature.drawing

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Redo
import androidx.compose.material.icons.automirrored.filled.Undo
import androidx.compose.material.icons.filled.AddPhotoAlternate
import androidx.compose.material.icons.filled.DeleteForever
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DrawingTopBar(
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
    onClear: () -> Unit
) {
    val actionEnabled = !isSaving && hasContent
    val disabledTint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f)

    TopAppBar(
        title = {
            Text(
                text = "Drawing",
                style = MaterialTheme.typography.titleLarge
            )
        },
        navigationIcon = {
            IconButton(onClick = onBack) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back"
                )
            }
        },
        actions = {
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
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = MaterialTheme.colorScheme.surface,
            titleContentColor = MaterialTheme.colorScheme.onSurface
        )
    )
}