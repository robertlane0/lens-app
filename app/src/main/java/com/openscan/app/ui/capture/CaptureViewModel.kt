package com.openscan.app.ui.capture

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.openscan.app.data.db.Document
import com.openscan.app.data.db.Page
import com.openscan.app.data.repository.DocumentRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.io.File
import javax.inject.Inject

data class CaptureState(
    val isCapturing: Boolean = false,
    val lastCapturedUri: Uri? = null,
    val currentDocumentId: Long? = null,
    val pageCount: Int = 0,
    val error: String? = null
)

@HiltViewModel
class CaptureViewModel @Inject constructor(
    private val repository: DocumentRepository
) : ViewModel() {

    private val _state = MutableStateFlow(CaptureState())
    val state: StateFlow<CaptureState> = _state.asStateFlow()

    fun startNewDocument() {
        viewModelScope.launch {
            val doc = Document(
                title = "Scan ${java.text.SimpleDateFormat(
                    "yyyy-MM-dd HH:mm",
                    java.util.Locale.getDefault()
                ).format(java.util.Date())}",
                pageCount = 0
            )
            val docId = repository.insertDocument(doc)
            _state.value = _state.value.copy(currentDocumentId = docId, pageCount = 0)
        }
    }

    fun addPage(imageFile: File) {
        val docId = _state.value.currentDocumentId
        if (docId == null) {
            startNewDocument()
            viewModelScope.launch {
                _state.value.let { state ->
                    val docId = state.currentDocumentId ?: return@launch
                    addPageInternal(docId, imageFile)
                }
            }
        } else {
            viewModelScope.launch {
                addPageInternal(docId, imageFile)
            }
        }
    }

    private suspend fun addPageInternal(docId: Long, imageFile: File) {
        val maxPage = repository.getMaxPageNumber(docId) ?: -1
        val page = Page(
            documentId = docId,
            pageNumber = maxPage + 1,
            imagePath = imageFile.absolutePath
        )
        repository.insertPage(page)
        val newCount = maxPage + 2
        repository.updatePageCount(docId, newCount)
        _state.value = _state.value.copy(
            pageCount = newCount,
            lastCapturedUri = Uri.fromFile(imageFile)
        )
    }

    fun finish() {
        _state.value.currentDocumentId?.let { docId ->
            viewModelScope.launch {
                val pages = repository.getPages(docId)
                if (pages.isNotEmpty()) {
                    repository.updateThumbnail(docId, pages.first().imagePath)
                }
            }
        }
    }

    fun clear() {
        _state.value = CaptureState()
    }
}
