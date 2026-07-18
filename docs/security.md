# Security

## No Authentication

OpenScan has no user accounts, no sign-in, and no authentication system. No credential storage, no token management, no OAuth flows.

## No Encryption

OpenScan does not encrypt data at rest or in transit. All data is stored as plain files and unencrypted Room database entries.

**Rationale**: The app processes user-generated document scans that the user explicitly chooses to capture, edit, and export. There are no credentials, tokens, or sensitive metadata that would benefit from encryption beyond what the Android filesystem provides (sandboxed app storage).

## No Telemetry

OpenScan includes zero telemetry, analytics, or crash reporting libraries. No data leaves the device.

**Rationale**: Privacy by design — users should not need to opt out of data collection when using a document scanner.

## No Network Security Configuration

The app does not define a `network_security_config.xml` because it makes no network requests. No cleartext traffic concerns, no certificate pinning, no SSL configuration.

## No Anti-Tampering

OpenScan has no root detection, debugger detection, or obfuscation. The ProGuard rules file only contains Room entity keep rules:

```
-keep class com.openscan.app.data.db.** { *; }
```

**Rationale**: As a fully open-source application, there is no need to protect proprietary logic or license enforcement.

## No Enterprise Features

| Feature | Status |
|---------|--------|
| Intune MAM | Not integrated |
| Certificate pinning | Not implemented |
| Data loss prevention (DLP) | Not implemented |
| Device compliance checks | Not implemented |
| Remote wipe | Not applicable |

## Permissions

The app requests only the minimum permissions required for its function:

| Permission | Purpose | Rationale |
|------------|---------|-----------|
| `CAMERA` | Capture documents | Core feature — cannot scan without camera |
| `READ_MEDIA_IMAGES` | Import images (API 33+) | User convenience for importing existing photos |
| `READ_EXTERNAL_STORAGE` | Import images (API ≤32) | Legacy equivalent |
| `WRITE_EXTERNAL_STORAGE` | Save exports (API ≤28) | Legacy — modern APIs use MediaStore or FileProvider |

## Privacy

- **All processing is on-device**: Camera frames, images, OCR text, and barcode data never leave the device
- **No data collection**: No analytics, crash reports, or usage statistics
- **No third-party SDKs with network access**: ML Kit runs on-device; no Firebase or Google Play Services telemetry is linked
- **User controls**: Documents can be deleted individually or via app data clear in system settings
