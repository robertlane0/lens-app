# OpenScan — Application Overview

## General Information

| Property | Value |
|----------|-------|
| **Package** | `com.openscan.app` |
| **App Name** | OpenScan |
| **Min SDK** | 24 (Android 7.0 Nougat) |
| **Target SDK** | 35 (Android 15) |
| **Compile SDK** | 35 |
| **Build** | AGP 8.7.3, Kotlin 2.1.0, Gradle 8.11.1 |
| **APK Size** | ~8 MB (estimated) |

## Description

OpenScan is an open-source document scanning application. It uses CameraX for capture, ML Kit for on-device OCR and barcode scanning, Room for local persistence, and Jetpack Compose for UI. All processing is on-device — no cloud services, no accounts, no telemetry.

## Key Features

- **Capture**: Multi-page document capture with CameraX
- **Image processing**: Perspective correction, crop, rotate, grayscale/document filters
- **OCR**: On-device text recognition via ML Kit
- **Barcode/QR scanning**: ML Kit Barcode Scanning (QR, EAN, UPC, Code 39/128, PDF417, Data Matrix, Aztec)
- **Export**: PDF (via `android.graphics.pdf.PdfDocument`) and PNG/JPEG images
- **Gallery**: Thumbnail grid of scanned documents with Room persistence
- **Page management**: Reorder, delete, and re-capture pages within a document
- **Settings**: Version info, license, privacy notice

## Technology Stack

| Layer | Technology |
|-------|------------|
| **Language** | Kotlin 2.1.0 |
| **Architecture** | MVVM + State Machine (sealed class workflow) |
| **UI** | Jetpack Compose + Material 3 |
| **Navigation** | Compose Navigation (NavHost) |
| **Camera** | CameraX 1.4.1 |
| **ML** | ML Kit Text Recognition, ML Kit Barcode Scanning |
| **Storage** | Room 2.6.1 + file system |
| **DI** | Hilt 2.53.1 |
| **Images** | Coil 2.7.0 |
| **PDF** | `android.graphics.pdf.PdfDocument` (built-in) |
| **Image Processing** | Android `Bitmap`, `Matrix`, `ColorMatrix` |
| **Coroutines** | Kotlinx Coroutines 1.9.0 |
