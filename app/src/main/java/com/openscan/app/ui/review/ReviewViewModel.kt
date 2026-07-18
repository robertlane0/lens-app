package com.openscan.app.ui.review

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
import javax.inject.Inject

data class ReviewState(
    val document: Document? = null,
    val pages: List<Page> = emptyList(),
    val currentPageIndex: Int = 0,
    val isLoading: Boolean = true
)

@HiltViewModel
class ReviewViewModel @Inject constructor(
    private val repository: DocumentRepository
) : ViewModel() {

    private val _state = MutableStateFlow(ReviewState())
    val state: StateFlow<ReviewState> = _state.asStateFlow()

    fun loadDocument(documentId: Long) {
        viewModelScope.launch {
            val doc = repository.getDocument(documentId)
            val pages = repository.getPages(documentId)
            _state.value = ReviewState(
                document = doc,
                pages = pages,
                currentPageIndex = 0,
                isLoading = false
            )
        }
    }

    fun deletePage(page: Page) {
        viewModelScope.launch {
            repository.deletePage(page)
            _state.value.document?.let { doc ->
                val pages = repository.getPages(doc.id)
                repository.updatePageCount(doc.id, pages.size)
                _state.value = _state.value.copy(pages = pages)
            }
        }
    }

    fun selectPage(index: Int) {
        if (index in _state.value.pages.indices) {
            _state.value = _state.value.copy(currentPageIndex = index)
        }
    }

    fun movePage(from: Int, to: Int) {
        val pages = _state.value.pages.toMutableList()
        if (from in pages.indices && to in pages.indices) {
            val item = pages.removeAt(from)
            pages.add(to, item)
            viewModelScope.launch {
                pages.forEachIndexed { index, page ->
                    repository.updatePageNumber(page.id, index)
                }
                _state.value = _state.value.copy(pages = pages)
            }
        }
    }

    fun deleteDocument() {
        viewModelScope.launch {
            _state.value.document?.let { repository.deleteDocument(it) }
        }
    }
}
