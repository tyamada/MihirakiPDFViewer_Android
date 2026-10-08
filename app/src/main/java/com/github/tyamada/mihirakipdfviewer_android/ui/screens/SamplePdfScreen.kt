package com.github.tyamada.mihirakipdfviewer_android.ui.screens

import android.net.Uri
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.OpenInNew
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Download
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.github.tyamada.mihirakipdfviewer_android.data.SamplePdfItem
import com.github.tyamada.mihirakipdfviewer_android.data.SamplePdfItemState
import com.github.tyamada.mihirakipdfviewer_android.viewmodel.SamplePdfViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SamplePdfScreen(
    viewModel: SamplePdfViewModel,
    back: () -> Unit,
    onOpenPdf: (Uri) -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Sample PDFs (サンプルPDF)") },
                navigationIcon = {
                    IconButton(onClick = back) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            // Language Filter Chips
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState())
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                uiState.availableLanguages.forEach { lang ->
                    val isSelected = uiState.selectedLanguage == lang
                    FilterChip(
                        selected = isSelected,
                        onClick = { viewModel.setLanguage(lang) },
                        label = {
                            val labelText = when (lang) {
                                "ALL" -> "All (全部)"
                                "en" -> "English"
                                "ja" -> "Japanese (日本語)"
                                "ko" -> "Korean (한국어)"
                                "zh" -> "Chinese (中文)"
                                "de" -> "German (Deutsch)"
                                "fr" -> "French (Français)"
                                else -> lang.uppercase()
                            }
                            Text(labelText)
                        }
                    )
                }
            }

            // PDF List & Attribution Footer
            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(uiState.items, key = { it.item.id }) { itemState ->
                    SamplePdfCard(
                        itemState = itemState,
                        onDownload = { viewModel.requestDownload(itemState.item) },
                        onOpen = {
                            itemState.localFile?.let { file ->
                                onOpenPdf(Uri.fromFile(file))
                            }
                        },
                        onDelete = { viewModel.deletePdf(itemState.item) }
                    )
                }

                item {
                    Spacer(modifier = Modifier.height(16.dp))
                    Card(
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(
                                "© 2026 Komairo Biyori / CC BY 4.0",
                                style = MaterialTheme.typography.titleSmall
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                "Parts of this work are provided under the Creative Commons Attribution 4.0 International (CC BY 4.0) license.\n" +
                                "Attribution: \"The Try-It Club\" by Komairo Biyori.",
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                    }
                }
            }
        }
    }

    // Cellular Network Confirmation Dialog
    uiState.cellularDialogItem?.let { item ->
        AlertDialog(
            onDismissRequest = { viewModel.dismissCellularDialog() },
            title = { Text("Cellular Data Warning (モバイル通信の確認)") },
            text = { Text("You are currently on a cellular network. Downloading \"${item.title}\" (${item.fileSize}) may consume a significant amount of mobile data. Do you want to proceed?") },
            confirmButton = {
                TextButton(onClick = { viewModel.confirmCellularDownload(item) }) {
                    Text("Download (ダウンロード)")
                }
            },
            dismissButton = {
                TextButton(onClick = { viewModel.dismissCellularDialog() }) {
                    Text("Cancel (キャンセル)")
                }
            }
        )
    }
}

@Composable
fun SamplePdfCard(
    itemState: SamplePdfItemState,
    onDownload: () -> Unit,
    onOpen: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Text(
                text = itemState.item.title,
                style = MaterialTheme.typography.titleMedium
            )
            Spacer(modifier = Modifier.height(4.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "${itemState.item.languageDisplayName} • ${itemState.item.fileSize}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            if (itemState.isDownloading) {
                Spacer(modifier = Modifier.height(8.dp))
                LinearProgressIndicator(
                    progress = { itemState.progress },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Downloading... ${(itemState.progress * 100).toInt()}%",
                    style = MaterialTheme.typography.bodySmall
                )
            }

            itemState.errorMessage?.let { err ->
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Error: $err",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.error
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (itemState.isDownloaded && !itemState.isDownloading) {
                    OutlinedButton(
                        onClick = onDelete,
                        modifier = Modifier.padding(end = 8.dp)
                    ) {
                        Icon(Icons.Default.Delete, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Delete (削除)")
                    }

                    Button(onClick = onOpen) {
                        Icon(Icons.AutoMirrored.Filled.OpenInNew, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Open (開く)")
                    }
                } else {
                    Button(
                        onClick = onDownload,
                        enabled = !itemState.isDownloading
                    ) {
                        Icon(Icons.Default.Download, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(if (itemState.isDownloading) "Downloading... (ダウンロード中)" else "Download (ダウンロード)")
                    }
                }
            }
        }
    }
}
