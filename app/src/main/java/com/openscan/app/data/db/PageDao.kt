package com.openscan.app.data.db

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update

@Dao
interface PageDao {
    @Query("SELECT * FROM pages WHERE documentId = :documentId ORDER BY pageNumber ASC")
    suspend fun getPagesForDocument(documentId: Long): List<Page>

    @Query("SELECT * FROM pages WHERE id = :id")
    suspend fun getPageById(id: Long): Page?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPage(page: Page): Long

    @Update
    suspend fun updatePage(page: Page)

    @Delete
    suspend fun deletePage(page: Page)

    @Query("DELETE FROM pages WHERE documentId = :documentId")
    suspend fun deletePagesForDocument(documentId: Long)

    @Query("UPDATE pages SET pageNumber = :pageNumber WHERE id = :id")
    suspend fun updatePageNumber(id: Long, pageNumber: Int)

    @Query("""
        UPDATE pages SET rotation = :rotation, 
        filterType = :filterType, enhancedPath = :enhancedPath 
        WHERE id = :id
    """)
    suspend fun updatePageEnhancements(
        id: Long,
        rotation: Int,
        filterType: String,
        enhancedPath: String?
    )

    @Query("SELECT MAX(pageNumber) FROM pages WHERE documentId = :documentId")
    suspend fun getMaxPageNumber(documentId: Long): Int?
}
