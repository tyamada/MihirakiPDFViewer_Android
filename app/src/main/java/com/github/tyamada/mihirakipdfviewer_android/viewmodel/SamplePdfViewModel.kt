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
import com.github.tyamada.mihirakipdfviewer_android.data.SettingsRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
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

    val uiState: StateFlow<SamplePdfUiState> = combine(
        _downloadStates,
        _selectedLanguage,
        _sortOrder,
        _cellularDialogItem,
        _refreshTrigger
    ) { states, lang, sortOrder, dialogItem, _ ->
        val allLangs = SamplePdfCatalog.items.map { it.languageCode }.distinct()
        val mappedItems = SamplePdfCatalog.items.map { item ->
            val dlState = states[item.id] ?: DownloadState.Idle
            val isDownloaded = repository.isDownloaded(item.fileName)
            val isDownloading = dlState is DownloadState.Downloading
            val progress = (dlState as? DownloadState.Downloading)?.progress ?: 0f
            val err = (dlState as? DownloadState.Error)?.message
            val localFile = if (isDownloaded) repository.getLocalFile(item.fileName) else null

            SamplePdfItemState(
                item = item,
                isDownloaded = isDownloaded,
                isDownloading = isDownloading,
                progress = progress,
                errorMessage = err,
                localFile = localFile
            )
        }.filter { lang == "ALL" || it.item.languageCode == lang }

        val sortedItems = mappedItems.sortedWith(
            compareBy<SamplePdfItemState> { itemState ->
                val epNum = itemState.item.id.removePrefix("ep").substringBefore("_").toIntOrNull() ?: 1
                if (sortOrder == SamplePdfSortOrder.ASC) epNum else -epNum
            }.thenBy { itemState ->
                itemState.item.languageDisplayName
            }
        )

        SamplePdfUiState(
            items = sortedItems,
            selectedLanguage = lang,
            availableLanguages = listOf("ALL") + allLangs,
            sortOrder = sortOrder,
            cellularDialogItem = dialogItem
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = SamplePdfUiState()
    )

    private fun getDefaultLanguage(): String {
        val deviceLang = Locale.getDefault().language
        val available = SamplePdfCatalog.items.map { it.languageCode }.distinct()
        return if (available.contains(deviceLang)) deviceLang else "en"
    }

    fun setLanguage(lang: String) {
        _selectedLanguage.value = lang
    }

    fun toggleSortOrder() {
        val newOrder = if (_sortOrder.value == SamplePdfSortOrder.ASC) {
            SamplePdfSortOrder.DESC
        } else {
            SamplePdfSortOrder.ASC
        }
        _sortOrder.value = newOrder
        viewModelScope.launch {
            val currentSettings = settingsRepository.settings.first()
            settingsRepository.save(currentSettings.copy(samplePdfSortOrder = newOrder))
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

    fun deletePdf(item: SamplePdfItem) {
        viewModelScope.launch {
            repository.deletePdf(item.fileName)
            _downloadStates.value = _downloadStates.value - item.id
            _refreshTrigger.value++
        }
    }
}
