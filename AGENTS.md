# OpenScan — AGENTS.md

Open-source document scanner for Android.

- **Package:** `com.openscan.app`
- **Language:** Kotlin
- **UI:** Jetpack Compose
- **Architecture:** MVVM + state machine
- **Build:** Gradle (Kotlin DSL)
- **DI:** Hilt
- **Database:** Room
- **Module layout:** Single-module Android app (`app/`)

---

## Build

Build the entire project:

```bash
./gradlew assembleDebug
```

Build only the app module:

```bash
./gradlew :app:assembleDebug
```

On build failure, clean and retry:
```bash
./gradlew clean && ./gradlew (assembleDebug or :app:assembleDebug)
```

---

## Architecture

### UI

- Single `MainActivity`
- Compose `NavHost` defined in `navigation/NavGraph.kt`
- Screens are implemented using Jetpack Compose
- Keep composables focused on presentation; business logic belongs in ViewModels or the state machine.

### Application Flow

The scanner workflow is driven by:

```
scanner/LensStateMachine.kt
```

It manages the capture → review → edit → export lifecycle.

Unless a task explicitly requires it, preserve this flow rather than introducing parallel navigation or state handling.

### Dependency Injection

- `@HiltAndroidApp` in `OpenScanApp.kt`
- Modules located under `di/`
- Reuse existing Hilt modules before introducing new dependency injection patterns.

### Data Layer

Room v2 database located under:

```
data/db/
```

Contains:

- `Document`
- `Page`
- `DocumentDao`
- `PageDao`

Data access should go through `DocumentRepository` rather than directly from UI code.

---

## Key Dependencies

Managed through `gradle/libs.versions.toml`.

| Area | Library |
|------|---------|
| Camera | CameraX 1.4.1 |
| ML | ML Kit Text Recognition, Barcode Scanning |
| Database | Room 2.6.1 |
| Dependency Injection | Hilt 2.53.1 |
| Navigation | Compose Navigation 2.8.5 |
| Images | Coil 2.7.0 |
| PDF Export | `android.graphics.pdf.PdfDocument` |
| Image Processing | Android `Bitmap`, `Matrix`, `ColorMatrix` |

Room and Hilt use **KSP** for annotation processing.

---

## SDK & Toolchain

- minSdk: **24**
- targetSdk: **35**
- compileSdk: **35**
- Kotlin: **2.1.0**
- Gradle: **8.11.1**
- Android Gradle Plugin: **8.7.3**
- JDK: **17**

Release builds enable:

- code shrinking
- resource shrinking
- ProGuard (`app/proguard-rules.pro`)

---

## Permissions

Declared in `AndroidManifest.xml`:

- `CAMERA`
- `READ_MEDIA_IMAGES` (API 33+)
- `READ_EXTERNAL_STORAGE` (API ≤32)
- `WRITE_EXTERNAL_STORAGE` (API ≤28)

---

## Project Conventions

### General

- Prefer Kotlin for all new code.
- Follow official Kotlin style (`gradle.properties`).
- Prefer immutable (`val`) values whenever practical.
- Keep functions small and single-purpose.
- Use descriptive names instead of abbreviations.
- Avoid unrelated refactoring while implementing requested changes.
- Do not introduce new libraries unless they provide clear value.

### Compose

- UI belongs in composables.
- Business logic belongs in ViewModels or the state machine.
- Expose immutable UI state where possible.
- Reuse existing components before creating new ones.

### Navigation

Routes are defined in:

```
navigation/NavGraph.kt
```

Current destinations include:

- HOME
- CAPTURE
- REVIEW
- EDIT
- CROP
- GALLERY

Prefer extending existing navigation flows instead of creating duplicate routes.

---

## Current Project State

- No unit or instrumentation tests currently exist.
- No CI/CD workflows are configured.
- No dedicated lint/formatting configuration beyond official Kotlin style.
- Themes are defined in `res/values/themes.xml`.
- SplashScreen is used for application launch.

---

## When Making Changes

- Keep changes narrowly scoped to the requested task.
- Preserve the existing architecture unless the task explicitly requires restructuring.
- Reuse existing ViewModels, repositories, Hilt modules, and navigation patterns.
- Avoid bypassing `DocumentRepository` or `LensStateMachine`.
- Maintain backward compatibility unless a breaking change is intentional.

---

## Validation

Before submitting changes:

1. Run:

   ```bash
   ./gradlew assembleDebug
   ```

2. Verify modified screens compile and navigate correctly.

3. If changing Room models, verify database behavior and migrations if applicable.

4. If modifying camera, scanning, or image processing logic, test on a physical device when possible.
