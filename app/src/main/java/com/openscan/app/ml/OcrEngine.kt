package com.openscan.app.ml

import android.content.Context
import android.graphics.Bitmap
import com.google.android.gms.tasks.Tasks
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class OcrEngine {

    private val recognizer = TextRecognition.getClient(
        TextRecognizerOptions.DEFAULT_OPTIONS
    )

    suspend fun recognizeText(context: Context, bitmap: Bitmap): String {
        return withContext(Dispatchers.IO) {
            try {
                val image = InputImage.fromBitmap(bitmap, 0)
                val task = recognizer.process(image)
                val result = Tasks.await(task)
                result.text
            } catch (e: Exception) {
                ""
            }
        }
    }

    fun close() {
        recognizer.close()
    }
}
