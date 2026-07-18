# Project Findings & Design Decisions

## Build Information

| Property | Value |
|----------|-------|
| Android Gradle Plugin | 8.7.3 |
| Kotlin | 2.1.0 |
| KSP | 2.1.0-1.0.29 |
| Gradle | 8.11.1 |
| Minification | ProGuard/R8 enabled in release |
| Debuggable | Yes (debug builds) |
| JVM Target | 17 |

## Key Design Decisions

### OpenCV Integration

The original implementation plan called for OpenCV for contour-based document detection and perspective correction. Current state:

- **Perspective correction** uses Android's built-in `Matrix.setPolyToPoly()` + `Bitmap.createBitmap()`, which handles 4-point perspective transforms natively. This did not need OpenCV and was kept as-is.
- **Document border detection** now uses OpenCV (`org.opencv:opencv`, published to Maven Central) for contour-based largest-rectangle detection (see `image/DocumentDetector.kt`): grayscale → blur → Canny edges → dilate → find contours → largest convex 4-point contour above an area threshold. When a confident quadrilateral is found, the Crop screen automatically runs the perspective warp; otherwise it falls back to a centered inset rectangle for manual corner adjustment.
- OpenCV native libraries are loaded once at startup via `OpenCvSupport.init()` (called from `OpenScanApp.onCreate()`), using `OpenCVLoader.initLocal()` — fully local/offline, no OpenCV Manager or network dependency.
- The `org.opencv:opencv` AAR adds noticeable APK size (bundles native `.so` files for multiple ABIs). This is a deliberate trade-off for reliable edge detection; ABI splitting or a slimmed custom build are options if size becomes a concern.

### Why Not Auto-Detect in the Live Camera Preview

Auto-detection currently runs on the captured still image in the Crop screen, not on the live `PreviewView` frame stream:
- Running the same OpenCV pipeline per camera frame would need a `CameraX ImageAnalysis` use case and careful throttling/threading to avoid janking the preview — a larger change than detecting on the single captured photo.
- Detecting once on the full-resolution capture (rather than on downscaled, motion-blurred preview frames) gives more reliable corners.
- Live-preview auto-detect remains a reasonable future enhancement built on top of the same `DocumentDetector`.

### State Machine vs NavController

The `LensStateMachine` sealed class hierarchy was designed early as the architectural workflow blueprint. However, ViewModels navigate imperatively through callbacks passed to composables, and the `NavController` handles all screen transitions directly. The state machine serves primarily as documentation of the intended workflow states rather than as a runtime state driver.

### No DataStore

Room handles all structured data (documents, pages). There are no user preferences that require DataStore or SharedPreferences beyond what Compose/AndroidX provides internally.

### No Background Work

All processing (image filtering, PDF generation, OCR) completes within a few hundred milliseconds on modern devices and runs on coroutine dispatchers (`Dispatchers.Default`/`Dispatchers.IO`). WorkManager is not needed.

## Source Code Metrics

| Metric | Value |
|--------|-------|
| Total Kotlin files | 35 |
| Total lines of code | ~3,500 |
| Largest file | `CropScreen.kt` (437 lines) |
| Most complex file | `CropViewModel.kt` (243 lines) |
| Package structure depth | 6 levels from root |

## Dependencies Summary

From `gradle/libs.versions.toml`:

| Category | Libraries |
|----------|-----------|
| UI | Compose BOM 2024.12.01, Material 3, Navigation 2.8.5, Activity Compose 1.9.3 |
| Camera | CameraX 1.4.1 (core, lifecycle, view) |
| DI | Hilt 2.53.1 (Android + Compose navigation) |
| Database | Room 2.6.1 (runtime, KTX, compiler via KSP) |
| ML | ML Kit Text Recognition 16.0.1, Barcode Scanning 17.3.0 |
| Images | Coil 2.7.0 |
| Lifecycle | LiveData KTX, ViewModel Compose, Runtime Compose |
| Coroutines | Kotlinx Coroutines 1.9.0 (core + Android) |
| Core | Core KTX 1.15.0, SplashScreen 1.0.1 |

## What Was Removed from Original Lens

| Feature | Reason |
|---------|--------|
| Cloud sync (OneDrive/SharePoint/OneNote) | No cloud features |
| Account management / sign-in | No identity required |
| Intune MAM / enterprise MDM | Consumer app |
| Telemetry (Aria, EVT, Nexus, HockeyApp, App Center) | Privacy by design |
| Crash reporting | No data leaves device |
| License verification / DRM | Fully FOSS |
| Cross-app SSO / token sharing | Requires Microsoft ecosystem |
| Business card recognition | Requires cloud entity extraction |
| ONNX Runtime + TFLite models | Replaced by manual crop + Android SDK image processing |
| 31 native .so files | 64 MB of ARM-only native code, all unnecessary |
| Video capture | Out of scope |
| Ink annotation / text stickers | Not core to scanning |
| Immersive Reader / TTS | Android's built-in Select-to-Speak covers this |
| Copilot / Bing Chat AI | Cloud-dependent Microsoft service |
| DOCX / PPTX / OneNote export | Microsoft proprietary formats |
| Foldable / Wear OS layouts | Not needed for initial release |
| 337 XML layout files | Replaced by ~12 Compose files |
| 626 drawable resources | Replaced by Material 3 theming + icons |
| 110+ locale strings | English only (can be extended via community PRs) |

## Offline Capability

The app is fully offline by design. All features work without any network connectivity:

- Camera capture
- Image processing (rotate, filter, crop)
- OCR (ML Kit on-device)
- Barcode scanning (ML Kit on-device)
- PDF/image export
- Gallery browsing
- Settings
