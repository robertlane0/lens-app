package com.openscan.app.ui.export

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.pdf.PdfDocument
import android.net.Uri
import androidx.core.content.FileProvider
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.openscan.app.data.repository.DocumentRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import javax.inject.Inject

@HiltViewModel
class ExportViewModel @Inject constructor(
    private val repository: DocumentRepository,
    @ApplicationContext private val context: Context
) : ViewModel() {

    sealed class ExportResult {
        data class Success(val uris: List<Uri>, val mimeType: String) : ExportResult()
        data class Error(val message: String) : ExportResult()
    }

    fun exportAsPdf(documentId: Long, onResult: (ExportResult) -> Unit) {
        viewModelScope.launch {
            val result = exportPdfInternal(documentId)
            onResult(result)
        }
    }

    private suspend fun exportPdfInternal(documentId: Long): ExportResult {
        return withContext(Dispatchers.IO) {
            try {
                val document = repository.getDocument(documentId)
                if (document == null) {
                    return@withContext ExportResult.Error("Document not found")
                }
                val pages = repository.getPages(documentId)

                val pdfDocument = PdfDocument()

                for ((index, page) in pages.withIndex()) {
                    val bitmap = BitmapFactory.decodeFile(
                        page.enhancedPath ?: page.imagePath
                    ) ?: continue

                    val pdfPage = pdfDocument.startPage(
                        PdfDocument.PageInfo.Builder(
                            bitmap.width, bitmap.height, index + 1
                        ).create()
                    )
                    pdfPage.canvas.drawBitmap(bitmap, 0f, 0f, null)
                    pdfDocument.finishPage(pdfPage)
                }

                val fileName = "OpenScan_${SimpleDateFormat(
                    "yyyyMMdd_HHmmss", Locale.US
                ).format(Date())}.pdf"

                val file = File(context.cacheDir, "exports/$fileName")
                file.parentFile?.mkdirs()
                FileOutputStream(file).use { out ->
                    pdfDocument.writeTo(out)
                }
                pdfDocument.close()

                val uri = FileProvider.getUriForFile(
                    context,
                    "${context.packageName}.provider",
                    file
                )

                ExportResult.Success(listOf(uri), "application/pdf")
            } catch (e: Exception) {
                ExportResult.Error(e.message ?: "Export failed")
            }
        }
    }

    fun exportAsImages(documentId: Long, onResult: (ExportResult) -> Unit) {
        viewModelScope.launch {
            val result = exportImagesInternal(documentId)
            onResult(result)
        }
    }

    private suspend fun exportImagesInternal(documentId: Long): ExportResult {
        return withContext(Dispatchers.IO) {
            try {
                val document = repository.getDocument(documentId)
                if (document == null) {
                    return@withContext ExportResult.Error("Document not found")
                }
                val pages = repository.getPages(documentId)

                val exportDir = File(context.cacheDir, "exports")
                exportDir.mkdirs()

                val uris = mutableListOf<Uri>()

                for ((index, page) in pages.withIndex()) {
                    val bitmap = BitmapFactory.decodeFile(
                        page.enhancedPath ?: page.imagePath
                    ) ?: continue
                    val file = File(exportDir, "page_${index + 1}.jpg")
                    FileOutputStream(file).use { out ->
                        bitmap.compress(Bitmap.CompressFormat.JPEG, 95, out)
                    }
                    val uri = FileProvider.getUriForFile(
                        context, "${context.packageName}.provider", file
                    )
                    uris.add(uri)
                }

                ExportResult.Success(uris, "image/jpeg")
            } catch (e: Exception) {
                ExportResult.Error(e.message ?: "Export failed")
            }
        }
    }
}
