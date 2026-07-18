package com.openscan.app.data.db

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface DocumentDao {
    @Query("SELECT * FROM documents ORDER BY updatedAt DESC")
    fun getAllDocuments(): Flow<List<Document>>

    @Query("SELECT * FROM documents WHERE id = :id")
    suspend fun getDocumentById(id: Long): Document?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDocument(document: Document): Long

    @Update
    suspend fun updateDocument(document: Document)

    @Delete
    suspend fun deleteDocument(document: Document)

    @Query("UPDATE documents SET pageCount = :count, updatedAt = :updatedAt WHERE id = :id")
    suspend fun updatePageCount(id: Long, count: Int, updatedAt: Long = System.currentTimeMillis())

    @Query("UPDATE documents SET thumbnailPath = :path WHERE id = :id")
    suspend fun updateThumbnail(id: Long, path: String)
}
