package com.openscan.app.ml

import android.graphics.Bitmap
import com.google.android.gms.tasks.Tasks
import com.google.mlkit.vision.barcode.BarcodeScannerOptions
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.barcode.common.Barcode
import com.google.mlkit.vision.common.InputImage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class BarcodeScanner {

    private val scanner = BarcodeScanning.getClient(
        BarcodeScannerOptions.Builder()
            .setBarcodeFormats(
                Barcode.FORMAT_QR_CODE,
                Barcode.FORMAT_EAN_13,
                Barcode.FORMAT_EAN_8,
                Barcode.FORMAT_UPC_A,
                Barcode.FORMAT_UPC_E,
                Barcode.FORMAT_CODE_39,
                Barcode.FORMAT_CODE_128,
                Barcode.FORMAT_PDF417,
                Barcode.FORMAT_DATA_MATRIX,
                Barcode.FORMAT_AZTEC
            )
            .build()
    )

    data class BarcodeResult(
        val rawValue: String,
        val format: Int,
        val valueType: Int
    )

    suspend fun scan(bitmap: Bitmap): List<BarcodeResult> {
        return withContext(Dispatchers.IO) {
            try {
                val image = InputImage.fromBitmap(bitmap, 0)
                val task = scanner.process(image)
                val barcodes = Tasks.await(task)
                barcodes.mapNotNull { barcode ->
                    barcode.rawValue?.let { value ->
                        BarcodeResult(
                            rawValue = value,
                            format = barcode.format,
                            valueType = barcode.valueType
                        )
                    }
                }
            } catch (e: Exception) {
                emptyList()
            }
        }
    }

    fun close() {
        scanner.close()
    }
}
