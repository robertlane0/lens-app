# Security

## Authentication

### Supported Account Types

1. **Personal account** — Microsoft Account (MSA), Live ID
2. **Business account** — Azure AD (work/school), Office 365 tenant
3. **On-premises** — Business On Premise (SharePoint/Exchange)

### Auth Stack

```
TokenSharingService (cross-app SSO)
  ├── MSAL (Microsoft Authentication Library)
  ├── ADAL (Azure AD Authentication Library)
  ├── OneAuth (unified auth)
  └── Live OAuth2 (legacy MSA)
```

### Authentication Flow

```
┌─────────┐     ┌──────────┐     ┌──────────┐     ┌──────────┐
│   App   │     │  Broker  │     │   MSAL   │     │   AAD    │
│         │────>│ (if av.) │────>│          │────>│  Server  │
│         │<────│          │<────│          │<────│          │
└─────────┘     └──────────┘     └──────────┘     └──────────┘
     │                                              │
     │              ┌──────────┐                    │
     │              │   Live   │                    │
     │              │  OAuth2  │                    │
     └─────────────>│  (MSA)   │<───────────────────┘
                    └──────────┘
```

### Token Sharing

The app implements a cross-app SSO system via `TokenSharingService`:
- AIDL-based IPC (`ITokenProvider.Stub`)
- Versioned response filters (V3 → V2 → V1)
- Signature verification via `RemoteTokenShareConfiguration` (JWT-based)
- Supports 57+ Microsoft applications
- Token sharing config fetched from `https://oneclient.sfx.ms/mobile/ts_configuration.jwt`
- Configuration validated via embedded RSA public key

## Encryption

### Key Management

| Component | Mechanism |
|-----------|-----------|
| Master key | Android KeyStore (AES-256 GCM) |
| ADAL cache | PBE SHA256, 100 iterations |
| Settings | EncryptedSharedPreferences (AES256-SIV/AES256-GCM) |
| Legacy crypto | CryptoUtils with AES/CBC/PKCS5Padding |
| Default master key | Hardcoded Base64: `E1tby7beW7Q0o1jBPOjOmMMJhJjpuBJOEPrQjhiqx5c=` |

### ADAL Token Cache

- Encrypted with PBE-derived key
- Key derivation: SHA-256, 100 iterations
- Salt: `"com.microsoft.office.onenote"`
- Key length: 256 bits

### Document Encryption

`libofficecrypto.so` (JNI) handles Office document encryption/decryption.

## Network Security

### Certificate Pinning

- **Active for**: Business (AAD) accounts only
- **Controlled by**: Ramp flag `EnableSslPinning`
- **Implementation**: `CertificatePinningInterceptor` (OkHttp Interceptor)
- **Backend**: `MAMCertificatePinningManager.validatePins()` (Intune SDK)
- **Per-cloud pin sets**: WorldwideCerts, ArlingtonCerts, GallatinCerts
- **Failure action**: Block connection with `IOException("Cert Pinning Validation Failed")`

### SSL/TLS

- Custom `TlsSniSocketFactory` for SNI support
- `SSLContext` configured with per-identity `TrustManager[]`
- Fallback to `SSLContext.getDefault()` when no protocol specified
- Network security config permits cleartext traffic globally

### Hostname Verification

Available verifiers:
- `StrictHostnameVerifier` (production)
- `BrowserCompatHostnameVerifier`
- `AllowAllHostnameVerifier` (present but likely unused in production)
- `DefaultHostnameVerifier`

## Anti-Tampering & Anti-Debugging

### Root Detection

```java
String[] rootPaths = {
    "/system/app/Superuser.apk",
    "/sbin/su",
    "/system/bin/su",
    "/system/bin/failsafe/su",
    "/system/xbin/su",
    "/system/sd/xbin/su",
    "/data/local/xbin/su",
    "/data/local/bin/su",
    "/data/local/su"
};
```

The `DeviceAndApplicationInfo` class checks for common `su` binary paths and the Superuser APK.

### Debugger Detection

`HttpWebRequest.java` calls `Debug.isDebuggerConnected()` during HTTP request execution.

### Obfuscation

- **ProGuard / R8**: Confirmed by code comments about type token preservation
- **Resource obfuscation**: Library name `"0_resource_name_obfuscated"` observed
- **No string encryption**: Hardcoded URLs, keys, and credentials visible in decompiled code

## Intune MAM (Mobile App Management)

The entire application is Intune-MAM-wrapped:

| Component | Intune Equivalent |
|-----------|------------------|
| Application | `MAMApplication` |
| Activities | `MAMActivity` |
| Fragments | `MAMFragment` |
| Dialogs | `MAMDialogFragment` |
| WebView | `MAMWebView` |
| EditText | `MAMEditText` |

### Policy Enforcement Points

- **Certificate pinning**: `MAMCertificatePinningManager`
- **Data loss prevention**: `MAMWebView`, `MAMEditText` for copy/paste control
- **Print management**: `MAMPrintManagement`
- **Package queries**: `MAMPackageManagement`
- **Compliance blocks**: `OfflineBlockedActivityBase` calls `MAMApplication.endProcess()`
- **Identity**: Per-session MAM identity mapping
- **Network**: `HVCIntunePolicy.getManagedSocketFactory()` for managed SSL sockets

### Intune Configuration

| Metadata | Value |
|----------|-------|
| AAD Client ID | `d3590ed6-52b3-4102-aeff-aad2292ab01c` |
| AAD Authority | `https://login.windows.net/common/` |
| Multi-identity | Enabled |
| Skip broker | Disabled |
| Force production | Enabled |

### App Restrictions

Referenced from `@xml/app_restrictions_config`:
- `AllowedAccountUPNs` — restrict to specific users
- `NotesCreationEnabled` — OneNote creation
- `TeamsApps.IsAllowed` — Teams integration
- `BingChatEnterprise.IsAllowed` — Copilot access

## License Verification

### Google Play LVL

- `LicenseChecker` with 10-second timeout
- Verifies against `ILicensingService` on `com.android.vending`
- RSA public key signature verification
- Response codes: LICENSED (256), NOT_LICENSED (561), ERROR_RETRY (291)

### Microsoft Licensing

- `LicensingManager` with JNI (`NativeProxy`)
- Operations: `CanPerformPremiumEdit()`, `CheckAndActivateSubscriptionLicense()`, `GetApplicationLicense()`, `DeleteLicense()`
- Premium feature gating via `DeviceConfig`

## YubiKey / FIDO2

- YubiKey SDK (OtpActivity, YubiKeyPromptActivity)
- FIDO2/WebAuthn via Google Play Services
- `PublicKeyCredentialCreationOptions`, `PublicKeyCredentialRequestOptions`

## Findings & Observations

### Hardcoded Secrets

- **Default master key**: `E1tby7beW7Q0o1jBPOjOmMMJhJjpuBJOEPrQjhiqx5c=` in `CryptoCore.java`
- **Client ID**: `d3590ed6-52b3-4102-aeff-aad2292ab01c`
- Multiple API URLs and endpoints hardcoded in `Constants.java` classes

### Weaknesses

1. **PBE with only 100 iterations** — weak by modern standards (OWASP recommends 600,000+ for PBKDF2)
2. **`AllowAllHostnameVerifier`** present in codebase (though likely production-unused)
3. **`jwt none` algorithm** supported in token sharing JWT library — could accept unsigned tokens
4. **Cleartext traffic permitted globally** — no HTTPS-only enforcement in network config
5. **Debug builds trust user certificates** — allows man-in-the-middle via proxy
6. **No certificate pinning for consumer accounts** — only enforced for business accounts
7. **Hardcoded default master key** in CryptoCore — defeats encryption if device is compromised

### Mitigations

- Intune MAM provides strong enterprise-grade DLP controls
- Android KeyStore integration for master key storage
- Token sharing uses RS256-signed JWT configuration with certificate chain validation
- Certificate pinning for business accounts via industry-standard Intune SDK
