package com.openscan.app.image

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.ColorMatrix
import android.graphics.ColorMatrixColorFilter
import android.graphics.Matrix
import android.graphics.Paint
import android.graphics.PointF
import android.graphics.RectF
import android.renderscript.Allocation
import android.renderscript.Element
import android.renderscript.RenderScript
import android.renderscript.ScriptIntrinsicConvolve3x3
import java.io.File
import kotlin.math.hypot
import kotlin.math.max

class ImageProcessor(private val context: Context) {

    fun rotate(bitmap: Bitmap, degrees: Float): Bitmap {
        val matrix = Matrix().apply { postRotate(degrees) }
        return Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true)
    }

    fun toGrayscale(bitmap: Bitmap): Bitmap {
        val result = bitmap.copy(Bitmap.Config.ARGB_8888, true)
        val canvas = android.graphics.Canvas(result)
        val paint = Paint().apply {
            colorFilter = ColorMatrixColorFilter(
                ColorMatrix(floatArrayOf(
                    0.33f, 0.33f, 0.33f, 0f, 0f,
                    0.33f, 0.33f, 0.33f, 0f, 0f,
                    0.33f, 0.33f, 0.33f, 0f, 0f,
                    0f, 0f, 0f, 1f, 0f
                ))
            )
        }
        canvas.drawBitmap(bitmap, 0f, 0f, paint)
        return result
    }

    fun enhanceDocument(bitmap: Bitmap): Bitmap {
        // Enhance contrast and sharpen for document readability
        val result = bitmap.copy(Bitmap.Config.ARGB_8888, true)
        val canvas = android.graphics.Canvas(result)

        // Increase contrast via color matrix
        val contrast = 1.4f
        val brightness = -30f
        val matrix = ColorMatrix(floatArrayOf(
            contrast, 0f, 0f, 0f, brightness,
            0f, contrast, 0f, 0f, brightness,
            0f, 0f, contrast, 0f, brightness,
            0f, 0f, 0f, 1f, 0f
        ))
        val paint = Paint().apply { colorFilter = ColorMatrixColorFilter(matrix) }
        canvas.drawBitmap(bitmap, 0f, 0f, paint)
        return result
    }

    fun autoCrop(bitmap: Bitmap): Bitmap? {
        // Find content bounds by scanning non-white pixels
        val width = bitmap.width
        val height = bitmap.height
        val pixels = IntArray(width * height)
        bitmap.getPixels(pixels, 0, width, 0, 0, width, height)

        var top = height
        var bottom = 0
        var left = width
        var right = 0

        for (y in 0 until height) {
            for (x in 0 until width) {
                val pixel = pixels[y * width + x]
                val r = (pixel shr 16) and 0xFF
                val g = (pixel shr 8) and 0xFF
                val b = pixel and 0xFF
                // If pixel is not near-white
                if (r < 240 || g < 240 || b < 240) {
                    if (y < top) top = y
                    if (y > bottom) bottom = y
                    if (x < left) left = x
                    if (x > right) right = x
                }
            }
        }

        val margin = 4
        val cropLeft = maxOf(0, left - margin)
        val cropTop = maxOf(0, top - margin)
        val cropRight = minOf(width, right + margin)
        val cropBottom = minOf(height, bottom + margin)

        if (cropRight <= cropLeft || cropBottom <= cropTop) return null

        return Bitmap.createBitmap(bitmap, cropLeft, cropTop,
            cropRight - cropLeft, cropBottom - cropTop)
    }

    /**
     * Perspective warp — maps the 4 source corners to a flat rectangle.
     * Output bitmap dimensions are computed from physical corner distances
     * to avoid stretching (see ASSESSMENT.md).
     */
    fun warpPerspective(bitmap: Bitmap, srcPoints: List<PointF>): Bitmap {
        val (tl, tr, br, bl) = srcPoints

        val widthA = hypot((br.x - bl.x).toDouble(), (br.y - bl.y).toDouble())
        val widthB = hypot((tr.x - tl.x).toDouble(), (tr.y - tl.y).toDouble())
        val maxWidth = max(widthA, widthB).toInt().coerceAtLeast(1)

        val heightA = hypot((tr.x - br.x).toDouble(), (tr.y - br.y).toDouble())
        val heightB = hypot((tl.x - bl.x).toDouble(), (tl.y - bl.y).toDouble())
        val maxHeight = max(heightA, heightB).toInt().coerceAtLeast(1)

        val src = floatArrayOf(tl.x, tl.y, tr.x, tr.y, br.x, br.y, bl.x, bl.y)
        val dst = floatArrayOf(
            0f, 0f,
            maxWidth.toFloat(), 0f,
            maxWidth.toFloat(), maxHeight.toFloat(),
            0f, maxHeight.toFloat()
        )

        val matrix = Matrix().apply { setPolyToPoly(src, 0, dst, 0, 4) }
        val result = Bitmap.createBitmap(maxWidth, maxHeight, Bitmap.Config.ARGB_8888)
        Canvas(result).drawBitmap(bitmap, matrix, Paint(Paint.FILTER_BITMAP_FLAG))
        return result
    }

    /**
     * Standard rectangular crop with normalized coordinates.
     */
    fun standardCrop(bitmap: Bitmap, normRect: RectF): Bitmap {
        val left = (normRect.left * bitmap.width).toInt().coerceIn(0, bitmap.width - 1)
        val top = (normRect.top * bitmap.height).toInt().coerceIn(0, bitmap.height - 1)
        val right = (normRect.right * bitmap.width).toInt().coerceIn(left + 1, bitmap.width)
        val bottom = (normRect.bottom * bitmap.height).toInt().coerceIn(top + 1, bitmap.height)
        return Bitmap.createBitmap(bitmap, left, top, right - left, bottom - top)
    }

    /**
     * Automatically locates the document's edges using OpenCV contour
     * detection (see [DocumentDetector]) and returns normalized (0..1)
     * corner positions in TL, TR, BR, BL order, along with whether a
     * confident quadrilateral was actually found. Falls back to a centered
     * inset rectangle when detection doesn't find one, so the crop screen
     * always has a usable starting point for manual adjustment.
     */
    fun detectDocumentBorders(bitmap: Bitmap): DetectedCorners {
        return DocumentDetector.detect(bitmap)
    }

    /**
     * Load a downscaled bitmap for UI display (avoids OOM on 50MP photos).
     */
    fun loadDownscaled(path: String, maxDimension: Int = 1080): Bitmap? {
        val opts = BitmapFactory.Options().apply { inJustDecodeBounds = true }
        BitmapFactory.decodeFile(path, opts)
        val scale = maxOf(opts.outWidth, opts.outHeight) / maxDimension
        opts.inSampleSize = scale.coerceAtLeast(1)
        opts.inJustDecodeBounds = false
        return BitmapFactory.decodeFile(path, opts)
    }
}
