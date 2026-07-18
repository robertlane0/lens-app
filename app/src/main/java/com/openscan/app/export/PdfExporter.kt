package com.openscan.app.export

import android.content.Context
import android.graphics.BitmapFactory
import android.graphics.pdf.PdfDocument
import android.net.Uri
import android.os.Environment
import androidx.core.content.FileProvider
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class PdfExporter(private val context: Context) {

    data class ExportRequest(
        val imagePaths: List<String>,
        val title: String = "Scan"
    )

    data class ExportResult(
        val uri: Uri,
        val file: File,
        val mimeType: String = "application/pdf"
    )

    fun exportToPdf(request: ExportRequest): ExportResult {
        val document = PdfDocument()

        for ((index, path) in request.imagePaths.withIndex()) {
            val bitmap = BitmapFactory.decodeFile(path) ?: continue

            val pageInfo = PdfDocument.PageInfo.Builder(
                bitmap.width, bitmap.height, index + 1
            ).create()

            val page = document.startPage(pageInfo)
            page.canvas.drawBitmap(bitmap, 0f, 0f, null)
            document.finishPage(page)
        }

        val fileName = "${request.title}_${SimpleDateFormat(
            "yyyyMMdd_HHmmss", Locale.US
        ).format(Date())}.pdf"

        val dir = File(context.cacheDir, "exports")
        dir.mkdirs()
        val file = File(dir, fileName)

        FileOutputStream(file).use { out ->
            document.writeTo(out)
        }
        document.close()

        val uri = FileProvider.getUriForFile(
            context, "${context.packageName}.provider", file
        )

        return ExportResult(uri = uri, file = file)
    }
}
