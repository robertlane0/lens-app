# Networking

## No Networking

OpenScan is a **fully offline** application. No network permissions are declared in the manifest, and no networking libraries (OkHttp, Retrofit, etc.) are included as dependencies.

## Rationale

All document scanning, image processing, OCR, and barcode scanning happens on-device. There are no cloud services, no accounts, no telemetry, and no remote APIs.

## What the App Does NOT Do

| Feature | Reason |
|---------|--------|
| Cloud sync (OneDrive, SharePoint, Google Drive) | Out of scope — local-only |
| OCR via cloud API | ML Kit runs on-device |
| Crash reporting (Firebase, App Center, Sentry) | No telemetry — privacy by design |
| Analytics / telemetry | No data leaves the device |
| License verification | Fully FOSS — no DRM |
| Update checking | Handled by Google Play Store |
| Deep-link / external URL loading | Not required for document scanning |

## Network Permissions

The manifest declares **no** network permissions:

- No `INTERNET`
- No `ACCESS_NETWORK_STATE`
- No `ACCESS_WIFI_STATE`
