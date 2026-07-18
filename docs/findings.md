# Findings & Observations

## Build Information

| Property | Value |
|----------|-------|
| Android Gradle Plugin | 8.5.1 |
| Kotlin Multiplatform | Confirmed (iOS arm64 targets in project metadata) |
| Minification | ProGuard/R8 enabled (resource name obfuscation observed) |
| Debuggable | No (production build) |
| Test instrumentation | `android.test.InstrumentationTestRunner` with target `com.microsoft.office.msohttp.tests` |

## Third-Party Dependencies

### Major SDKs

| SDK | Purpose |
|-----|---------|
| Google Play Services (auth, fido, mlkit, tflite) | Auth, ML, FIDO2 |
| Firebase (ML Kit, transport) | ML and telemetry |
| Microsoft Intune MAM SDK | Enterprise MDM |
| Microsoft Authentication Library (MSAL/ADAL) | Auth |
| Microsoft Token Sharing SDK | Cross-app SSO |
| Microsoft App Center / HockeyApp | Crash reporting |
| Microsoft Aria/EVT | Telemetry |
| Microsoft ODSP | OneDrive/SharePoint |
| OkHttp / Retrofit 2 / Moshi / Gson | Networking/serialization |
| AndroidX (Room, WorkManager, CameraX, DataStore, SecurityCrypto) | Various |
| Apache PDFBox (tom_roush port) | PDF generation |
| YubiKit (Yubico) | Hardware 2FA |
| Lottie (Airbnb) | Animations |
| Square LeakCanary | Memory leak detection |
| Nimbus JOSE+JWT | JWT handling |
| SpongyCastle | Crypto primitives |
| Bolts (Facebook) | Task/continuation patterns |
| OpenTelemetry | Distributed tracing |
| Kotlinx Coroutines | Async programming |

### ONNX Runtime Models

| Model | Size | Purpose |
|-------|------|---------|
| `mnv2_ep42_wb_quant.ort` | 342 KB | Quantized MobileNetV2 — image classification / document detection |
| `triclass_doc_classifier.ort` | 204 KB | Three-class document type classifier (whiteboard, document, photo/business card) |

### TensorFlow Lite Model

| Model | Size | Purpose |
|-------|------|---------|
| `clf_model.tflite` | 401 KB | Additional classifier (likely for text/label detection via ML Kit) |

### Native Libraries (31 .so files, ~64 MB)

All compiled for **armeabi-v7a (32-bit ARM)** only. No 64-bit or x86 variants.

| Library | Size | Purpose |
|---------|------|---------|
| `libOfficeLens.so` | 5.9 MB | Core Lens native module |
| `libmsoandroid.so` | 16.3 MB | Office shared platform |
| `libmso20android.so` | 11.0 MB | Office 2.0 component |
| `libmso30android.so` | 10.3 MB | Office 3.0 component |
| `libmso40uiandroid.so` | 4.9 MB | Office UI component |
| `libmso50android.so` | 2.3 MB | Office 5.0 component |
| `libmso98android.so` | 1.3 MB | Office 9.8 component |
| `libofficecrypto.so` | 1.0 MB | Document encryption |
| `libofficessl.so` | 936 KB | SSL/TLS networking |
| `libskiaoffice.so` | 1.4 MB | Skia graphics (Office fork) |
| `libtensorflowlite_jni_gms_client.so` | 46 KB | TFLite GMS bridge |
| `lib7zofficeassetdecoder.so` | 238 KB | 7z asset decompression |

## Obfuscation Observations

- **ProGuard/R8 applied**: Class and method names in smali appear obfuscated (`a`, `b`, `c`, etc.) in many packages
- **String obfuscation**: Some strings may be encrypted at rest but are decrypted at runtime. Hardcoded strings (URLs, keys) visible in decompiled Java.
- **Resource obfuscation**: Library entry `"0_resource_name_obfuscated"` observed
- **Package splitting**: Code split across 6 smali directories (classes.dex through classes6.dex) indicating large app with multidex
- **jadx decompilation errors**: 18 classes failed to decompile (likely obfuscated or containing unreachable control flow)

## Notable Findings

### 1. Architecture Quality
- Well-structured component system with clear separation of concerns
- ViewModel pattern used consistently with manual factories
- Comprehensive telemetry instrumentation
- Strong Intune MAM integration for enterprise scenarios

### 2. Security Concerns

| Issue | Severity | Detail |
|-------|----------|--------|
| Hardcoded master key | Medium | Default AES key in CryptoCore: `E1tby7beW7Q0o1jBPOjOmMMJhJjpuBJOEPrQjhiqx5c=` |
| Weak PBE iterations | Low | Only 100 iterations for ADAL cache key derivation |
| Cleartext traffic | Low | `cleartextTrafficPermitted="true"` globally |
| AllowAllHostnameVerifier | Low | Present in codebase (unlikely used in production) |
| None algorithm JWT | Low | `JWSAlgorithm.none` supported (token sharing library) |

### 3. Data Privacy
- Consent framework via Reykjavik privacy SDK
- GDPR-compliant consent flows
- Telemetry levels configurable (0=off, 1=required, 2=full)
- Privacy preferences roam across devices

### 4. Enterprise Readiness
- Full Intune MAM integration
- Certificate pinning for business accounts
- Multi-identity support
- Per-session MAM identity tracking
- Compliance blocking with process termination

### 5. Cross-App Ecosystem
- Token sharing with 57+ Microsoft applications
- Signature-verified cross-app authentication
- Shared Preference service for cross-app settings
- Deep Office 365 ecosystem integration

### 6. Machine Learning
- Hybrid ONNX Runtime + TensorFlow Lite approach
- On-device document classification (whiteboard vs document vs photo)
- ML Kit for text recognition and image labeling
- No cloud-based ML model execution detected

### 7. Offline Capability
- Capture and basic editing works offline
- Uploads queued and retried when connectivity restored
- License cached with offline grace period

### 8. Platform Support
- **32-bit ARM only**: No 64-bit native libraries
- **Foldable devices**: Dedicated support for Surface Duo
- **Wear OS**: Layout variants present
- **RTL**: Full right-to-left layout support
- **Dark mode**: Full night theme support

## Decompilation Success Rate

| Tool | Classes | Success | Failed |
|------|---------|---------|--------|
| apktool (baksmali) | 6 DEX files | 100% | 0 |
| jadx | 16,317 classes | 99.9% | 18 |

The 18 failed classes are likely ProGuard-obfuscated or use features jadx cannot handle.

## Screenshots

No screenshots are available as the analysis was performed on a headless system without an Android emulator. All UI information is inferred from layout XML files, string resources, and source code analysis.
