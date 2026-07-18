# AGENTS.md

## Objective

Your task is to analyze the Android APK file located in this directory and produce comprehensive documentation describing its implementation, behavior, architecture, and user interface.

The final output should consist of one or more well-organized Markdown documents that are sufficiently detailed for another engineer to understand how the application works without performing the reverse engineering themselves.

---

# Primary Goals

Perform a thorough reverse engineering pass over the APK.

Your documentation should include, where applicable:

- Application overview
- Package structure
- Activities
- Fragments
- Services
- Broadcast receivers
- Content providers
- Permissions
- Manifest analysis
- Navigation flow
- Application lifecycle
- Data storage
- SharedPreferences
- Databases
- Network communication
- APIs
- Authentication
- Encryption
- Native libraries
- Assets
- Resources
- Strings
- Configuration files
- Dependency analysis
- Third-party SDKs
- Security mechanisms
- Background work
- Notifications
- Intent usage
- IPC mechanisms
- Build information
- Obfuscation observations
- Anti-tampering or anti-debugging features (if present)

Document all significant findings.

---

# UI Documentation

Produce a complete walkthrough of the application's UI.

For every screen you can identify:

- Screen name
- Purpose
- Layout hierarchy
- Controls
- Buttons
- Menus
- Navigation
- Dialogs
- User flows
- Inputs
- Outputs
- State transitions

Include inferred behavior when supported by code analysis, and clearly distinguish between observed behavior and inferred behavior.

---

# Architecture Documentation

Document:

- Overall architecture
- Major modules
- Component relationships
- Data flow
- Event flow
- Initialization sequence
- Startup behavior
- Long-running tasks
- Dependency injection (if present)
- Design patterns used

---

# Reverse Engineering

Use any appropriate reverse engineering tools, including but not limited to:

- jadx
- apktool
- aapt
- dex2jar
- CFR
- Bytecode viewers
- Android SDK tools
- jadx-gui (if useful)
- Other open-source Android reverse engineering utilities

Cross-reference results between tools when appropriate.

---

# Deliverables

Produce clear Markdown documentation.

Suggested organization:

```
docs/
    overview.md
    architecture.md
    ui.md
    manifest.md
    networking.md
    storage.md
    security.md
    findings.md
```

Use headings, code blocks, tables, and diagrams (Mermaid is encouraged) where they improve clarity.

---

# Working Practices

- Be systematic.
- Verify findings whenever possible.
- Clearly identify assumptions.
- Distinguish observed facts from inferred behavior.
- Prefer primary evidence from the APK over speculation.
- Keep notes as you progress.

---

# Environment

You do **not** have sudo access.

If you need additional tools, you may use **Nix** to install packages into your environment.

Prefer reproducible tooling through Nix instead of assuming packages are preinstalled.

---

# If Tools Fail

If you encounter a situation where:

- a required tool cannot be installed,
- Nix installation fails,
- a package is unavailable,
- a tool crashes repeatedly,
- or your environment prevents meaningful progress,

**Stop immediately** and ask the user to resolve the environment issue.

Do **not** spend excessive effort attempting alternative installation methods or workarounds once it becomes clear the environment is the blocker.

---

# Completion Criteria

The task is complete only when:

- the APK has been thoroughly analyzed,
- all major components have been documented,
- the UI has been comprehensively described,
- the application's architecture has been documented,
- significant implementation details have been explained,
- and the resulting Markdown documentation is organized, readable, and suitable as technical reference material.

---

# Phase 2: New Open-Source Implementation Plan

Using the reverse-engineering documentation in `docs/` as reference, produce a detailed implementation plan for a **new open-source document-scanning Android application** with the following constraints:

## Constraints

1. **No Microsoft code, libraries, SDKs, or trademarks** — the app must be built entirely from open-source components.
2. **No cloud features** — everything runs locally on-device. No sync, no cloud upload/download, no remote APIs.
3. **No accounts, authentication, or sign-in** — zero user identity required.
4. **Export formats only**: **PDF** and **image files** (PNG/JPEG). No Word, PPTX, OneNote, or any other format.
5. **No Intune, no MDM, no enterprise management** — the app is consumer-only.
6. **No telemetry, analytics, or crash reporting** — no data leaves the device.
7. **No license verification or DRM** — fully free and open source.

## Recommended Plan Structure

Write a new document `docs/implementation-plan.md` that covers:

### 1. Scope
- Feature set — exactly what the app does and doesn't do (be precise about exclusions).

### 2. Architecture
- Proposed architecture (component-based, MVVM, or similar).
- How to replace the Microsoft Lens component system with a simpler state-machine-based flow.

### 3. Technology Stack Recommendations
- Camera API: CameraX (already open-source).
- ML: OpenCV, Google ML Kit (on-device only), or TensorFlow Lite for document border detection and image enhancement.
- PDF export: Android `PdfDocument` API or iText / Apache PDFBox (open-source).
- Image processing: OpenCV, Android `Bitmap` APIs, or `RenderScript`.
- UI: Jetpack Compose or XML layouts.
- Storage: Room or plain file-based.
- Build: AGP + Kotlin + Gradle.
- Do **not** recommend: Retrofit, OkHttp, Gson, or any networking library (no network calls).

### 4. Feature Implementations
For each feature from the original Lens app that you plan to keep:
- Capture (camera with auto-detect)
- Image enhancement (auto-crop, perspective correction, filters)
- Crop/edit
- Reorder pages
- Export to PDF/images
- Gallery/recent items
- Barcode and QR scanning
- Immersive Reader

Describe how each would work **without** any of Microsoft's code, cloud services, or authentication.

For features you are **removing** (Business Cards, OneNote, Word export, Copilot, cloud sync, account management, telemetry, etc.), explicitly list them as out of scope.

### 5. Removed Features Checklist
An explicit list of every Lens feature to remove, with notes on what replaces it (or why it is deleted entirely).

### 6. UI Screens (from docs/ui.md)
Map each original screen to its replacement:
- Which screens remain (with modifications)?
- Which screens are deleted entirely?
- Which new screens are needed?

### 7. Data Model
- Document model, page model, image storage.
- No cloud-backed data structures. Everything local.

### 8. File Format
- The on-device file format for saved scans (PDF with embedded images).
- No proprietary `.document` or `.per` files.

### 9. Dependencies
- Complete list of open-source libraries to use.
- Which dependencies from the original app to drop.

### 10. Development Roadmap
- Suggested phases for building the app incrementally.

## Working Practices

- Base every decision on the reverse-engineering docs in `docs/`.
- Every plan entry should state whether it is **Kept**, **Modified**, or **Removed** relative to the original Lens app.
- Be explicit about scope boundaries — "out of scope" is better than vague.

## Deliverable

A single file `docs/implementation-plan.md` that another developer could follow to build the app from scratch.
