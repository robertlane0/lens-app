# Data Storage

## Overview

OpenScan uses two storage mechanisms:

```mermaid
flowchart TD
    A[Storage Layer] --> B[Room Database]
    A --> C[File System]
    B --> D[openscan.db]
    D --> E[documents table]
    D --> F[pages table]
    C --> G[captured/ directory]
    C --> H[exports/ directory]
    C --> I[thumbnails/ (in captured/)]
```

## Room Database

### openscan.db (Version 2)

#### Table: documents

| Column | Type | Notes |
|--------|------|-------|
| `id` | INTEGER (PK) | Auto-generate |
| `title` | TEXT | Document title |
| `createdAt` | INTEGER | Unix timestamp (ms) |
| `updatedAt` | INTEGER | Unix timestamp (ms) |
| `pageCount` | INTEGER | Number of pages |
| `thumbnailPath` | TEXT (nullable) | Path to thumbnail image |

#### Table: pages

| Column | Type | Notes |
|--------|------|-------|
| `id` | INTEGER (PK) | Auto-generate |
| `documentId` | INTEGER (FK) | References documents.id, CASCADE delete |
| `pageNumber` | INTEGER | Order within document |
| `imagePath` | TEXT | Path to original captured image |
| `enhancedPath` | TEXT (nullable) | Path to enhanced version |
| `rotation` | INTEGER | Rotation in degrees (0, 90, 180, 270) |
| `filterType` | TEXT | "original", "grayscale", or "document" |
| `cropPoints` | TEXT (nullable) | Serialized corner points for perspective crop |
| `cropRect` | TEXT (nullable) | Serialized standard crop rectangle |
| `perspectivePath` | TEXT (nullable) | Path to perspective-corrected image |

**Index**: `idx_pages_documentId` on `documentId`.

**Migration**: Version 2 uses `fallbackToDestructiveMigration()` (no production data migration).

## File System

### Directory Structure

```
{appFilesDir}/
  captured/
    capture_<timestamp>.jpg      # Original captured image
    enhanced_<pageId>.jpg        # After filter/rotation enhancement
    persp_<pageId>.jpg           # After perspective correction
  exports/
    <title>.pdf                  # Exported PDF
    <title>/                     # Exported multi-image folder
      page_001.jpg
      page_002.jpg
  cache/                         # Android cache (temp export files)
```

### FileProvider Paths

Defined in `res/xml/file_paths.xml`:

| Path | Type |
|------|------|
| `captured/` | `files-path` — internal captured images |
| `exports/` | `files-path` — exported PDFs/images |
| Cache root | `cache-path` — temporary export sharing |

## Data Access

`DocumentRepository` (`data/repository/DocumentRepository.kt`) is the single point of data access. It wraps both DAOs:

- **DocumentDao**: `getAllDocuments` (Flow), `getDocumentById`, `insertDocument`, `updateDocument`, `deleteDocument`, `updatePageCount`, `updateThumbnail`
- **PageDao**: `getPagesForDocument`, `getPageById`, `insertPage`, `updatePage`, `deletePage`, `deletePagesForDocument`, `updatePageNumber`, `updatePageEnhancements`, `updatePageCrop`, `getMaxPageNumber`

## Image Storage Strategy

| File | When Created | Usage |
|------|-------------|-------|
| `capture_<timestamp>.jpg` | Camera capture | Original source image |
| `enhanced_<pageId>.jpg` | Edit screen save | Filtered/rotated version for export |
| `persp_<pageId>.jpg` | Crop screen apply | Perspective-corrected version for export |

Export uses the most processed version available: `perspectivePath` → `enhancedPath` → `imagePath`.

## No SharedPreferences for Document Data

Unlike the original Lens app, OpenScan does not use SharedPreferences for document metadata. All structured data lives in Room. Preferences are only used implicitly by Android/Compose framework components.
