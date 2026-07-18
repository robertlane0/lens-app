# Networking

## Architecture

The app uses **Retrofit 2** over **OkHttp 3** as its primary HTTP stack, with a legacy `HttpURLConnection`-based client for specific operations.

```mermaid
flowchart TD
    A[App Code] --> B{Cloud Connector?}
    B -->|Yes| C[CloudConnectorHelper]
    B -->|No| D[Service-specific Client]
    C --> E[RetrofitFactory / VroomProvider]
    D --> E
    E --> F[OkHttp Client]
    F --> G[Interceptors]
    G --> H[Auth (Bearer/t)]
    G --> I[Certificate Pinning]
    G --> J[Logging]
    G --> K[Attribution Headers]
    H --> L[Network]
    I --> L
    J --> L
    K --> L
```

## Base URLs

| Service | Production URL | Debug/PPE URL |
|---------|---------------|----------------|
| **OneDrive API** | `https://api.onedrive.com/v1.0` | `https://skyapi.live-tst.net` |
| **Microsoft Graph** | `https://graph.microsoft.com/v1.0/me` | `https://graph.microsoft-ppe.com` |
| **ImageToDoc** | `https://imagetodoc.officeapps.live.com` | `https://imagetodoc.edog.officeapps.live.com` |
| **Office Apps Live** | `https://officeapps.live.com` | `https://odc.edog.officeapps.live.com` |
| **Nexus Telemetry** | `https://nexus.officeapps.live.com` | `https://nexus.edog.officeapps.live.com` |
| **Roaming Settings** | `https://roaming.officeapps.live.com/rs/RoamingSoapService.svc` | — |
| **Office Discovery** | `https://api.office.com/discovery/v1.0/me/services` | — |
| **OneNote API** | `https://www.onenote.com/api/v1.0/me/notes/pages` | — |
| **Config Service** | `https://office15client.microsoft.com` | — |
| **Token Share Config** | `https://oneclient.sfx.ms/mobile/ts_configuration.jwt` | — |

## Authentication Endpoints

| Environment | OAuth2 Authority |
|-------------|-----------------|
| Production | `https://login.microsoftonline.com/common` |
| PPE | `https://login.windows-ppe.net/common` |
| US Gov | `https://login.microsoftonline.us` |
| China (Gallatin) | `https://login.chinacloudapi.cn` |
| China (Mooncake) | `https://login.partner.microsoftonline.cn` |
| Germany | `https://login.microsoftonline.de` |
| Legacy | `https://login.windows.net/common` |

## Retrofit Service Interfaces

### MicrosoftGraphService
```
GET /v1.0/me?$select=mysite
GET /v1.0/organization?$select=displayName
GET /v1.0/subscribedSkus
GET /v1.0/drive/root?$select=webUrl
```

### OneDriveService
```
GET /API/2/GetStorageInfo?planDetails=true&provisionUserIfNeeded=true&quotaForDisabled=true
```

### OneDriveVroomService
```
GET  sites/root/consumerSiteAppConfigs/files
GET  drives/{owner-cid}/
POST drive/status/action.unlockDrive/
```

### OfficeAppsService
```
POST /odc/servicemanager/serviceadd?app=3&ver=15
GET  /odc/servicemanager/userconnected?app=3&ver=15
```

### Live OAuth2 Token Interface
```
POST oauth20_token.srf (authorization_code grant)
POST oauth20_token.srf (refresh_token grant)
POST oauth20_token.srf (assertion grant)
```

## HTTP Client Configuration

| Parameter | Value |
|-----------|-------|
| Connect timeout | 10,000 ms |
| Read timeout | 10,000 ms |
| Write timeout | 10,000 ms |
| Max requests/host | min(20, availableProcessors × 2) |
| Connection pool | 10 connections, 5 min keep-alive |
| Follow redirects | true |

## Interceptor Stack

| Interceptor | Position | Purpose |
|-------------|----------|---------|
| `HttpLoggingInterceptor` | Application | Debug network logging |
| `CertificatePinningInterceptor` | Network | SSL pinning via Intune |
| Auth interceptor | Network | Authorization headers |
| `OkHttp3AttributionInterceptor` | Network | Telemetry attribution |
| `OkHttp3TagInterceptor` | Network | Request tagging |
| `TokenRequestInterceptor` | Network | Token injection |
| `OfficeAppsRequestInterceptor` | Application | X-Office-* headers |
| `SPOfficeAppsRequestInterceptor` | Application | SharePoint headers |

## Upload Pipeline

### ImageToDoc Service (OCR/Conversion)
```
POST /i2dsvc/api/v1/upload (multipart/form-data)
GET  /i2dsvc/api/v1/status/{processId} (polling)
```

### OneDrive Upload
```
POST /drive/items/{itemId}/createUploadSession
PUT  {uploadUrl} with Content-Range
```

### OneNote Upload
```
POST /api/v1.0/me/notes/pages (multipart/form-data)
```

### Share Link Creation
```
POST /v1.0/me/drive/items/{id}/createLink
POST /v1.0/drive/items/{id}/oneDrive.createLink
```

## Cloud Connector Architecture

```
CloudConnectorHelper
  └─ CloudConnectorComponent
       └─ CloudConnectManager
            ├─ AnalyseContentHelper (ImageToDoc)
            ├─ BusinessCardHelper
            ├─ OneDriveUploadHelper
            ├─ OneNoteImageUploadHelper
            ├─ SendFeedbackForLearningHelper
            └─ LensCloudConnectHelper (orchestration/recovery)
```

## Network Security Configuration

```xml
<network-security-config>
    <base-config cleartextTrafficPermitted="true"/>
    <debug-overrides>
        <trust-anchors>
            <certificates src="user"/>
        </trust-anchors>
    </debug-overrides>
</network-security-config>
```

- Cleartext HTTP is **permitted globally**
- Debug builds trust **user-installed certificates** (allows proxy interception)

## Telemetry Endpoints

- **HockeyApp**: `https://rink.hockeyapp.net/api/2/apps/{appId}/app_users/check`
- **Nexus**: `https://nexus.officeapps.live.com` (prod) / `.edog.` (debug)
- **MATS allowed resources**: `graph.microsoft.com`, `officeapps.live.com`, `augloop.office.com`, `substrate.office.com` and others

## Certificate Pinning

- **Only active for Business (AAD) accounts**
- Controlled by ramp flag `EnableSslPinning`
- Delegates to `MAMCertificatePinningManager.validatePins()`
- Uses certificate hash comparison against known pin sets per cloud
- Blocking: throws `IOException` on pin failure
