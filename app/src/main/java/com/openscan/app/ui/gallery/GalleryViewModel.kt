package com.openscan.app.ui.gallery

import androidx.lifecycle.ViewModel
import com.openscan.app.data.db.Document
import com.openscan.app.data.repository.DocumentRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

@HiltViewModel
class GalleryViewModel @Inject constructor(
    repository: DocumentRepository
) : ViewModel() {
    val documents: Flow<List<Document>> = repository.allDocuments
}
