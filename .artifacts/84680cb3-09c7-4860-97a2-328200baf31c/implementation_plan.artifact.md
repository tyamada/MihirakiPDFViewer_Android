# Implementation Plan - Desktop Mouse Click Page Navigation & Edge Click Indicators

Implement mouse click page navigation on the left and right edges of the PDF view exclusively for Desktop devices (non-touchscreen devices), along with an updated opening tutorial animation indicating left/right edge page turning.

## User Review Required

> [!IMPORTANT]
> - **Desktop Detection**: We will determine Desktop devices by checking if the device lacks a touchscreen feature (`!packageManager.hasSystemFeature(PackageManager.FEATURE_TOUCHSCREEN)` or configuration touchscreen `TOUCHSCREEN_NOTOUCH`).
> - **Edge Click Zones (Desktop Only)**:
>   - Left Edge (outer 20%): Navigates to previous page (in L2R) or next page (in R2L).
>   - Right Edge (outer 20%): Navigates to next page (in L2R) or previous page (in R2L).
>   - Center Area (middle 60%): Toggles chrome (`onTap()`).
> - **Opening Animation**: The initial hint animation displayed upon opening a PDF will be updated to display visual indicators/animations on both the left and right edges, illustrating that clicking the edges turns pages.

## Proposed Changes

### Viewer Screen & UI Component

#### [MODIFY] [ViewerScreen.kt](file:///Users/tyamada22/src/MihirakiPDFViewer_Android/app/src/main/java/com/github/tyamada/mihirakipdfviewer_android/ui/screens/ViewerScreen.kt)
- Add desktop device detection (`isDesktop`).
- Update `ZoomablePage` (or add edge click handling in `ViewerScreen` / `ZoomablePage`) so that on Desktop devices (`isDesktop == true`), clicks on the left or right edge zones trigger `onPrevious()` / `onNext()` based on reading direction (`direction`).
- Update `showSwipeHint` opening animation to render left and right edge visual indicators (pulse/arrow animations) showing page turning via edge clicks.

## Verification Plan

### Automated Tests
- Run existing unit tests and instrumentation tests via Gradle.

### Manual Verification
- Verify that on non-touchscreen/desktop mode, clicking left/right edges turns pages correctly according to reading direction (L2R vs R2L).
- Verify that the opening tutorial animation correctly highlights the left and right edge page turning capability.
