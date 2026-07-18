package com.openscan.app.image

import android.graphics.Bitmap
import android.graphics.PointF
import org.opencv.android.Utils
import org.opencv.core.Mat
import org.opencv.core.MatOfPoint
import org.opencv.core.MatOfPoint2f
import org.opencv.core.Point
import org.opencv.core.Size
import org.opencv.imgproc.Imgproc
import kotlin.math.max
import kotlin.math.min

/**
 * Result of automatic document-edge detection.
 *
 * @param corners Normalized (0..1) corner positions in the image's own
 *   coordinate space, ordered top-left, top-right, bottom-right, bottom-left.
 * @param found Whether a confident, document-shaped quadrilateral was
 *   located. When false, [corners] is still populated (with a centered
 *   inset rectangle) so callers always have a usable starting point for
 *   manual adjustment rather than having to null-check everywhere.
 */
data class DetectedCorners(
    val corners: List<PointF>,
    val found: Boolean
)

/**
 * Locates the largest document-like quadrilateral in a photo using OpenCV
 * contour analysis:
 *
 * grayscale -> Gaussian blur -> Canny edges -> dilate -> find contours ->
 * keep the largest convex 4-point contour above an area threshold.
 *
 * Detection runs on a downscaled copy of the bitmap for speed; the returned
 * corners are normalized (0..1) so callers can map them onto the
 * full-resolution image regardless of what size was passed in.
 */
object DocumentDetector {

    /** Longest side, in px, that the working copy is downscaled to before analysis. */
    private const val WORKING_MAX_DIMENSION = 700.0

    /** A candidate contour must cover at least this fraction of the frame to count as "the document". */
    private const val MIN_AREA_FRACTION = 0.15

    /** approxPolyDP epsilon, as a fraction of the contour's perimeter. */
    private const val APPROX_EPSILON_FRACTION = 0.02

    fun detect(bitmap: Bitmap): DetectedCorners {
        if (!OpenCvSupport.isAvailable) {
            return DetectedCorners(defaultInsetCorners(), found = false)
        }

        val srcMat = Mat()
        val resized = Mat()
        val gray = Mat()
        val blurred = Mat()
        val edges = Mat()
        val dilated = Mat()
        val hierarchy = Mat()
        val contours = mutableListOf<MatOfPoint>()

        return try {
            Utils.bitmapToMat(bitmap, srcMat)

            val longestSide = max(srcMat.width(), srcMat.height()).toDouble()
            val scale = min(1.0, WORKING_MAX_DIMENSION / longestSide)
            Imgproc.resize(srcMat, resized, Size(), scale, scale, Imgproc.INTER_AREA)

            Imgproc.cvtColor(resized, gray, Imgproc.COLOR_RGBA2GRAY)
            Imgproc.GaussianBlur(gray, blurred, Size(5.0, 5.0), 0.0)
            Imgproc.Canny(blurred, edges, 50.0, 150.0)
            Imgproc.dilate(
                edges,
                dilated,
                Imgproc.getStructuringElement(Imgproc.MORPH_RECT, Size(3.0, 3.0))
            )

            Imgproc.findContours(
                dilated,
                contours,
                hierarchy,
                Imgproc.RETR_LIST,
                Imgproc.CHAIN_APPROX_SIMPLE
            )

            val bestQuad = findLargestDocumentQuad(contours, resized.size().area())

            if (bestQuad != null) {
                val ordered = orderCorners(bestQuad)
                val w = resized.width().toDouble()
                val h = resized.height().toDouble()
                val normalized = ordered.map {
                    PointF(
                        (it.x / w).coerceIn(0.0, 1.0).toFloat(),
                        (it.y / h).coerceIn(0.0, 1.0).toFloat()
                    )
                }
                DetectedCorners(normalized, found = true)
            } else {
                DetectedCorners(defaultInsetCorners(), found = false)
            }
        } catch (t: Throwable) {
            // Detection is a best-effort convenience — never let it crash the
            // crop flow. Fall back to the manual-adjustment default instead.
            DetectedCorners(defaultInsetCorners(), found = false)
        } finally {
            contours.forEach { it.release() }
            hierarchy.release()
            dilated.release()
            edges.release()
            blurred.release()
            gray.release()
            resized.release()
            srcMat.release()
        }
    }

    /** Largest convex 4-point contour above the area threshold, or null if none qualifies. */
    private fun findLargestDocumentQuad(contours: List<MatOfPoint>, frameArea: Double): List<Point>? {
        var bestQuad: List<Point>? = null
        var bestArea = frameArea * MIN_AREA_FRACTION

        for (contour in contours) {
            val area = Imgproc.contourArea(contour)
            if (area <= bestArea) continue

            val contour2f = MatOfPoint2f(*contour.toArray())
            val perimeter = Imgproc.arcLength(contour2f, true)
            val approx = MatOfPoint2f()
            Imgproc.approxPolyDP(contour2f, approx, APPROX_EPSILON_FRACTION * perimeter, true)
            val approxPoints = approx.toArray()

            if (approxPoints.size == 4 && Imgproc.isContourConvex(MatOfPoint(*approxPoints))) {
                bestArea = area
                bestQuad = approxPoints.toList()
            }

            approx.release()
            contour2f.release()
        }

        return bestQuad
    }

    /**
     * Orders four unordered corner points as top-left, top-right,
     * bottom-right, bottom-left, using the standard sum/difference trick:
     * the top-left point has the smallest x+y, the bottom-right the
     * largest; the top-right has the smallest y-x, the bottom-left the
     * largest.
     */
    private fun orderCorners(points: List<Point>): List<Point> {
        val bySum = points.sortedBy { it.x + it.y }
        val byDiff = points.sortedBy { it.y - it.x }
        val tl = bySum.first()
        val br = bySum.last()
        val tr = byDiff.first()
        val bl = byDiff.last()
        return listOf(tl, tr, br, bl)
    }

    /** Centered 90%-of-frame rectangle used when no confident detection is available. */
    fun defaultInsetCorners(): List<PointF> = listOf(
        PointF(0.05f, 0.05f),
        PointF(0.95f, 0.05f),
        PointF(0.95f, 0.95f),
        PointF(0.05f, 0.95f)
    )
}
