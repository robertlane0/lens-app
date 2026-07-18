package com.openscan.app.ui.export

import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel

@Composable
fun ExportDialog(
    documentId: Long,
    onDismiss: () -> Unit,
    viewModel: ExportViewModel = hiltViewModel()
) {
    val context = LocalContext.current

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Export") },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text("Choose export format:")
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedButton(
                        onClick = {
                            viewModel.exportAsPdf(documentId) { result ->
                                when (result) {
                                    is ExportViewModel.ExportResult.Success -> {
                                        if (result.uris.size == 1) {
                                            com.openscan.app.ui.export.ShareHelper.shareUri(
                                                context, result.uris.first(), result.mimeType, "Document"
                                            )
                                        } else {
                                            com.openscan.app.ui.export.ShareHelper.shareMultipleUris(
                                                context, result.uris, result.mimeType, "Document"
                                            )
                                        }
                                    }
                                    is ExportViewModel.ExportResult.Error -> {
                                        Toast.makeText(context, result.message, Toast.LENGTH_SHORT).show()
                                    }
                                }
                            }
                            onDismiss()
                        },
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(Icons.Default.PictureAsPdf, contentDescription = null)
                        Text(" PDF", modifier = Modifier.padding(start = 4.dp))
                    }
                    OutlinedButton(
                        onClick = {
                            viewModel.exportAsImages(documentId) { result ->
                                when (result) {
                                    is ExportViewModel.ExportResult.Success -> {
                                        if (result.uris.size == 1) {
                                            com.openscan.app.ui.export.ShareHelper.shareUri(
                                                context, result.uris.first(), result.mimeType, "Document"
                                            )
                                        } else {
                                            com.openscan.app.ui.export.ShareHelper.shareMultipleUris(
                                                context, result.uris, result.mimeType, "Document"
                                            )
                                        }
                                    }
                                    is ExportViewModel.ExportResult.Error -> {
                                        Toast.makeText(context, result.message, Toast.LENGTH_SHORT).show()
                                    }
                                }
                            }
                            onDismiss()
                        },
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(Icons.Default.Image, contentDescription = null)
                        Text(" Images", modifier = Modifier.padding(start = 4.dp))
                    }
                }
            }
        },
        confirmButton = {},
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@Composable
fun showOcrResult(text: String?, onDismiss: () -> Unit) {
    if (text != null) {
        AlertDialog(
            onDismissRequest = onDismiss,
            title = { Text("Extracted Text") },
            text = { Text(text) },
            confirmButton = {
                TextButton(onClick = onDismiss) {
                    Text("Close")
                }
            }
        )
    }
}
