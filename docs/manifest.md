# Android Manifest Analysis

## App Identity

```xml
package="com.openscan.app"
compileSdkVersion="35"
minSdkVersion="24"
targetSdkVersion="35"
```

## Permissions

| Permission | Purpose |
|------------|---------|
| `CAMERA` | Camera capture (required, core feature) |
| `READ_MEDIA_IMAGES` | Read gallery images (API 33+) |
| `READ_EXTERNAL_STORAGE` | Legacy gallery access (API ≤32) |
| `WRITE_EXTERNAL_STORAGE` | Save images/documents (API ≤28) |

No `INTERNET`, `ACCESS_NETWORK_STATE`, or any other network permissions.

## Hardware Features

| Feature | Required |
|---------|----------|
| `android.hardware.camera` | Yes |

## Activities

| Activity | Export | Launch Mode | Theme |
|----------|--------|-------------|-------|
| `MainActivity` | Yes | standard | `Theme.OpenScan` |

### Intent Filters

**MainActivity** handles:
- `MAIN` / `LAUNCHER` — app launch
- `ACTION_SEND` with `image/*` — share-to-scan from other apps

## Services

None.

## Broadcast Receivers

None.

## Content Providers

| Provider | Authority | Export |
|----------|-----------|--------|
| `FileProvider` | `com.openscan.app.provider` | No |

## Queries (Package Visibility)

None.

## Metadata

None beyond standard Android configuration.

## FileProvider Paths

Defined in `res/xml/file_paths.xml`:

| Path | Type |
|------|------|
| `captured/` | `files-path` |
| `exports/` | `files-path` |
| Cache root | `cache-path` |
