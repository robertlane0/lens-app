package com.openscan.app.di

import android.content.Context
import com.openscan.app.data.db.DatabaseProvider
import com.openscan.app.data.repository.DocumentRepository
import com.openscan.app.export.PdfExporter
import com.openscan.app.image.ImageProcessor
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideDatabaseProvider(@ApplicationContext context: Context): DatabaseProvider {
        return DatabaseProvider(context)
    }

    @Provides
    @Singleton
    fun provideDocumentRepository(provider: DatabaseProvider): DocumentRepository {
        return DocumentRepository(provider)
    }

    @Provides
    @Singleton
    fun provideImageProcessor(@ApplicationContext context: Context): ImageProcessor {
        return ImageProcessor(context)
    }

    @Provides
    @Singleton
    fun providePdfExporter(@ApplicationContext context: Context): PdfExporter {
        return PdfExporter(context)
    }
}
