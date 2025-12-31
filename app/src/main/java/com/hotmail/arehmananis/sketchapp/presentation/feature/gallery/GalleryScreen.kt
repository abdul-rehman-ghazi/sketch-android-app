package com.hotmail.arehmananis.sketchapp.presentation.feature.gallery

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.hotmail.arehmananis.sketchapp.domain.model.Sketch
import com.hotmail.arehmananis.sketchapp.domain.model.SyncStatus
import com.hotmail.arehmananis.sketchapp.presentation.common.components.GradientButton
import com.hotmail.arehmananis.sketchapp.presentation.theme.AppShapes
import com.hotmail.arehmananis.sketchapp.presentation.theme.VibrantIndigo
import com.hotmail.arehmananis.sketchapp.presentation.theme.VibrantPurple
import org.koin.androidx.compose.koinViewModel

/**
 * Actions available in sketch card menu
 */
enum class MenuAction {
    Rename,
    Delete
}

/**
 * Gallery screen showing all user sketches
 */
@Composable
fun GalleryScreen(
    onCreateNewSketch: () -> Unit,
    onSketchClick: (String) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: GalleryViewModel = koinViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val showDeleteDialog by viewModel.showDeleteDialog.collectAsStateWithLifecycle()
    val showRenameDialog by viewModel.showRenameDialog.collectAsStateWithLifecycle()
    val deleteInProgress by viewModel.deleteInProgress.collectAsStateWithLifecycle()
    val renameInProgress by viewModel.renameInProgress.collectAsStateWithLifecycle()
    val operationError by viewModel.operationError.collectAsStateWithLifecycle()

    Scaffold(
        modifier = modifier,
        floatingActionButton = {
            // Modern gradient FAB with larger size
            FloatingActionButton(
                onClick = onCreateNewSketch,
                containerColor = Color.Transparent,
                modifier = Modifier
                    .size(64.dp)
                    .clip(MaterialTheme.shapes.large)
                    .background(
                        brush = Brush.linearGradient(
                            colors = listOf(VibrantPurple, VibrantIndigo)
                        ),
                        shape = MaterialTheme.shapes.large
                    ),
                elevation = FloatingActionButtonDefaults.elevation(
                    defaultElevation = 6.dp,
                    pressedElevation = 8.dp
                )
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Create new sketch",
                    tint = Color.White,
                    modifier = Modifier.size(32.dp)
                )
            }
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            when (val state = uiState) {
                is GalleryUiState.Loading -> {
                    CircularProgressIndicator(
                        modifier = Modifier.align(Alignment.Center)
                    )
                }

                is GalleryUiState.Success -> {
                    if (state.sketches.isEmpty()) {
                        EmptyGalleryMessage(
                            modifier = Modifier.align(Alignment.Center)
                        )
                    } else {
                        SketchGrid(
                            sketches = state.sketches,
                            onSketchClick = onSketchClick,
                            onMenuAction = { action, sketch ->
                                when (action) {
                                    MenuAction.Rename -> viewModel.showRenameDialog(sketch)
                                    MenuAction.Delete -> viewModel.showDeleteConfirmation(sketch.id)
                                }
                            }
                        )
                    }
                }

                is GalleryUiState.Error -> {
                    ErrorMessage(
                        message = state.message,
                        onRetry = viewModel::refresh,
                        modifier = Modifier.align(Alignment.Center)
                    )
                }
            }
        }
    }

    // Dialog overlays
    showDeleteDialog?.let { sketchId ->
        DeleteConfirmationDialog(
            onConfirm = { viewModel.confirmDelete(sketchId) },
            onDismiss = { viewModel.dismissDeleteDialog() },
            isLoading = deleteInProgress,
            error = operationError
        )
    }

    showRenameDialog?.let { sketch ->
        RenameSketchDialog(
            currentTitle = sketch.title,
            onConfirm = { newTitle -> viewModel.confirmRename(sketch, newTitle) },
            onDismiss = { viewModel.dismissRenameDialog() },
            isLoading = renameInProgress,
            error = operationError
        )
    }
}

@Composable
private fun SketchGrid(
    sketches: List<Sketch>,
    onSketchClick: (String) -> Unit,
    onMenuAction: (MenuAction, Sketch) -> Unit
) {
    LazyVerticalGrid(
        columns = GridCells.Fixed(2),
        contentPadding = PaddingValues(16.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        items(sketches, key = { it.id }) { sketch ->
            SketchCard(
                sketch = sketch,
                onClick = { onSketchClick(sketch.id) },
                onMenuAction = onMenuAction
            )
        }
    }
}

/**
 * Modern sketch card with rounded corners, gradient overlay, and smooth animations
 */
@Composable
private fun SketchCard(
    sketch: Sketch,
    onClick: () -> Unit,
    onMenuAction: (MenuAction, Sketch) -> Unit
) {
    var showMenu by remember { mutableStateOf(false) }
    var isPressed by remember { mutableStateOf(false) }

    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.95f else 1f,
        label = "card_scale"
    )

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(1f)
            .scale(scale)
            .clickable(
                onClick = {
                    isPressed = true
                    onClick()
                    isPressed = false
                }
            ),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        shape = AppShapes.medium // Modern rounded corners (16dp)
    ) {
        Box {
            // Sketch image
            AsyncImage(
                model = sketch.localImagePath ?: sketch.remoteImageUrl,
                contentDescription = sketch.title,
                modifier = Modifier
                    .fillMaxSize()
                    .clip(AppShapes.medium),
                contentScale = ContentScale.Crop
            )

            // 3-dot menu overlay with modern background
            Surface(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(8.dp),
                color = MaterialTheme.colorScheme.surface.copy(alpha = 0.7f),
                shape = MaterialTheme.shapes.small
            ) {
                IconButton(
                    onClick = { showMenu = true },
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.MoreVert,
                        contentDescription = "More options",
                        tint = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            // Dropdown menu
            DropdownMenu(
                expanded = showMenu,
                onDismissRequest = { showMenu = false }
            ) {
                DropdownMenuItem(
                    text = { Text("Rename") },
                    onClick = {
                        showMenu = false
                        onMenuAction(MenuAction.Rename, sketch)
                    }
                )
                DropdownMenuItem(
                    text = { Text("Delete") },
                    onClick = {
                        showMenu = false
                        onMenuAction(MenuAction.Delete, sketch)
                    }
                )
            }

            // Sync status indicator
            if (sketch.syncStatus != SyncStatus.SYNCED) {
                Surface(
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .padding(8.dp),
                    color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.9f),
                    shape = MaterialTheme.shapes.small
                ) {
                    Text(
                        text = when (sketch.syncStatus) {
                            SyncStatus.PENDING_UPLOAD -> "⟳"
                            SyncStatus.SYNCING -> "↻"
                            SyncStatus.PENDING_DOWNLOAD -> "↓"
                            SyncStatus.CONFLICT -> "⚠"
                            else -> ""
                        },
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 4.dp),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
            }

            // Modern gradient overlay for title (transparent to dark)
            Box(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .height(60.dp)
                    .background(
                        brush = Brush.verticalGradient(
                            colors = listOf(
                                Color.Transparent,
                                Color.Black.copy(alpha = 0.7f)
                            )
                        )
                    )
            )

            // Title text
            Text(
                text = sketch.title,
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(12.dp),
                style = MaterialTheme.typography.titleMedium,
                color = Color.White,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
private fun EmptyGalleryMessage(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier.padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = "No sketches yet",
            style = MaterialTheme.typography.headlineSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = "Tap the + button to create your first sketch",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

/**
 * Error message with modern gradient retry button
 */
@Composable
private fun ErrorMessage(
    message: String,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        Text(
            text = message,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.error
        )
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                Icons.Default.Refresh,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary
            )
            GradientButton(
                text = "Retry",
                onClick = onRetry
            )
        }
    }
}
