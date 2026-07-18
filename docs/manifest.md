# Android Manifest Analysis

## App Identity

```xml
package="com.microsoft.office.officelens"
compileSdkVersion="34"
compileSdkVersionCodename="14"
```

## Permissions

| Permission | Purpose |
|------------|---------|
| `CAMERA` | Camera capture (required, core feature) |
| `READ_MEDIA_IMAGES` | Read gallery images (API 33+) |
| `READ_EXTERNAL_STORAGE` | Legacy gallery access |
| `WRITE_EXTERNAL_STORAGE` | Save images/documents |
| `READ_MEDIA_VIDEO` | Video processing |
| `READ_MEDIA_VISUAL_USER_SELECTED` | Partial media access (API 34) |
| `INTERNET` | Cloud connectivity |
| `ACCESS_NETWORK_STATE` | Network status checks |
| `ACCESS_DOWNLOAD_MANAGER` | Download manager access |
| `DOWNLOAD_WITHOUT_NOTIFICATION` | Silent downloads |
| `WAKE_LOCK` | Background processing |
| `FOREGROUND_SERVICE` | Foreground service |
| `VIBRATE` | Haptic feedback |
| `RECEIVE_BOOT_COMPLETED` | WorkManager rescheduling |
| `ACCESS_WIFI_STATE` | WiFi detection |
| `NFC` | NFC tag scanning |
| `AUTHENTICATE_ACCOUNTS` | Account management |
| `USE_CREDENTIALS` | Credential access |
| `MANAGE_ACCOUNTS` | Account creation |
| `CHECK_LICENSE` | Google Play licensing |

## Hardware Features

| Feature | Required |
|---------|----------|
| `android.hardware.camera` | Yes |
| `android.hardware.camera.autofocus` | No |
| `android.hardware.usb.host` | No |
| `android.hardware.nfc` | No |

## Activities

| Activity | Export | Launch Mode | Theme | Parent |
|----------|--------|-------------|-------|--------|
| `MainActivity` | Yes | singleTask | SplashTheme | — |
| `SecureActivity` | Yes | standard | AppCompatTheme | MainActivity |
| `SettingsActivity` | Yes | standard | AppTheme | MainActivity |
| `AboutActivity` | Yes | standard | AppTheme | SettingsActivity |
| `FirstRunActivity` | Yes | standard | FirstRunTheme | MainActivity |
| `PermissionRequestActivity` | No | standard | FirstRunTheme | — |
| `AccountPickerActivity` | Yes | standard | AppTheme | — |
| `SignInWrapperActivity` | No | singleTop | AppTheme | — |
| `LensActivity` | Yes | — | NoActionBar | — |
| `ImmersiveGalleryActivity` | Yes | — | GalleryTheme | — |
| `IRActivity` (Immersive Reader) | Yes | — | LensTheme | — |
| `OneNotePickerActivity` | Yes | — | Translucent | — |
| `OneDrivePicker` (deprecated) | Yes | — | Translucent | — |

### Intent Filters

**MainActivity** handles:
- `MAIN` / `LAUNCHER` — app launch
- `SEND` / `SEND_MULTIPLE` with `image/jpeg`, `image/png` — share-to-Lens

## Services

| Service | Export | Purpose |
|---------|--------|---------|
| `TokenSharingService` (tokenshare) | Yes | Cross-app token sharing (AIDL) |
| `AuthenticationService` | Yes | Account authenticator |
| `SharedPrefService` | Yes | Cross-app shared preferences |
| `MlKitComponentDiscoveryService` | No | ML Kit component discovery |
| `MAMNotificationReceiverService` | Yes | Intune notifications |
| `MAMBackgroundService` | No | Intune background tasks |
| `MAMBackgroundJobService` | No | Intune job scheduler |

## Broadcast Receivers

| Receiver | Export | Action |
|----------|--------|--------|
| `AccountChangedBroadcastReceiver` | Yes | `LOGIN_ACCOUNTS_CHANGED` |
| `MAMBackgroundReceiver` | Yes | `DOWNLOAD_COMPLETE` |
| WorkManager receivers | Varies | Power, storage, network, boot, time changes |

## Content Providers

| Provider | Authority | Export |
|----------|-----------|--------|
| `FileProvider` | `com.microsoft.office.officelens.provider` | No |
| `MlKitInitProvider` | `com.microsoft.office.officelens.mlkitinitprovider` | No |

## Queries (Package Visibility)

<details>
<summary>Authentication packages</summary>

- `com.azure.authenticator`
- `com.microsoft.identity.testuserapp`
- `com.microsoft.windowsintune.companyportal`
- `com.microsoft.mockauthapp`, `mockcp`, `mockltw`
- `com.microsoft.appmanager`
</details>

<details>
<summary>Office suite packages</summary>

- `com.microsoft.office.officehub`, `officehubrow`, `officehubrow.internal`
- `com.microsoft.office.onenote`, `onenote.internal`
- `com.microsoft.office.powerpoint`, `powerpoint.internal`
- `com.microsoft.office.word`, `word.internal`
</details>

## Metadata

| Key | Value |
|-----|-------|
| `appType` | `full` |
| `accountType` | `com.microsoft.office` |
| `accountLabel` | Microsoft 365 |
| `intune.mam.aad.ClientID` | `d3590ed6-52b3-4102-aeff-aad2292ab01c` |
| `intune.mam.aad.Authority` | `https://login.windows.net/common/` |
| `intune.mam.aad.NonBrokerRedirectURI` | `urn:ietf:wg:oauth:2.0:oob` |
| `intune.mam.MAMMultiIdentity` | `true` |
| `intune.mam.aad.SkipBroker` | `false` |
| `intune.mam.ForceProductionAgent` | `true` |
| `authorization.supportedAccountTypes` | `@array/authentication_supported_account_types` |
| `token_share_sdk_version` | `2` |
| `token_share_build_version` | `1.6.12` |
| `token_share_parcelable_version` | `3` |

## App Restrictions (Intune MAM Policies)

- `AllowedAccountUPNs` — restrict to specific user accounts
- `NotesCreationEnabled` — allow/disallow note creation
- `TeamsApps.IsAllowed` — allow/disallow Teams integration
- `BingChatEnterprise.IsAllowed` — Copilot access control
