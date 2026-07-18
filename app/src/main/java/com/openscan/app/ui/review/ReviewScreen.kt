package com.openscan.app.ui.review

import android.net.Uri
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.FileDownload
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.TextSnippet
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.rememberAsyncImagePainter
import com.openscan.app.data.db.Page
import com.openscan.app.ui.export.ExportDialog
import com.openscan.app.ui.export.showOcrResult
import com.openscan.app.ml.OcrEngine

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReviewScreen(
    documentId: Long,
    onNavigateBack: () -> Unit,
    onNavigateToEdit: (Long, Long) -> Unit,
    onNavigateToCamera: () -> Unit,
    viewModel: ReviewViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()
    val context = LocalContext.current
    var showExportDialog by remember { mutableStateOf(false) }
    var showOcrDialog by remember { mutableStateOf<String?>(null) }
    val ocrEngine = remember { OcrEngine() }

    LaunchedEffect(documentId) {
        viewModel.loadDocument(documentId)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(state.document?.title ?: "Review") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = { showExportDialog = true }) {
                        Icon(Icons.Default.FileDownload, contentDescription = "Export")
                    }
                    IconButton(onClick = { viewModel.deleteDocument(); onNavigateBack() }) {
                        Icon(Icons.Default.Delete, contentDescription = "Delete")
                    }
                }
            )
        },
        bottomBar = {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.surface)
                    .padding(8.dp),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                androidx.compose.material3.Button(
                    onClick = onNavigateToCamera,
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(Icons.Default.Add, contentDescription = null)
                    Text(" Add Page", modifier = Modifier.padding(start = 4.dp))
                }
            }
        }
    ) { padding ->
        if (state.isLoading) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                androidx.compose.material3.CircularProgressIndicator()
            }
        } else if (state.pages.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize().padding(padding),
                contentAlignment = Alignment.Center
            ) {
                Text("No pages", style = MaterialTheme.typography.bodyLarge)
            }
        } else {
            val currentPage = state.pages.getOrNull(state.currentPageIndex)

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
            ) {
                // Main preview
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .padding(8.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant),
                    contentAlignment = Alignment.Center
                ) {
                    if (currentPage != null) {
                        Image(
                            painter = rememberAsyncImagePainter(Uri.fromFile(
                                java.io.File(currentPage.imagePath)
                            )),
                            contentDescription = "Page ${currentPage.pageNumber + 1}",
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(8.dp),
                            contentScale = ContentScale.Fit
                        )

                        // Edit button
                        IconButton(
                            onClick = { onNavigateToEdit(currentPage.id, documentId) },
                            modifier = Modifier
                                .align(Alignment.TopEnd)
                                .padding(8.dp)
                                .background(
                                    MaterialTheme.colorScheme.surface.copy(alpha = 0.8f),
                                    RoundedCornerShape(50)
                                )
                        ) {
                            Icon(Icons.Default.Edit, contentDescription = "Edit")
                        }
                    }
                }

                // Page thumbnails
                LazyRow(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(8.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    itemsIndexed(state.pages) { index, page ->
                        ThumbnailItem(
                            page = page,
                            index = index,
                            isSelected = index == state.currentPageIndex,
                            onClick = { viewModel.selectPage(index) },
                            onDelete = { viewModel.deletePage(page) }
                        )
                    }
                }
            }
        }
    }

    if (showExportDialog) {
        ExportDialog(
            documentId = documentId,
            onDismiss = { showExportDialog = false }
        )
    }

    showOcrResult(showOcrDialog) { showOcrDialog = null }
}

@Composable
fun ThumbnailItem(
    page: Page,
    index: Int,
    isSelected: Boolean,
    onClick: () -> Unit,
    onDelete: () -> Unit
) {
    Box(
        modifier = Modifier
            .width(72.dp)
            .clickable(onClick = onClick)
    ) {
        Image(
            painter = rememberAsyncImagePainter(Uri.fromFile(java.io.File(page.imagePath))),
            contentDescription = "Page ${index + 1}",
            modifier = Modifier
                .aspectRatio(0.7f)
                .clip(RoundedCornerShape(4.dp))
                .then(
                    if (isSelected) Modifier.background(
                        MaterialTheme.colorScheme.primary,
                        RoundedCornerShape(4.dp)
                    ) else Modifier
                ),
            contentScale = ContentScale.Crop
        )
        Text(
            text = "${index + 1}",
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .background(
                    MaterialTheme.colorScheme.surface.copy(alpha = 0.8f),
                    RoundedCornerShape(4.dp)
                )
                .padding(horizontal = 4.dp),
            style = MaterialTheme.typography.labelSmall
        )
        IconButton(
            onClick = onDelete,
            modifier = Modifier
                .align(Alignment.TopEnd)
                .size(20.dp)
        ) {
            Icon(
                Icons.Default.Delete,
                contentDescription = "Delete",
                modifier = Modifier.size(14.dp)
            )
        }
    }
}
