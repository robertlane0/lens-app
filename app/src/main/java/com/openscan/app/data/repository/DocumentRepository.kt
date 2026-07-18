package com.openscan.app.data.repository

import com.openscan.app.data.db.AppDatabase
import com.openscan.app.data.db.Document
import com.openscan.app.data.db.Page
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DocumentRepository @Inject constructor(
    databaseProvider: com.openscan.app.data.db.DatabaseProvider
) {
    private val db = databaseProvider.database
    private val documentDao = db.documentDao()
    private val pageDao = db.pageDao()

    val allDocuments: Flow<List<Document>> = documentDao.getAllDocuments()

    suspend fun getDocument(id: Long): Document? = documentDao.getDocumentById(id)
    suspend fun insertDocument(document: Document): Long = documentDao.insertDocument(document)
    suspend fun updateDocument(document: Document) = documentDao.updateDocument(document)
    suspend fun deleteDocument(document: Document) = documentDao.deleteDocument(document)

    suspend fun updatePageCount(id: Long, count: Int) = documentDao.updatePageCount(id, count)
    suspend fun updateThumbnail(id: Long, path: String) = documentDao.updateThumbnail(id, path)

    suspend fun getPages(documentId: Long): List<Page> = pageDao.getPagesForDocument(documentId)
    suspend fun getPage(id: Long): Page? = pageDao.getPageById(id)
    suspend fun insertPage(page: Page): Long = pageDao.insertPage(page)
    suspend fun updatePage(page: Page) = pageDao.updatePage(page)
    suspend fun deletePage(page: Page) = pageDao.deletePage(page)

    suspend fun deletePagesForDocument(documentId: Long) =
        pageDao.deletePagesForDocument(documentId)

    suspend fun updatePageNumber(id: Long, pageNumber: Int) =
        pageDao.updatePageNumber(id, pageNumber)

    suspend fun updatePageEnhancements(
        id: Long, rotation: Int, filterType: String, enhancedPath: String?
    ) = pageDao.updatePageEnhancements(id, rotation, filterType, enhancedPath)

    suspend fun updatePageCrop(
        id: Long, cropPoints: String?, cropRect: String?, perspectivePath: String?
    ) = pageDao.updatePageCrop(id, cropPoints, cropRect, perspectivePath)

    suspend fun getMaxPageNumber(documentId: Long): Int? =
        pageDao.getMaxPageNumber(documentId)
}
