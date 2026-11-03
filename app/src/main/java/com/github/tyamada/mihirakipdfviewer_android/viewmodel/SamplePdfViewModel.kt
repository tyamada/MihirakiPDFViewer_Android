package com.github.tyamada.mihirakipdfviewer_android.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.github.tyamada.mihirakipdfviewer_android.data.DownloadState
import com.github.tyamada.mihirakipdfviewer_android.data.SamplePdfCatalog
import com.github.tyamada.mihirakipdfviewer_android.data.SamplePdfItem
import com.github.tyamada.mihirakipdfviewer_android.data.SamplePdfItemState
import com.github.tyamada.mihirakipdfviewer_android.data.SamplePdfRepository
import com.github.tyamada.mihirakipdfviewer_android.data.SamplePdfSortOrder
import com.github.tyamada.mihirakipdfviewer_android.data.sortSamplePdfs
import com.github.tyamada.mihirakipdfviewer_android.data.SettingsRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import java.io.File
import com.github.tyamada.mihirakipdfviewer_android.data.SamplePdfLanguages
import com.github.tyamada.mihirakipdfviewer_android.R
import java.util.Locale

data class SamplePdfUiState(
    val items: List<SamplePdfItemState> = emptyList(),
    val selectedLanguage: String = "en",
    val availableLanguages: List<String> = emptyList(),
    val sortOrder: SamplePdfSortOrder = SamplePdfSortOrder.DESC,
    val cellularDialogItem: SamplePdfItem? = null
)

class SamplePdfViewModel(application: Application) : AndroidViewModel(application) {
    private val repository = SamplePdfRepository(application)
    private val settingsRepository = SettingsRepository(application)

    private val _downloadStates = MutableStateFlow<Map<String, DownloadState>>(emptyMap())
    private val _selectedLanguage = MutableStateFlow(getDefaultLanguage())
    private val _sortOrder = MutableStateFlow(SamplePdfSortOrder.DESC)
    private val _cellularDialogItem = MutableStateFlow<SamplePdfItem?>(null)
    private val _refreshTrigger = MutableStateFlow(0)

    init {
        viewModelScope.launch {
            settingsRepository.settings.collect { settings ->
                _sortOrder.value = settings.samplePdfSortOrder
            }
        }
    }

    private val verifiedFiles = _refreshTrigger.map {
        SamplePdfCatalog.items.associate { item -> item.id to repository.getDownloadedFile(item) }
    }.flowOn(Dispatchers.IO)

    val uiState: StateFlow<SamplePdfUiState> = combine(
        _downloadStates,
        _selectedLanguage,
        _sortOrder,
        _cellularDialogItem,
        verifiedFiles
    ) { states, lang, sortOrder, dialogItem, files ->
        val allLangs = SamplePdfCatalog.getAvailableLanguages()
        val mappedItems = SamplePdfCatalog.items.map { item ->
            val dlState = states[item.id] ?: DownloadState.Idle
            val localFile = files[item.id]
            val isDownloaded = localFile != null
            val isDownloading = dlState is DownloadState.Downloading
            val progress = (dlState as? DownloadState.Downloading)?.progress ?: 0f
            val err = (dlState as? DownloadState.Error)?.let {
                if (it.integrityFailure) getApplication<Application>().getString(R.string.sample_pdf_integrity_error) else it.message
            }

            SamplePdfItemState(
                item = item,
                isDownloaded = isDownloaded,
                isDownloading = isDownloading,
                progress = progress,
                errorMessage = err,
                localFile = localFile
            )
        }.filter { lang == "ALL" || it.item.languageCode == lang }

        val sortedItems = sortSamplePdfs(mappedItems, sortOrder)

        SamplePdfUiState(
            items = sortedItems,
            selectedLanguage = lang,
            availableLanguages = listOf("ALL") + allLangs,
            sortOrder = sortOrder,
            cellularDialogItem = dialogItem
        )
    }.flowOn(Dispatchers.IO).stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = SamplePdfUiState()
    )

    private fun getDefaultLanguage(): String {
        return SamplePdfLanguages.defaultCode(Locale.getDefault())
    }

    fun setLanguage(lang: String) {
        _selectedLanguage.value = lang
    }

    fun toggleSortOrder() {
        viewModelScope.launch {
            settingsRepository.toggleSamplePdfSortOrder()
        }
    }

    fun requestDownload(item: SamplePdfItem) {
        if (repository.isOnCellularNetwork()) {
            _cellularDialogItem.value = item
        } else {
            startDownload(item)
        }
    }

    fun confirmCellularDownload(item: SamplePdfItem) {
        _cellularDialogItem.value = null
        startDownload(item)
    }

    fun dismissCellularDialog() {
        _cellularDialogItem.value = null
    }

    fun startDownload(item: SamplePdfItem) {
        viewModelScope.launch {
            repository.downloadPdf(item).collect { state ->
                _downloadStates.value = _downloadStates.value + (item.id to state)
                if (state is DownloadState.Success || state is DownloadState.Error) {
                    _refreshTrigger.value++
                }
            }
        }
    }

    fun openDownloadedPdf(item: SamplePdfItem, onOpen: (File) -> Unit) {
        viewModelScope.launch {
            val file = withContext(Dispatchers.IO) { repository.getDownloadedFile(item) }
            if (file != null) onOpen(file) else {
                _downloadStates.value = _downloadStates.value + (item.id to DownloadState.Error(
                    getApplication<Application>().getString(R.string.sample_pdf_integrity_error), true
                ))
                _refreshTrigger.value++
            }
        }
    }

    fun deletePdf(item: SamplePdfItem) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.deletePdf(item)
            _downloadStates.value = _downloadStates.value - item.id
            _refreshTrigger.value++
        }
    }
}
