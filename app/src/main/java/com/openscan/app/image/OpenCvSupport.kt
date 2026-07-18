package com.openscan.app.image

import android.util.Log
import org.opencv.android.OpenCVLoader

/**
 * Loads OpenCV's native libraries once for the process lifetime.
 *
 * The `org.opencv:opencv` artifact bundles the native `.so` files directly
 * in the AAR, so [OpenCVLoader.initLocal] loads them synchronously from the
 * APK — no Google Play Services "OpenCV Manager" or network access needed,
 * which keeps the app fully offline per project conventions.
 *
 * [init] is called once from [com.openscan.app.OpenScanApp.onCreate].
 * [isAvailable] lets callers (e.g. [DocumentDetector]) fall back gracefully
 * instead of crashing if native loading ever fails on a given device/ABI.
 */
object OpenCvSupport {
    private const val TAG = "OpenCvSupport"

    @Volatile
    var isAvailable: Boolean = false
        private set

    fun init() {
        isAvailable = try {
            OpenCVLoader.initLocal()
        } catch (t: Throwable) {
            Log.e(TAG, "OpenCV native libraries failed to load", t)
            false
        }
        if (!isAvailable) {
            Log.w(TAG, "OpenCV native libraries did not load; auto edge detection is disabled")
        }
    }
}
