package com.hotmail.arehmananis.sketchapp.presentation.feature.drawing

import android.graphics.BitmapFactory
import android.net.Uri
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.koin.androidx.compose.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ImageCropScreen(
    uriString: String,
    onCropConfirmed: (filePath: String, width: Int, height: Int) -> Unit,
    onCancel: () -> Unit,
    viewModel: ImageCropViewModel = koinViewModel()
) {
    val context = LocalContext.current
    val cropRect by viewModel.cropRect.collectAsStateWithLifecycle()
    val isProcessing by viewModel.isProcessing.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val uri = remember(uriString) { Uri.parse(uriString) }

    var displayBitmap by remember { mutableStateOf<android.graphics.Bitmap?>(null) }

    LaunchedEffect(uriString) {
        withContext(Dispatchers.IO) {
            val opts = BitmapFactory.Options().apply { inSampleSize = 2 }
            val bmp = context.contentResolver.openInputStream(uri)
                ?.use { BitmapFactory.decodeStream(it, null, opts) }
            bmp?.let { viewModel.setImageDimensions(it.width * 2, it.height * 2) }
            displayBitmap = bmp
        }
    }

    LaunchedEffect(Unit) {
        viewModel.cropResult.collect { result ->
            onCropConfirmed(result.filePath, result.width, result.height)
        }
    }

    LaunchedEffect(Unit) {
        viewModel.error.collect { msg -> snackbarHostState.showSnackbar(msg) }
    }

    val handleRadiusDp = 12.dp
    val handleRadiusPx = with(LocalDensity.current) { handleRadiusDp.toPx() }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text("Crop Image") },
                navigationIcon = {
                    IconButton(onClick = onCancel) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Cancel")
                    }
                },
                actions = {
                    TextButton(
                        onClick = { viewModel.confirmCrop(uri, context) },
                        enabled = !isProcessing
                    ) {
                        Text("Done")
                    }
                }
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(Color.Black)
        ) {
            val bmp = displayBitmap
            if (bmp != null) {
                val imageBitmap = remember(bmp) { bmp.asImageBitmap() }
                var activeHandle by remember { mutableStateOf<CropHandle?>(null) }

                Canvas(
                    modifier = Modifier
                        .fillMaxSize()
                        .pointerInput(bmp) {
                            detectDragGestures(
                                onDragStart = { offset ->
                                    val (dispW, dispH, dispLeft, dispTop) =
                                        imageDisplayBounds(size.width.toFloat(), size.height.toFloat(), bmp)
                                    val positions = handleScreenPositions(cropRect, dispLeft, dispTop, dispW, dispH)
                                    activeHandle = positions.entries
                                        .minByOrNull { (offset - it.value).getDistance() }
                                        ?.takeIf { (offset - it.value).getDistance() < handleRadiusPx * 2.5f }
                                        ?.key
                                },
                                onDrag = { change, dragAmount ->
                                    change.consume()
                                    val handle = activeHandle ?: return@detectDragGestures
                                    val (dispW, dispH, _, _) =
                                        imageDisplayBounds(size.width.toFloat(), size.height.toFloat(), bmp)
                                    viewModel.onHandleDrag(handle, dragAmount.x / dispW, dragAmount.y / dispH)
                                },
                                onDragEnd = { activeHandle = null },
                                onDragCancel = { activeHandle = null }
                            )
                        }
                ) {
                    val (dispW, dispH, dispLeft, dispTop) =
                        imageDisplayBounds(size.width, size.height, bmp)

                    drawImage(
                        image = imageBitmap,
                        dstOffset = IntOffset(dispLeft.toInt(), dispTop.toInt()),
                        dstSize = IntSize(dispW.toInt(), dispH.toInt())
                    )

                    val cropL = dispLeft + cropRect.left * dispW
                    val cropT = dispTop + cropRect.top * dispH
                    val cropR = dispLeft + cropRect.right * dispW
                    val cropB = dispTop + cropRect.bottom * dispH

                    val scrim = Color(0x99000000)
                    drawRect(scrim, topLeft = Offset(0f, 0f), size = Size(size.width, cropT))
                    drawRect(scrim, topLeft = Offset(0f, cropB), size = Size(size.width, size.height - cropB))
                    drawRect(scrim, topLeft = Offset(0f, cropT), size = Size(cropL, cropB - cropT))
                    drawRect(scrim, topLeft = Offset(cropR, cropT), size = Size(size.width - cropR, cropB - cropT))

                    drawRect(
                        color = Color.White,
                        topLeft = Offset(cropL, cropT),
                        size = Size(cropR - cropL, cropB - cropT),
                        style = Stroke(width = 2f)
                    )

                    handleScreenPositions(cropRect, dispLeft, dispTop, dispW, dispH).values.forEach { pos ->
                        drawCircle(color = Color.White, radius = handleRadiusPx, center = pos)
                    }
                }
            } else {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
            }

            if (isProcessing) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
            }
        }
    }
}

private data class DisplayBounds(
    val width: Float,
    val height: Float,
    val left: Float,
    val top: Float
)

private fun imageDisplayBounds(
    canvasW: Float,
    canvasH: Float,
    bmp: android.graphics.Bitmap
): DisplayBounds {
    val imgAspect = bmp.width.toFloat() / bmp.height.toFloat()
    val canvasAspect = canvasW / canvasH
    val (dispW, dispH) = if (imgAspect > canvasAspect) {
        canvasW to canvasW / imgAspect
    } else {
        canvasH * imgAspect to canvasH
    }
    return DisplayBounds(
        width = dispW,
        height = dispH,
        left = (canvasW - dispW) / 2f,
        top = (canvasH - dispH) / 2f
    )
}

private fun handleScreenPositions(
    rect: CropRect,
    dispLeft: Float,
    dispTop: Float,
    dispW: Float,
    dispH: Float
): Map<CropHandle, Offset> {
    val l = dispLeft + rect.left * dispW
    val t = dispTop + rect.top * dispH
    val r = dispLeft + rect.right * dispW
    val b = dispTop + rect.bottom * dispH
    val mx = (l + r) / 2f
    val my = (t + b) / 2f
    return mapOf(
        CropHandle.TOP_LEFT to Offset(l, t),
        CropHandle.TOP_CENTER to Offset(mx, t),
        CropHandle.TOP_RIGHT to Offset(r, t),
        CropHandle.MIDDLE_LEFT to Offset(l, my),
        CropHandle.MIDDLE_RIGHT to Offset(r, my),
        CropHandle.BOTTOM_LEFT to Offset(l, b),
        CropHandle.BOTTOM_CENTER to Offset(mx, b),
        CropHandle.BOTTOM_RIGHT to Offset(r, b)
    )
}
