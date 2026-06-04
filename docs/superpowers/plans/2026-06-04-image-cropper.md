# Image Cropper Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Add a fullscreen custom Compose crop screen that appears between gallery image selection and canvas placement, allowing the user to free-form crop an image before it is centered on the drawing canvas.

**Architecture:** A new `ImageCropScreen` (Compose) backed by `ImageCropViewModel` handles the crop UI and bitmap processing. The screen sits between the gallery picker and `DrawingScreen` via a new `Screen.ImageCrop` nav route. Results are passed back to `DrawingScreen` via `SavedStateHandle` on the previous backstack entry; `DrawingScreen` receives three new optional parameters (`pendingCropPath`, `pendingCropWidth`, `pendingCropHeight`) and calls `DrawingViewModel.addImage()` when they arrive.

**Tech Stack:** Jetpack Compose, Compose Navigation, Koin (viewModelOf), Kotlin Coroutines + `Dispatchers.IO`, Android `BitmapFactory` / `Bitmap.createBitmap`, JUnit 4, kotlinx-coroutines-test

---

## File Map

| Action | File |
|--------|------|
| **Create** | `presentation/feature/drawing/ImageCropViewModel.kt` |
| **Create** | `presentation/feature/drawing/ImageCropScreen.kt` |
| **Create** | `test/.../presentation/feature/drawing/ImageCropViewModelTest.kt` |
| **Create** | `androidTest/.../presentation/feature/drawing/ImageCropScreenTest.kt` |
| **Modify** | `di/RepositoryModule.kt` — register `ImageCropViewModel` |
| **Modify** | `presentation/common/Navigation.kt` — add `Screen.ImageCrop`, composable destination, crop result observation |
| **Modify** | `presentation/feature/drawing/DrawingScreen.kt` — new params, navigate-to-crop, process crop result |
| **Modify** | `app/build.gradle.kts` — add `kotlinx-coroutines-test` test dep |
| **Modify** | `gradle/libs.versions.toml` — add coroutines-test catalog entry |

Full paths (package root `app/src/main/java/com/hotmail/arehmananis/sketchapp/`):

- `presentation/feature/drawing/ImageCropViewModel.kt`
- `presentation/feature/drawing/ImageCropScreen.kt`
- `app/src/test/java/com/hotmail/arehmananis/sketchapp/presentation/feature/drawing/ImageCropViewModelTest.kt`
- `app/src/androidTest/java/com/hotmail/arehmananis/sketchapp/presentation/feature/drawing/ImageCropScreenTest.kt`

---

## Task 1: Add `kotlinx-coroutines-test` dependency

**Files:**
- Modify: `gradle/libs.versions.toml`
- Modify: `app/build.gradle.kts`

- [ ] **Step 1: Add version catalog entry**

In `gradle/libs.versions.toml`, find the `[versions]` section and add the coroutines version (match existing coroutines usage — add to `[libraries]` only, no new version needed since it piggybacks on `kotlinx-coroutines-core`):

Find the `[libraries]` section. After the last library entry add:
```toml
kotlinx-coroutines-test = { group = "org.jetbrains.kotlinx", name = "kotlinx-coroutines-test", version = "1.8.1" }
```

- [ ] **Step 2: Add test dependency in `app/build.gradle.kts`**

Find the `dependencies { }` block. In the `testImplementation` section (alongside `libs.junit`) add:
```kotlin
testImplementation(libs.kotlinx.coroutines.test)
```

- [ ] **Step 3: Sync and verify**

```bash
./gradlew :app:dependencies --configuration testDebugRuntimeClasspath | grep coroutines-test
```
Expected: a line containing `kotlinx-coroutines-test`

- [ ] **Step 4: Commit**

```bash
git add gradle/libs.versions.toml app/build.gradle.kts
git commit -m "build: add kotlinx-coroutines-test for ViewModel unit tests"
```

---

## Task 2: Create `ImageCropViewModel` — crop rect state and handle drag logic

**Files:**
- Create: `app/src/main/java/com/hotmail/arehmananis/sketchapp/presentation/feature/drawing/ImageCropViewModel.kt`
- Create: `app/src/test/java/com/hotmail/arehmananis/sketchapp/presentation/feature/drawing/ImageCropViewModelTest.kt`

- [ ] **Step 1: Write failing unit tests**

Create `app/src/test/java/com/hotmail/arehmananis/sketchapp/presentation/feature/drawing/ImageCropViewModelTest.kt`:

```kotlin
package com.hotmail.arehmananis.sketchapp.presentation.feature.drawing

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class ImageCropViewModelTest {

    private lateinit var viewModel: ImageCropViewModel

    @Before
    fun setUp() {
        viewModel = ImageCropViewModel()
        viewModel.setImageDimensions(1000, 1000)
    }

    @Test
    fun `initial crop rect covers 80 percent of image`() {
        val rect = viewModel.cropRect.value
        assertEquals(0.1f, rect.left, 0.001f)
        assertEquals(0.1f, rect.top, 0.001f)
        assertEquals(0.9f, rect.right, 0.001f)
        assertEquals(0.9f, rect.bottom, 0.001f)
    }

    @Test
    fun `dragging TOP_LEFT handle clamps left to 0`() {
        viewModel.onHandleDrag(CropHandle.TOP_LEFT, -2f, 0f)
        assertEquals(0f, viewModel.cropRect.value.left, 0.001f)
    }

    @Test
    fun `dragging TOP_LEFT handle clamps top to 0`() {
        viewModel.onHandleDrag(CropHandle.TOP_LEFT, 0f, -2f)
        assertEquals(0f, viewModel.cropRect.value.top, 0.001f)
    }

    @Test
    fun `dragging BOTTOM_RIGHT handle clamps right to 1`() {
        viewModel.onHandleDrag(CropHandle.BOTTOM_RIGHT, 2f, 2f)
        assertEquals(1f, viewModel.cropRect.value.right, 0.001f)
        assertEquals(1f, viewModel.cropRect.value.bottom, 0.001f)
    }

    @Test
    fun `dragging MIDDLE_RIGHT enforces minimum width of 50px`() {
        // Collapse right edge past left — should stop at left + minFrac
        viewModel.onHandleDrag(CropHandle.MIDDLE_RIGHT, -2f, 0f)
        val rect = viewModel.cropRect.value
        val minFrac = 50f / 1000f
        assertTrue("width must be >= minFrac", rect.right - rect.left >= minFrac - 0.001f)
    }

    @Test
    fun `dragging BOTTOM_CENTER enforces minimum height of 50px`() {
        viewModel.onHandleDrag(CropHandle.BOTTOM_CENTER, 0f, -2f)
        val rect = viewModel.cropRect.value
        val minFrac = 50f / 1000f
        assertTrue("height must be >= minFrac", rect.bottom - rect.top >= minFrac - 0.001f)
    }

    @Test
    fun `dragging TOP_CENTER moves only top edge`() {
        val before = viewModel.cropRect.value
        viewModel.onHandleDrag(CropHandle.TOP_CENTER, 0f, 0.05f)
        val after = viewModel.cropRect.value
        assertEquals(before.left, after.left, 0.001f)
        assertEquals(before.right, after.right, 0.001f)
        assertEquals(before.bottom, after.bottom, 0.001f)
        assertEquals(before.top + 0.05f, after.top, 0.001f)
    }

    @Test
    fun `dragging MIDDLE_LEFT moves only left edge`() {
        val before = viewModel.cropRect.value
        viewModel.onHandleDrag(CropHandle.MIDDLE_LEFT, 0.05f, 0f)
        val after = viewModel.cropRect.value
        assertEquals(before.top, after.top, 0.001f)
        assertEquals(before.right, after.right, 0.001f)
        assertEquals(before.bottom, after.bottom, 0.001f)
        assertEquals(before.left + 0.05f, after.left, 0.001f)
    }
}
```

- [ ] **Step 2: Run tests to confirm they fail**

```bash
./gradlew :app:testDebugUnitTest --tests "*.ImageCropViewModelTest" 2>&1 | tail -20
```
Expected: FAILED — `ImageCropViewModel` and `CropHandle` not found

- [ ] **Step 3: Create `ImageCropViewModel.kt`**

Create `app/src/main/java/com/hotmail/arehmananis/sketchapp/presentation/feature/drawing/ImageCropViewModel.kt`:

```kotlin
package com.hotmail.arehmananis.sketchapp.presentation.feature.drawing

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.io.File
import java.util.UUID

enum class CropHandle {
    TOP_LEFT, TOP_CENTER, TOP_RIGHT,
    MIDDLE_LEFT, MIDDLE_RIGHT,
    BOTTOM_LEFT, BOTTOM_CENTER, BOTTOM_RIGHT
}

data class CropRect(
    val left: Float = 0.1f,
    val top: Float = 0.1f,
    val right: Float = 0.9f,
    val bottom: Float = 0.9f
)

data class CropResult(
    val filePath: String,
    val width: Int,
    val height: Int
)

class ImageCropViewModel : ViewModel() {

    private val _cropRect = MutableStateFlow(CropRect())
    val cropRect: StateFlow<CropRect> = _cropRect.asStateFlow()

    private val _isProcessing = MutableStateFlow(false)
    val isProcessing: StateFlow<Boolean> = _isProcessing.asStateFlow()

    private val _cropResult = MutableSharedFlow<CropResult>(extraBufferCapacity = 1)
    val cropResult: SharedFlow<CropResult> = _cropResult.asSharedFlow()

    private val _error = MutableSharedFlow<String>(extraBufferCapacity = 1)
    val error: SharedFlow<String> = _error.asSharedFlow()

    private var imageWidth: Int = 0
    private var imageHeight: Int = 0

    fun setImageDimensions(width: Int, height: Int) {
        imageWidth = width
        imageHeight = height
    }

    fun onHandleDrag(handle: CropHandle, fracDx: Float, fracDy: Float) {
        val r = _cropRect.value
        val minW = if (imageWidth > 0) 50f / imageWidth else 0.05f
        val minH = if (imageHeight > 0) 50f / imageHeight else 0.05f

        _cropRect.value = when (handle) {
            CropHandle.TOP_LEFT -> r.copy(
                left = (r.left + fracDx).coerceIn(0f, r.right - minW),
                top = (r.top + fracDy).coerceIn(0f, r.bottom - minH)
            )
            CropHandle.TOP_CENTER -> r.copy(
                top = (r.top + fracDy).coerceIn(0f, r.bottom - minH)
            )
            CropHandle.TOP_RIGHT -> r.copy(
                right = (r.right + fracDx).coerceIn(r.left + minW, 1f),
                top = (r.top + fracDy).coerceIn(0f, r.bottom - minH)
            )
            CropHandle.MIDDLE_LEFT -> r.copy(
                left = (r.left + fracDx).coerceIn(0f, r.right - minW)
            )
            CropHandle.MIDDLE_RIGHT -> r.copy(
                right = (r.right + fracDx).coerceIn(r.left + minW, 1f)
            )
            CropHandle.BOTTOM_LEFT -> r.copy(
                left = (r.left + fracDx).coerceIn(0f, r.right - minW),
                bottom = (r.bottom + fracDy).coerceIn(r.top + minH, 1f)
            )
            CropHandle.BOTTOM_CENTER -> r.copy(
                bottom = (r.bottom + fracDy).coerceIn(r.top + minH, 1f)
            )
            CropHandle.BOTTOM_RIGHT -> r.copy(
                right = (r.right + fracDx).coerceIn(r.left + minW, 1f),
                bottom = (r.bottom + fracDy).coerceIn(r.top + minH, 1f)
            )
        }
    }

    fun confirmCrop(uri: Uri, context: Context) {
        viewModelScope.launch(Dispatchers.IO) {
            _isProcessing.value = true
            try {
                val source = context.contentResolver.openInputStream(uri)
                    ?.use { BitmapFactory.decodeStream(it) }
                    ?: throw Exception("Could not read image")

                val rect = _cropRect.value
                val x = (rect.left * source.width).toInt().coerceIn(0, source.width - 1)
                val y = (rect.top * source.height).toInt().coerceIn(0, source.height - 1)
                val w = ((rect.right - rect.left) * source.width).toInt()
                    .coerceAtLeast(50).coerceAtMost(source.width - x)
                val h = ((rect.bottom - rect.top) * source.height).toInt()
                    .coerceAtLeast(50).coerceAtMost(source.height - y)

                val cropped = Bitmap.createBitmap(source, x, y, w, h)
                source.recycle()

                val imagesDir = File(context.filesDir, "images").also { it.mkdirs() }
                val outFile = File(imagesDir, "${UUID.randomUUID()}.jpg")
                outFile.outputStream().use { cropped.compress(Bitmap.CompressFormat.JPEG, 95, it) }
                cropped.recycle()

                _cropResult.emit(CropResult(outFile.absolutePath, w, h))
            } catch (e: Exception) {
                _error.emit("Failed to crop image: ${e.message}")
            } finally {
                _isProcessing.value = false
            }
        }
    }
}
```

- [ ] **Step 4: Run tests to confirm they pass**

```bash
./gradlew :app:testDebugUnitTest --tests "*.ImageCropViewModelTest" 2>&1 | tail -20
```
Expected: BUILD SUCCESSFUL, 8 tests passed

- [ ] **Step 5: Commit**

```bash
git add app/src/main/java/com/hotmail/arehmananis/sketchapp/presentation/feature/drawing/ImageCropViewModel.kt \
        app/src/test/java/com/hotmail/arehmananis/sketchapp/presentation/feature/drawing/ImageCropViewModelTest.kt
git commit -m "feat: add ImageCropViewModel with crop rect state and handle drag logic"
```

---

## Task 3: Register `ImageCropViewModel` in Koin + add `Screen.ImageCrop` navigation route

**Files:**
- Modify: `app/src/main/java/com/hotmail/arehmananis/sketchapp/di/RepositoryModule.kt`
- Modify: `app/src/main/java/com/hotmail/arehmananis/sketchapp/presentation/common/Navigation.kt`

- [ ] **Step 1: Register `ImageCropViewModel` in Koin**

In `RepositoryModule.kt`, find the `/** ViewModel bindings */` section. After `viewModelOf(::DrawingViewModel)` add:

```kotlin
viewModelOf(::ImageCropViewModel)
```

Also add the import at the top of the file:
```kotlin
import com.hotmail.arehmananis.sketchapp.presentation.feature.drawing.ImageCropViewModel
```

- [ ] **Step 2: Add `Screen.ImageCrop` sealed object to `Navigation.kt`**

In `Navigation.kt`, inside the `sealed class Screen(...)` body, after the `Settings` object add:

```kotlin
object ImageCrop : Screen("image_crop?uri={uri}", "Crop Image") {
    fun createRoute(uriString: String): String =
        "image_crop?uri=${android.net.Uri.encode(uriString)}"
}
```

- [ ] **Step 3: Verify compile**

```bash
./gradlew :app:compileDebugKotlin 2>&1 | grep -E "error:|warning:" | head -20
```
Expected: no errors

- [ ] **Step 4: Commit**

```bash
git add app/src/main/java/com/hotmail/arehmananis/sketchapp/di/RepositoryModule.kt \
        app/src/main/java/com/hotmail/arehmananis/sketchapp/presentation/common/Navigation.kt
git commit -m "feat: register ImageCropViewModel in Koin and add Screen.ImageCrop nav route"
```

---

## Task 4: Build `ImageCropScreen` composable

**Files:**
- Create: `app/src/main/java/com/hotmail/arehmananis/sketchapp/presentation/feature/drawing/ImageCropScreen.kt`

- [ ] **Step 1: Create `ImageCropScreen.kt`**

Create `app/src/main/java/com/hotmail/arehmananis/sketchapp/presentation/feature/drawing/ImageCropScreen.kt`:

```kotlin
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

                    // Draw the image scaled to fit
                    drawImage(
                        image = imageBitmap,
                        dstOffset = IntOffset(dispLeft.toInt(), dispTop.toInt()),
                        dstSize = IntSize(dispW.toInt(), dispH.toInt())
                    )

                    // Crop rect in screen coordinates
                    val cropL = dispLeft + cropRect.left * dispW
                    val cropT = dispTop + cropRect.top * dispH
                    val cropR = dispLeft + cropRect.right * dispW
                    val cropB = dispTop + cropRect.bottom * dispH

                    // Dark scrim outside crop rect (4 rects)
                    val scrim = Color(0x99000000)
                    drawRect(scrim, topLeft = Offset(0f, 0f), size = Size(size.width, cropT))
                    drawRect(scrim, topLeft = Offset(0f, cropB), size = Size(size.width, size.height - cropB))
                    drawRect(scrim, topLeft = Offset(0f, cropT), size = Size(cropL, cropB - cropT))
                    drawRect(scrim, topLeft = Offset(cropR, cropT), size = Size(size.width - cropR, cropB - cropT))

                    // Crop rect border
                    drawRect(
                        color = Color.White,
                        topLeft = Offset(cropL, cropT),
                        size = Size(cropR - cropL, cropB - cropT),
                        style = Stroke(width = 2f)
                    )

                    // Drag handles
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
```

- [ ] **Step 2: Verify compile**

```bash
./gradlew :app:compileDebugKotlin 2>&1 | grep -E "error:" | head -20
```
Expected: no errors

- [ ] **Step 3: Commit**

```bash
git add app/src/main/java/com/hotmail/arehmananis/sketchapp/presentation/feature/drawing/ImageCropScreen.kt
git commit -m "feat: add ImageCropScreen composable with crop overlay and handle drag"
```

---

## Task 5: Wire navigation and `DrawingScreen` to use the crop screen

**Files:**
- Modify: `app/src/main/java/com/hotmail/arehmananis/sketchapp/presentation/common/Navigation.kt`
- Modify: `app/src/main/java/com/hotmail/arehmananis/sketchapp/presentation/feature/drawing/DrawingScreen.kt`

- [ ] **Step 1: Update `DrawingScreen` signature and launcher**

In `DrawingScreen.kt`, update the `DrawingScreen` function signature to add four new optional parameters (with defaults so existing callers compile unchanged):

```kotlin
@Composable
fun DrawingScreen(
    sketchId: String? = null,
    viewModel: DrawingViewModel = koinViewModel(),
    onBack: () -> Unit,
    onNavigateToCrop: (String) -> Unit = {},
    pendingCropPath: String? = null,
    pendingCropWidth: Int? = null,
    pendingCropHeight: Int? = null,
    onCropResultConsumed: () -> Unit = {}
)
```

In the `DrawingScreen` body, change the `imagePickerLauncher` callback from:
```kotlin
) { uri ->
    uri?.let { viewModel.onImagePicked(it, context, canvasWidth, canvasHeight) }
}
```
to:
```kotlin
) { uri ->
    uri?.let { onNavigateToCrop(it.toString()) }
}
```

Then add a new `LaunchedEffect` **after** the existing `LaunchedEffect` blocks, before the `Box`:
```kotlin
LaunchedEffect(pendingCropPath) {
    if (pendingCropPath != null && pendingCropWidth != null && pendingCropHeight != null
        && canvasWidth > 0 && canvasHeight > 0) {
        val maxDim = minOf(canvasWidth, canvasHeight) * 0.6f
        val scale = minOf(maxDim / pendingCropWidth, maxDim / pendingCropHeight, 1f)
        val w = (pendingCropWidth * scale).coerceAtLeast(50f)
        val h = (pendingCropHeight * scale).coerceAtLeast(50f)
        viewModel.addImage(
            imagePath = pendingCropPath,
            x = canvasWidth / 2f,
            y = canvasHeight / 2f,
            width = w,
            height = h
        )
        onCropResultConsumed()
    }
}
```

- [ ] **Step 2: Add `Screen.ImageCrop` composable destination to NavHost**

In `Navigation.kt`, add the necessary imports at the top:
```kotlin
import com.hotmail.arehmananis.sketchapp.presentation.feature.drawing.ImageCropScreen
import androidx.navigation.navArgument
import androidx.navigation.NavType
import androidx.compose.runtime.collectAsState
```

Inside the `NavHost { }` block, after the Drawing screen `composable { }` entry, add:

```kotlin
// Crop screen (no bottom bar)
composable(
    route = Screen.ImageCrop.route,
    arguments = listOf(
        navArgument("uri") { type = NavType.StringType }
    )
) { backStackEntry ->
    val uriString = backStackEntry.arguments?.getString("uri") ?: return@composable
    ImageCropScreen(
        uriString = uriString,
        onCropConfirmed = { filePath, width, height ->
            navController.previousBackStackEntry?.savedStateHandle?.let { handle ->
                handle["crop_file_path"] = filePath
                handle["crop_width"] = width
                handle["crop_height"] = height
            }
            navController.popBackStack()
        },
        onCancel = { navController.popBackStack() }
    )
}
```

- [ ] **Step 3: Update the Drawing composable block to observe savedStateHandle and pass crop result to `DrawingScreen`**

Replace the existing Drawing `composable { backStackEntry -> ... }` block in `Navigation.kt`:

```kotlin
composable(
    route = Screen.Drawing.route,
    arguments = listOf(
        navArgument("sketchId") {
            type = NavType.StringType
            nullable = true
        }
    )
) { backStackEntry ->
    val sketchId = backStackEntry.arguments?.getString("sketchId")

    val pendingCropPath = backStackEntry.savedStateHandle
        .getStateFlow<String?>("crop_file_path", null)
        .collectAsState().value
    val pendingCropWidth = backStackEntry.savedStateHandle
        .getStateFlow<Int?>("crop_width", null)
        .collectAsState().value
    val pendingCropHeight = backStackEntry.savedStateHandle
        .getStateFlow<Int?>("crop_height", null)
        .collectAsState().value

    DrawingScreen(
        sketchId = if (sketchId == "new") null else sketchId,
        onBack = { navController.popBackStack() },
        onNavigateToCrop = { uriString ->
            navController.navigate(Screen.ImageCrop.createRoute(uriString))
        },
        pendingCropPath = pendingCropPath,
        pendingCropWidth = pendingCropWidth,
        pendingCropHeight = pendingCropHeight,
        onCropResultConsumed = {
            backStackEntry.savedStateHandle.remove<String>("crop_file_path")
            backStackEntry.savedStateHandle.remove<Int>("crop_width")
            backStackEntry.savedStateHandle.remove<Int>("crop_height")
        }
    )
}
```

- [ ] **Step 4: Verify compile**

```bash
./gradlew :app:compileDebugKotlin 2>&1 | grep -E "error:" | head -20
```
Expected: no errors

- [ ] **Step 5: Build debug APK to confirm it assembles**

```bash
./gradlew assembleDevDebug 2>&1 | tail -10
```
Expected: BUILD SUCCESSFUL

- [ ] **Step 6: Commit**

```bash
git add app/src/main/java/com/hotmail/arehmananis/sketchapp/presentation/feature/drawing/DrawingScreen.kt \
        app/src/main/java/com/hotmail/arehmananis/sketchapp/presentation/common/Navigation.kt
git commit -m "feat: wire DrawingScreen and Navigation to use ImageCropScreen for image import"
```

---

## Task 6: Instrumented tests

**Files:**
- Create: `app/src/androidTest/java/com/hotmail/arehmananis/sketchapp/presentation/feature/drawing/ImageCropScreenTest.kt`

- [ ] **Step 1: Create instrumented test file**

Create `app/src/androidTest/java/com/hotmail/arehmananis/sketchapp/presentation/feature/drawing/ImageCropScreenTest.kt`:

```kotlin
package com.hotmail.arehmananis.sketchapp.presentation.feature.drawing

import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.platform.app.InstrumentationRegistry
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import java.io.File

class ImageCropScreenTest {

    @get:Rule
    val composeRule = createComposeRule()

    private lateinit var context: Context
    private lateinit var testImageUri: Uri

    @Before
    fun setUp() {
        context = InstrumentationRegistry.getInstrumentation().targetContext
        // Create a 200x200 test bitmap and save it to a temp file
        val bmp = Bitmap.createBitmap(200, 200, Bitmap.Config.ARGB_8888)
        val file = File(context.cacheDir, "test_crop_image.jpg")
        file.outputStream().use { bmp.compress(Bitmap.CompressFormat.JPEG, 90, it) }
        bmp.recycle()
        testImageUri = Uri.fromFile(file)
    }

    @Test
    fun cancelDismissesScreenWithoutConfirming() {
        var confirmed = false

        composeRule.setContent {
            ImageCropScreen(
                uriString = testImageUri.toString(),
                onCropConfirmed = { _, _, _ -> confirmed = true },
                onCancel = {}
            )
        }

        composeRule.onNodeWithText("Cancel").performClick()
        composeRule.waitForIdle()

        assertTrue("onCropConfirmed should NOT be called on cancel", !confirmed)
    }

    @Test
    fun doneButtonTriggersConfirmationWithValidDimensions() {
        var resultWidth = 0
        var resultHeight = 0

        composeRule.setContent {
            ImageCropScreen(
                uriString = testImageUri.toString(),
                onCropConfirmed = { _, w, h ->
                    resultWidth = w
                    resultHeight = h
                },
                onCancel = {}
            )
        }

        // Wait for bitmap to load
        composeRule.waitUntil(timeoutMillis = 5000) {
            composeRule.onNodeWithText("Done").isDisplayed()
        }
        composeRule.onNodeWithText("Done").performClick()

        // Wait for async crop to complete
        composeRule.waitUntil(timeoutMillis = 5000) { resultWidth > 0 }

        assertTrue("Cropped width must be >= 50px", resultWidth >= 50)
        assertTrue("Cropped height must be >= 50px", resultHeight >= 50)
        // Default crop rect covers 80% of image (0.1 to 0.9), so 200 * 0.8 = 160
        assertEquals(160, resultWidth)
        assertEquals(160, resultHeight)
    }
}

private fun androidx.compose.ui.test.SemanticsNodeInteraction.isDisplayed(): Boolean {
    return try {
        assertExists(); true
    } catch (_: AssertionError) {
        false
    }
}
```

- [ ] **Step 2: Run instrumented tests (requires a connected device or emulator)**

```bash
./gradlew :app:connectedDevDebugAndroidTest \
  --tests "*.ImageCropScreenTest" 2>&1 | tail -30
```
Expected: BUILD SUCCESSFUL, 2 tests passed

- [ ] **Step 3: Commit**

```bash
git add app/src/androidTest/java/com/hotmail/arehmananis/sketchapp/presentation/feature/drawing/ImageCropScreenTest.kt
git commit -m "test: add instrumented tests for ImageCropScreen cancel and done behavior"
```

---

## Self-Review Checklist

| Spec Requirement | Covered By |
|-----------------|-----------|
| Fullscreen crop screen (not overlay) | `ImageCropScreen` as a separate nav destination |
| Free-form crop (no aspect ratio lock) | `CropHandle` with independent edge/corner drag |
| 8 drag handles (4 corners + 4 edge midpoints) | `handleScreenPositions()` in `ImageCropScreen` |
| Done → cropped image centered on canvas | `LaunchedEffect(pendingCropPath)` in `DrawingScreen` |
| Cancel → no image added | `onCancel = { navController.popBackStack() }`, nothing written |
| Minimum 50×50px crop | `coerceIn` logic in `onHandleDrag` using `50f / imageWidth` |
| Error shown as Snackbar, user stays | `viewModel.error` → `snackbarHostState.showSnackbar()` |
| Bitmap written to `filesDir/images/` | `confirmCrop()` writes `context.filesDir/images/<uuid>.jpg` |
| Unit tests: rect clamping | Task 2 — `ImageCropViewModelTest` |
| Unit tests: min size enforcement | Task 2 — `ImageCropViewModelTest` |
| UI tests: cancel behavior | Task 6 — `ImageCropScreenTest.cancelDismissesScreen...` |
| UI tests: done + dimensions | Task 6 — `ImageCropScreenTest.doneButtonTriggers...` |
| Undo/redo unchanged | `addImage()` in `DrawingViewModel` already records `UndoAction.ImageAdded` |
| KMP readiness maintained | `ImageCropViewModel` has no Android framework in logic; `confirmCrop` is isolated |
