# Microsoft Office Lens — Application Overview

## General Information

| Property | Value |
|----------|-------|
| **Package** | `com.microsoft.office.officelens` |
| **App Name** | Microsoft Lens |
| **Version** | 16.0.18526.20136 |
| **Version Code** | 2004466765 |
| **Min SDK** | 24 (Android 7.0 Nougat) |
| **Target SDK** | 34 (Android 14) |
| **Build** | Android Gradle Plugin 8.5.1, Kotlin Multiplatform |
| **Signing** | Microsoft (MSFTSIG certificate) |
| **APK Size** | 62 MB (compressed) |

## Description

Microsoft Lens is a document scanning and image capture application developed by Microsoft. It uses the device camera to capture documents, whiteboards, business cards, QR codes, and photos, then applies intelligent image processing to enhance, crop, and extract content. It integrates with OneDrive, SharePoint, OneNote, and the broader Microsoft 365 ecosystem for cloud storage and sharing.

## Key Features

- **Capture modes**: Document, Photo, Whiteboard, Business Card, QR/Barcode, Video, Text, Table, Auto-detect
- **Immersive Reader**: Extracted text can be read aloud with Immersive Reader integration
- **Copilot integration**: Microsoft Copilot (Bing Chat Enterprise) integration for AI-powered analysis
- **OCR**: Optical Character Recognition with ML Kit and custom ONNX models
- **Image processing**: Perspective correction, cropping, filtering, ink annotation
- **Cloud sync**: OneDrive, SharePoint, OneNote integration
- **Cross-app SSO**: Token sharing with 57+ Microsoft applications
- **Enterprise readiness**: Microsoft Intune MAM (Mobile App Management), certificate pinning, data loss prevention

## Technology Stack

| Layer | Technology |
|-------|------------|
| **Language** | Kotlin + Java |
| **Architecture** | Component-based with ViewModel + Command pattern |
| **Camera** | CameraX + Camera2 |
| **ML** | ONNX Runtime (2 models), TensorFlow Lite (1 model), Google ML Kit |
| **Networking** | OkHttp 3, Retrofit 2, Gson, Moshi |
| **Auth** | MSAL, ADAL, OneAuth, Token Sharing Service |
| **Storage** | Room, SQLite, SharedPreferences (Encrypted), File-based properties |
| **DI** | Manual ViewModelProviderFactory (no Dagger/Hilt) |
| **Background** | AndroidX WorkManager, JobScheduler |
| **Coroutines** | Kotlinx Coroutines |
| **Enterprise** | Microsoft Intune MAM SDK |
| **Crash Reporting** | HockeyApp, App Center |
| **Telemetry** | Microsoft Aria/EVT pipeline, Nexus |
| **PDF** | Apache PDFBox (tom_roush port) |
| **2FA** | YubiKey SDK, FIDO2/WebAuthn |
