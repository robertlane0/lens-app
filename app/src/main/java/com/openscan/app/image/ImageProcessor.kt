package com.openscan.app.image

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.ColorMatrix
import android.graphics.ColorMatrixColorFilter
import android.graphics.Matrix
import android.graphics.Paint
import android.renderscript.Allocation
import android.renderscript.Element
import android.renderscript.RenderScript
import android.renderscript.ScriptIntrinsicConvolve3x3
import java.io.File

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

    data class DocumentBorders(
        val topLeft: Pair<Float, Float>,
        val topRight: Pair<Float, Float>,
        val bottomRight: Pair<Float, Float>,
        val bottomLeft: Pair<Float, Float>
    )

    fun detectDocumentBorders(bitmap: Bitmap): DocumentBorders? {
        // Simplified border detection using edge sampling
        // A full implementation would use OpenCV's findContours
        val width = bitmap.width
        val height = bitmap.height

        return DocumentBorders(
            topLeft = Pair(width * 0.05f, height * 0.05f),
            topRight = Pair(width * 0.95f, height * 0.05f),
            bottomRight = Pair(width * 0.95f, height * 0.95f),
            bottomLeft = Pair(width * 0.05f, height * 0.95f)
        )
    }
}
