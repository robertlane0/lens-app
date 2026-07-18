package com.openscan.app.ui.edit

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.openscan.app.data.db.Page
import com.openscan.app.data.repository.DocumentRepository
import com.openscan.app.image.ImageProcessor
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import javax.inject.Inject

data class EditState(
    val page: Page? = null,
    val currentBitmap: Bitmap? = null,
    val rotation: Int = 0,
    val filterType: String = "original",
    val isLoading: Boolean = true
)

@HiltViewModel
class EditViewModel @Inject constructor(
    private val repository: DocumentRepository,
    private val imageProcessor: ImageProcessor
) : ViewModel() {

    private val _state = MutableStateFlow(EditState())
    val state: StateFlow<EditState> = _state.asStateFlow()

    fun loadPage(pageId: Long) {
        viewModelScope.launch {
            val page = repository.getPage(pageId)
            if (page != null) {
                val bitmap = withContext(Dispatchers.IO) {
                    BitmapFactory.decodeFile(page.imagePath)
                }
                _state.value = EditState(
                    page = page,
                    currentBitmap = bitmap,
                    rotation = page.rotation,
                    filterType = page.filterType,
                    isLoading = false
                )
            }
        }
    }

    fun rotate(degrees: Int = 90) {
        val bitmap = _state.value.currentBitmap ?: return
        val newRotation = (_state.value.rotation + degrees) % 360
        val rotated = withContextIO { imageProcessor.rotate(bitmap, degrees.toFloat()) }
        if (rotated != null) {
            _state.value = _state.value.copy(
                currentBitmap = rotated,
                rotation = newRotation
            )
        }
    }

    fun setFilter(type: String) {
        val bitmap = _state.value.currentBitmap ?: return
        val filtered = withContextIO {
            when (type) {
                "grayscale" -> imageProcessor.toGrayscale(bitmap)
                "document" -> imageProcessor.enhanceDocument(bitmap)
                else -> bitmap
            }
        }
        if (filtered != null) {
            _state.value = _state.value.copy(
                currentBitmap = filtered,
                filterType = type
            )
        }
    }

    fun save() {
        val page = _state.value.page ?: return
        val bitmap = _state.value.currentBitmap ?: return
        viewModelScope.launch {
            val file = withContext(Dispatchers.IO) {
                val dir = File(page.imagePath).parentFile ?: return@withContext null
                val enhancedFile = File(dir, "enhanced_${page.id}.jpg")
                enhancedFile.outputStream().use { out ->
                    bitmap.compress(Bitmap.CompressFormat.JPEG, 95, out)
                }
                enhancedFile
            }
            if (file != null) {
                repository.updatePageEnhancements(
                    id = page.id,
                    rotation = _state.value.rotation,
                    filterType = _state.value.filterType,
                    enhancedPath = file.absolutePath
                )
            }
        }
    }

    private fun withContextIO(block: () -> Bitmap?): Bitmap? {
        var result: Bitmap? = null
        viewModelScope.launch {
            result = withContext(Dispatchers.IO) { block() }
        }
        return result
    }
}
