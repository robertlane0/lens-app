package com.openscan.app.ui.crop

import android.graphics.Bitmap
import android.graphics.PointF
import android.graphics.RectF
import androidx.compose.ui.geometry.Offset
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.openscan.app.data.db.Page
import com.openscan.app.data.repository.DocumentRepository
import com.openscan.app.image.ImageProcessor
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import javax.inject.Inject

enum class CropStage { PERSPECTIVE, STANDARD }

data class CropState(
    val stage: CropStage = CropStage.PERSPECTIVE,
    val isLoading: Boolean = true,
    val displayBitmap: Bitmap? = null,
    val originalWidth: Int = 0,
    val originalHeight: Int = 0,
    val corners: List<Offset> = emptyList(),
    val cropRect: RectF = RectF(0f, 0f, 1f, 1f),
    val error: String? = null
)

@HiltViewModel
class CropViewModel @Inject constructor(
    private val repository: DocumentRepository,
    private val imageProcessor: ImageProcessor
) : ViewModel() {

    private val _state = MutableStateFlow(CropState())
    val state: StateFlow<CropState> = _state.asStateFlow()

    private var page: Page? = null

    fun loadPage(pageId: Long) {
        viewModelScope.launch {
            val p = repository.getPage(pageId) ?: run {
                _state.value = _state.value.copy(error = "Page not found", isLoading = false)
                return@launch
            }
            page = p

            val sourcePath = p.perspectivePath ?: p.imagePath
            val fullOpts = BitmapFactoryUtils.decodeBounds(sourcePath)

            val display = withContext(Dispatchers.IO) {
                imageProcessor.loadDownscaled(sourcePath)
            }

            val hasSavedCrop = p.cropPoints != null
            var autoApplyPerspective = false

            val initialCorners = if (hasSavedCrop) {
                parseCorners(p.cropPoints!!)
            } else {
                withContext(Dispatchers.IO) {
                    val full = BitmapFactoryUtils.decodeFile(sourcePath)
                    if (full != null) {
                        val detected = imageProcessor.detectDocumentBorders(full)
                        autoApplyPerspective = detected.found
                        detected.corners.map { pt -> Offset(pt.x, pt.y) }
                    } else {
                        listOf(
                            Offset(0.05f, 0.05f),
                            Offset(0.95f, 0.05f),
                            Offset(0.95f, 0.95f),
                            Offset(0.05f, 0.95f)
                        )
                    }
                }
            }

            val initialRect = if (p.cropRect != null) {
                parseCropRect(p.cropRect)
            } else {
                RectF(0f, 0f, 1f, 1f)
            }

            _state.value = CropState(
                isLoading = false,
                displayBitmap = display,
                originalWidth = fullOpts.width,
                originalHeight = fullOpts.height,
                corners = initialCorners,
                cropRect = initialRect
            )

            // A confidently detected document gets its perspective crop
            // applied right away; a low-confidence/failed detection falls
            // back to the default inset rectangle and waits for the user
            // to line the corners up manually.
            if (autoApplyPerspective) {
                applyPerspectiveCorrection {}
            }
        }
    }

    fun updateCorner(index: Int, newNorm: Offset) {
        val list = _state.value.corners.toMutableList()
        if (index in list.indices) {
            list[index] = newNorm.coerceIn(0f, 1f)
            _state.value = _state.value.copy(corners = list)
        }
    }

    fun updateCropRect(newRect: RectF) {
        _state.value = _state.value.copy(
            cropRect = RectF(
                newRect.left.coerceIn(0f, 1f),
                newRect.top.coerceIn(0f, 1f),
                newRect.right.coerceIn(0f, 1f),
                newRect.bottom.coerceIn(0f, 1f)
            )
        )
    }

    fun applyPerspectiveCorrection(onDone: () -> Unit) {
        val p = page ?: return
        val s = _state.value

        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true)

            val points = s.corners.map { corner ->
                PointF(
                    corner.x * s.originalWidth,
                    corner.y * s.originalHeight
                )
            }

            withContext(Dispatchers.IO) {
                val fullBitmap = BitmapFactoryUtils.decodeFile(p.imagePath)
                if (fullBitmap != null) {
                    val warped = imageProcessor.warpPerspective(fullBitmap, points)
                    val parentDir = File(p.imagePath).parentFile ?: return@withContext
                    val perspFile = File(parentDir, "persp_${p.id}.jpg")
                    perspFile.outputStream().use { out ->
                        warped.compress(Bitmap.CompressFormat.JPEG, 95, out)
                    }

                    val displayWarped = imageProcessor.loadDownscaled(perspFile.absolutePath)

                    _state.value = CropState(
                        stage = CropStage.STANDARD,
                        isLoading = false,
                        displayBitmap = displayWarped,
                        originalWidth = warped.width,
                        originalHeight = warped.height,
                        corners = s.corners,
                        cropRect = RectF(0f, 0f, 1f, 1f)
                    )
                }
            }
        }
    }

    /**
     * Returns to the perspective stage with the same corners that produced
     * the current crop, so a user can nudge an imperfect auto-detection
     * (or redo a manual one) instead of starting over from scratch.
     */
    fun redoPerspective() {
        val p = page ?: return
        val previousCorners = _state.value.corners

        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true)

            val fullOpts = BitmapFactoryUtils.decodeBounds(p.imagePath)
            val display = withContext(Dispatchers.IO) {
                imageProcessor.loadDownscaled(p.imagePath)
            }

            _state.value = CropState(
                stage = CropStage.PERSPECTIVE,
                isLoading = false,
                displayBitmap = display,
                originalWidth = fullOpts.width,
                originalHeight = fullOpts.height,
                corners = previousCorners,
                cropRect = RectF(0f, 0f, 1f, 1f)
            )
        }
    }

    fun saveCrop(onSaved: () -> Unit) {
        val p = page ?: return
        val s = _state.value

        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true)

            val sourcePath = withContext(Dispatchers.IO) {
                val parentDir = File(p.imagePath).parentFile ?: return@withContext null
                val perspFile = File(parentDir, "persp_${p.id}.jpg")
                if (perspFile.exists()) perspFile.absolutePath else null
            }

            if (sourcePath == null) {
                _state.value = _state.value.copy(isLoading = false, error = "No corrected image")
                return@launch
            }

            withContext(Dispatchers.IO) {
                val fullBitmap = BitmapFactoryUtils.decodeFile(sourcePath)
                if (fullBitmap != null) {
                    val cropped = imageProcessor.standardCrop(fullBitmap, s.cropRect)
                    val parentDir = File(sourcePath).parentFile ?: return@withContext
                    val outFile = File(parentDir, "persp_${p.id}.jpg")
                    outFile.outputStream().use { out ->
                        cropped.compress(Bitmap.CompressFormat.JPEG, 95, out)
                    }

                    val cropPointsStr = s.corners.joinToString(";") { "${it.x},${it.y}" }
                    val cropRectStr = "${s.cropRect.left},${s.cropRect.top}," +
                            "${s.cropRect.right},${s.cropRect.bottom}"

                    repository.updatePageCrop(
                        id = p.id,
                        cropPoints = cropPointsStr,
                        cropRect = cropRectStr,
                        perspectivePath = outFile.absolutePath
                    )
                }
            }

            onSaved()
        }
    }

    companion object {
        fun parseCorners(str: String): List<Offset> {
            return str.split(";").map { part ->
                val (x, y) = part.split(",")
                Offset(x.toFloatOrNull() ?: 0f, y.toFloatOrNull() ?: 0f)
            }
        }

        fun parseCropRect(str: String): RectF {
            val parts = str.split(",").map { it.toFloatOrNull() ?: 0f }
            return if (parts.size >= 4) {
                RectF(parts[0], parts[1], parts[2], parts[3])
            } else {
                RectF(0f, 0f, 1f, 1f)
            }
        }
    }
}

private fun Offset.coerceIn(min: Float, max: Float): Offset {
    return Offset(
        x.coerceIn(min, max),
        y.coerceIn(min, max)
    )
}

private object BitmapFactoryUtils {
    data class Bounds(val width: Int, val height: Int)

    fun decodeBounds(path: String): Bounds {
        val opts = android.graphics.BitmapFactory.Options().apply { inJustDecodeBounds = true }
        android.graphics.BitmapFactory.decodeFile(path, opts)
        if (opts.outWidth <= 0 || opts.outHeight <= 0) {
            return Bounds(1, 1)
        }
        return Bounds(opts.outWidth, opts.outHeight)
    }

    fun decodeFile(path: String): Bitmap? {
        return android.graphics.BitmapFactory.decodeFile(path)
    }
}
