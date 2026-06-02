# Stroke Size Preview Bubble Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Show a floating bubble above the slider thumb displaying a filled dot and pixel label while the user drags the stroke width slider in `DrawingToolbar`.

**Architecture:** Add local `isDragging` state to the slider section, wrap the existing `Slider` in a `BoxWithConstraints` to measure width, calculate the thumb X position mathematically, and overlay an `AnimatedVisibility`-wrapped `StrokePreviewBubble` composable positioned above the thumb. No ViewModel or domain changes.

**Tech Stack:** Jetpack Compose, Material 3 (`Surface`, `Slider`, `AnimatedVisibility`), `BoxWithConstraints`, `LocalDensity`

---

## File Map

| File | Action |
|------|--------|
| `app/src/main/java/com/hotmail/arehmananis/sketchapp/presentation/feature/drawing/DrawingToolbar.kt` | Add `StrokePreviewBubble` private composable; replace bare `Slider` with `BoxWithConstraints` wrapper |
| `app/src/androidTest/java/com/hotmail/arehmananis/sketchapp/presentation/feature/drawing/DrawingToolbarStrokePreviewTest.kt` | New instrumented test file |

---

## Task 1: Write failing instrumented tests

**Files:**
- Create: `app/src/androidTest/java/com/hotmail/arehmananis/sketchapp/presentation/feature/drawing/DrawingToolbarStrokePreviewTest.kt`

- [ ] **Step 1: Create the test file**

```kotlin
package com.hotmail.arehmananis.sketchapp.presentation.feature.drawing

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithText
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.hotmail.arehmananis.sketchapp.domain.model.BrushType
import com.hotmail.arehmananis.sketchapp.presentation.theme.SketchAppTheme
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class DrawingToolbarStrokePreviewTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun strokeSizeLabelAlwaysVisible() {
        composeTestRule.setContent {
            SketchAppTheme {
                DrawingToolbar(
                    currentBrush = BrushType.PEN,
                    currentColor = Color.Black,
                    strokeWidth = 20f,
                    onBrushChange = {},
                    onColorChange = {},
                    onStrokeWidthChange = {},
                    onShapeToolChange = {},
                    onFilledChange = {}
                )
            }
        }
        composeTestRule.onNodeWithText("20px").assertIsDisplayed()
    }

    @Test
    fun strokePreviewBubbleHiddenInitially() {
        composeTestRule.setContent {
            SketchAppTheme {
                DrawingToolbar(
                    currentBrush = BrushType.PEN,
                    currentColor = Color.Black,
                    strokeWidth = 20f,
                    onBrushChange = {},
                    onColorChange = {},
                    onStrokeWidthChange = {},
                    onShapeToolChange = {},
                    onFilledChange = {}
                )
            }
        }
        // Only the header label shows "20px" on initial render — bubble is hidden
        composeTestRule.onAllNodesWithText("20px").assertCountEquals(1)
    }
}
```

- [ ] **Step 2: Run to confirm tests fail (or pass trivially before implementation)**

```bash
./gradlew connectedDevDebugAndroidTest \
  --tests "com.hotmail.arehmananis.sketchapp.presentation.feature.drawing.DrawingToolbarStrokePreviewTest" \
  2>&1 | tail -20
```

Expected: `strokeSizeLabelAlwaysVisible` passes (label already exists); `strokePreviewBubbleHiddenInitially` may pass or fail depending on current state. Both must pass after implementation.

---

## Task 2: Add `StrokePreviewBubble` private composable

**Files:**
- Modify: `app/src/main/java/com/hotmail/arehmananis/sketchapp/presentation/feature/drawing/DrawingToolbar.kt`

- [ ] **Step 1: Add new imports at the top of `DrawingToolbar.kt`**

After the existing `import androidx.compose.animation.shrinkVertically` line, add:

```kotlin
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.ui.platform.LocalDensity
```

- [ ] **Step 2: Add `StrokePreviewBubble` private composable at the bottom of `DrawingToolbar.kt` (before the closing of the file, after `IconData` data class)**

```kotlin
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
```

- [ ] **Step 3: Build to verify no compilation errors**

```bash
./gradlew compileDevDebugKotlin 2>&1 | grep -E "error:|warning:" | head -20
```

Expected: no errors.

---

## Task 3: Wire `BoxWithConstraints` + `isDragging` into the slider section

**Files:**
- Modify: `app/src/main/java/com/hotmail/arehmananis/sketchapp/presentation/feature/drawing/DrawingToolbar.kt` (lines ~219–229)

- [ ] **Step 1: Replace the bare `Slider` (lines 219–229) with the `BoxWithConstraints` wrapper**

Replace this block:
```kotlin
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
```

With:
```kotlin
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

    AnimatedVisibility(
        visible = isDragging,
        enter = fadeIn(animationSpec = tween(150)),
        exit = fadeOut(animationSpec = tween(150)),
        modifier = Modifier
            .align(Alignment.TopStart)
            .offset(x = thumbOffsetXDp, y = (-72).dp)
    ) {
        StrokePreviewBubble(strokeWidth = strokeWidth, color = currentColor)
    }
}
```

- [ ] **Step 2: Build to verify no compilation errors**

```bash
./gradlew compileDevDebugKotlin 2>&1 | grep -E "error:" | head -20
```

Expected: no errors.

- [ ] **Step 3: Run instrumented tests**

```bash
./gradlew connectedDevDebugAndroidTest \
  --tests "com.hotmail.arehmananis.sketchapp.presentation.feature.drawing.DrawingToolbarStrokePreviewTest" \
  2>&1 | tail -20
```

Expected: both tests PASS.

- [ ] **Step 4: Manual verification on device/emulator**

Install the dev debug build and open the drawing screen:
```bash
./gradlew installDevDebug
```

1. Open drawing screen
2. Tap the stroke size slider and drag left/right
3. Confirm a bubble appears above the thumb showing a filled circle + "Xpx" label
4. Confirm the bubble is centered over the thumb throughout the drag
5. Confirm the bubble fades out when you lift your finger
6. Confirm the bubble does NOT appear before dragging or after releasing

- [ ] **Step 5: Commit**

```bash
git add \
  app/src/main/java/com/hotmail/arehmananis/sketchapp/presentation/feature/drawing/DrawingToolbar.kt \
  app/src/androidTest/java/com/hotmail/arehmananis/sketchapp/presentation/feature/drawing/DrawingToolbarStrokePreviewTest.kt
git commit -m "feat: show stroke size preview bubble while dragging slider"
```
