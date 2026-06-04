# Image Cropper Feature Design

**Date:** 2026-06-04  
**Status:** Approved

## Overview

Add a fullscreen custom Compose crop screen that appears between gallery image selection and canvas placement. The user drags corner/edge handles to define a free-form crop region, then confirms to place the cropped image centered on the drawing canvas.

---

## User Flow

1. User taps "Import Image" in the drawing toolbar.
2. Gallery picker opens (existing `PickVisualMedia` launcher in `DrawingScreen`).
3. On image selected → navigate to `ImageCropScreen`, passing the image URI as a nav argument.
4. User adjusts the crop rectangle using 8 drag handles (4 corners + 4 edge midpoints). No aspect ratio lock — free-form only.
5. Tapping **Done** → crop is applied, screen pops, cropped image is placed centered on canvas.
6. Tapping **Cancel** → screen pops, no image added.

---

## Architecture

### New Files

**`presentation/feature/drawing/ImageCropScreen.kt`**
- Fullscreen Compose screen.
- Displays the full image scaled to fit the screen using a `Canvas` composable.
- Renders a semi-transparent dark scrim outside the crop rect and a clear rect inside it.
- 8 drag handles (4 corners + 4 edge midpoints) detected via `detectDragGestures`.
- Top app bar with **Cancel** (left) and **Done** (right) actions.
- Collects UI state from `ImageCropViewModel`; shows a `Snackbar` on error.

**`presentation/feature/drawing/ImageCropViewModel.kt`**
- Holds crop state as `MutableStateFlow<CropRect>` where each value is a fraction (0–1) of the displayed image dimensions. Keeps logic coordinate-system agnostic.
- `onHandleDrag(handle, delta)` — updates the crop rect, enforcing minimum 50×50px crop.
- `confirmCrop(uri, context)` — runs on `Dispatchers.IO`:
  1. Decodes the original bitmap from URI.
  2. Converts fractional crop rect to pixel coordinates.
  3. Calls `Bitmap.createBitmap(source, x, y, w, h)`.
  4. Writes result to `context.filesDir/images/<uuid>.jpg`.
  5. Emits the output file path via a `SharedFlow<String>` event.
- On IO failure, emits an error string to a `SharedFlow<String>` for snackbar display.

### Modified Files

**`presentation/common/Navigation.kt`**
- Add `Screen.ImageCrop(uri: String)` route.

**`presentation/feature/drawing/DrawingScreen.kt`**
- Change the `imagePickerLauncher` callback: instead of calling `viewModel.onImagePicked()` directly, navigate to `Screen.ImageCrop(uri.toString())`.
- Add a `NavBackStackEntry` result observer: when `ImageCropScreen` pops back with a file path, call `viewModel.addImage(path, canvasWidth / 2f, canvasHeight / 2f, w, h)` where `w` and `h` come from the cropped bitmap's dimensions (passed alongside the path).

**`presentation/feature/drawing/DrawingViewModel.kt`**
- `onImagePicked()` is no longer called from the gallery flow; it can be kept for any future direct-URI use or removed. `addImage()` is used directly from the crop result.

### Unchanged

`ImageElement`, `DrawingCanvas`, `ImageOverlay`, undo/redo stack, `BitmapExporter`, `ShareHelper` — the cropped image is a local file identical in structure to any other imported image.

---

## Data Flow

```
Gallery URI
    └─► ImageCropScreen (nav arg)
            └─► ImageCropViewModel.confirmCrop()
                    ├─ Decode bitmap (IO dispatcher)
                    ├─ Apply fractional crop rect → pixel rect
                    ├─ Bitmap.createBitmap()
                    ├─ Write to filesDir/images/<uuid>.jpg
                    └─► SharedFlow emits (filePath, width, height)
                            └─► DrawingScreen receives result
                                    └─► DrawingViewModel.addImage(path, centerX, centerY, w, h)
                                            └─► ImageElement placed on canvas, undo entry recorded
```

---

## Error Handling

- Decoding or file-write failure → snackbar on `ImageCropScreen`, user stays on screen to retry or cancel.
- Minimum crop size: handles cannot produce a rect smaller than 50×50px (matches existing canvas minimum image size in `DrawingViewModel.resizeImage`).
- Cancel always pops without side effects — no file is written, no image is added.

---

## Testing

### Unit Tests (`ImageCropViewModelTest`)
- Crop rect clamps to image bounds when dragging a handle past the edge.
- Minimum crop size (50×50px) is enforced — handle drag stops at the limit.
- `confirmCrop` output bitmap dimensions match the fractional crop rect applied to the source dimensions.

### UI/Instrumented Tests (`ImageCropScreenTest`)
- Cancel dismisses the screen; `DrawingViewModel.imageElements` remains unchanged.
- Done with a valid crop navigates back and `DrawingViewModel.imageElements` contains one new element centered on the canvas.

---

## Out of Scope

- Aspect ratio lock (free-form only per requirement).
- Rotation within the cropper (images can be rotated after placement via existing pinch gesture).
- Multi-image crop in one session.
