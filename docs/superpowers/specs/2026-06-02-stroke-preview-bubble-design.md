# Stroke Size Preview Bubble

**Date:** 2026-06-02
**Scope:** `DrawingToolbar.kt` only — no ViewModel or domain changes required

## Summary

When the user drags the stroke width slider, a floating bubble appears above the slider thumb showing a filled circle dot (sized proportionally to the stroke value) and the pixel label. The bubble disappears when the drag ends.

## Behavior

- Bubble appears as soon as dragging starts (first `onValueChange` fire)
- Bubble disappears when dragging stops (`onValueChangeFinished`)
- Bubble is centered horizontally over the slider thumb at all times during drag
- No bubble is shown on initial render or after drag ends

## State

A single local `Boolean` state `isDragging` is added inside the stroke width section of `DrawingToolbar`. It is:
- Set to `true` inside the slider's `onValueChange` lambda (before calling `onStrokeWidthChange`)
- Set to `false` inside the slider's `onValueChangeFinished` lambda

No state is added to `DrawingViewModel` or any other composable.

## Layout

Replace the bare `Slider` with a `BoxWithConstraints`. Inside it, two children are stacked:

1. The existing `Slider` (unchanged, full width)
2. A bubble overlay, absolutely positioned above the thumb

```
BoxWithConstraints {
    Slider(...)           // unchanged
    AnimatedVisibility(isDragging) {
        StrokePreviewBubble(...)  // positioned via offset
    }
}
```

## Thumb Position Calculation

```kotlin
val fraction = (strokeWidth - 1f) / (50f - 1f)   // 0f..1f
val thumbOffsetX = fraction * maxWidth.toPx()     // px from left edge
```

Material 3's `Slider` reserves ~10dp of horizontal padding for the thumb at each edge, so the effective track starts and ends 10dp inside the composable bounds. Adjust the position:
```kotlin
val thumbPaddingPx = 10.dp.toPx()
val trackWidth = maxWidth.toPx() - 2 * thumbPaddingPx
val thumbOffsetX = thumbPaddingPx + fraction * trackWidth
```

The bubble is then offset so its center aligns with `thumbOffsetX`. The y-offset is fixed at `-72.dp` (bubble content height ~56dp + 16dp gap) so Compose does not need to measure the bubble before placing it:
```kotlin
Modifier.offset(x = (thumbOffsetX - 32.dp.toPx()).toDp(), y = (-72).dp)
```

The bubble is placed above the slider so it does not obscure the track or thumb.

## Bubble Composable: `StrokePreviewBubble`

A private composable extracted for clarity.

**Parameters:** `strokeWidth: Float`, `color: Color`

**Visual structure:**
```
Surface (rounded, elevated)
  Column (centered)
    Box  ← filled circle, color = currentColor
         ← diameter = strokeWidth.dp clamped to 4.dp..36.dp
    Text ← "${strokeWidth.toInt()}px", labelSmall, primary color
```

**Dimensions:**
- Surface: fixed width 64.dp, auto height, `RoundedCornerShape(12.dp)`, `tonalElevation = 6.dp`
- Circle diameter: `strokeWidth.dp.coerceIn(4.dp, 36.dp)`
- Padding inside surface: 8.dp vertical, 8.dp horizontal

## Animation

`AnimatedVisibility` with `fadeIn(tween(150))` / `fadeOut(tween(150))` wraps the bubble. No slide — fade only keeps it unobtrusive.

## Files Changed

| File | Change |
|------|--------|
| `DrawingToolbar.kt` | Add `isDragging` state, wrap `Slider` in `BoxWithConstraints`, add `StrokePreviewBubble` private composable |

## Non-Goals

- No changes to `DrawingViewModel`, use cases, or domain models
- No persistence of preview preference
- No preview on the canvas itself
