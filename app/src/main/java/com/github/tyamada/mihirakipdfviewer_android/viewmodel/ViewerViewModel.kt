package com.github.tyamada.mihirakipdfviewer_android.viewmodel

import android.app.Application
import android.graphics.Bitmap
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.github.tyamada.mihirakipdfviewer_android.billing.*
import com.github.tyamada.mihirakipdfviewer_android.data.*
import com.github.tyamada.mihirakipdfviewer_android.pdf.*
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

data class ViewerUiState(
    val loading: Boolean = false, val source: PdfSource? = null, val uri: Uri? = null,
    val currentPage: Int = 0, val bitmap: Bitmap? = null, val secondBitmap: Bitmap? = null,
    val chromeVisible: Boolean = false, val passwordRequested: Boolean = false,
    val errorKey: String? = null, val searchQuery: String = "", val searchResults: List<SearchHit> = emptyList(),
    val currentSearchIndex: Int = -1,
    val settings: ViewerSettings = ViewerSettings(),
) { val pageCount get() = source?.pageCount ?: 0; val info get() = source?.info ?: DocumentInfo() }

class ViewerViewModel(app: Application) : AndroidViewModel(app) {
    private val pdfs = PdfDocumentRepository(app)
    private val preferences = SettingsRepository(app)
    val billing = BillingManager(app)
    private val _state = MutableStateFlow(ViewerUiState())
    val state: StateFlow<ViewerUiState> = _state.asStateFlow()
    private var renderJob: Job? = null

    init { viewModelScope.launch { preferences.settings.collect { _state.update { s -> s.copy(settings = it) } } } }

    fun open(uri: Uri, password: String? = null) = viewModelScope.launch {
        closeDocument(); _state.update { it.copy(loading = true, uri = uri, errorKey = null, passwordRequested = false) }
        runCatching { pdfs.open(uri, password) }.onSuccess { source ->
            val direction = DirectionDetector.fromMetadata(source.layoutHint, source.directionHint) ?: ReadingDirection.L2R
            val layout = LayoutDetector.fromMetadata(source.layoutHint) ?: ViewerLayout.SINGLE
            val showCover = LayoutDetector.shouldShowCover(source.layoutHint) ?: false

            val newSettings = _state.value.settings.copy(
                direction = direction,
                layout = layout,
                showCover = showCover,
            )
            _state.update {
                it.copy(
                    loading = false,
                    source = source,
                    passwordRequested = false,
                    chromeVisible = false,
                    settings = newSettings,
                )
            }
            preferences.save(newSettings)
            render(0)
        }.onFailure { e ->
            when (e) {
                is PdfOpenException.PasswordRequired -> _state.update { it.copy(loading = false, passwordRequested = true) }
                is PdfOpenException.WrongPassword -> _state.update { it.copy(loading = false, passwordRequested = true, errorKey = "wrong_password") }
                is PdfOpenException.PermissionDenied -> _state.update { it.copy(loading = false, errorKey = "permission_denied") }
                else -> _state.update { it.copy(loading = false, errorKey = "cannot_open_pdf") }
            }
        }
    }

    fun render(page: Int, width: Int = 1080) {
        val source = _state.value.source ?: return
        val target = page.coerceIn(0, source.pageCount - 1)
        _state.update { it.copy(currentPage = target) }
        renderJob?.cancel(); renderJob = viewModelScope.launch {
            val settings = _state.value.settings
            val hits = _state.value.searchResults
            val leftHits = hits.asSequence().filter { it.pageIndex == target }.flatMap { it.rects }.toList()
            
            if (settings.layout == ViewerLayout.SPREAD) {
                val spreads = SpreadPlanner.plan(source.pageCount, settings.direction, settings.showCover, settings.coverMode)
                val spread = spreads.firstOrNull { (it.left == target) || (it.right == target) } ?: spreads.first()
                
                val leftTarget = spread.left
                val rightTarget = spread.right
                val leftRects = hits.asSequence().filter { it.pageIndex == leftTarget }.flatMap { it.rects }.toList()
                val rightRects = hits.asSequence().filter { it.pageIndex == rightTarget }.flatMap { it.rects }.toList()

                val left = leftTarget?.let { source.render(it, width / 2, settings.highQuality, settings.sharpness, leftRects) }
                val right = rightTarget?.let { source.render(it, width / 2, settings.highQuality, settings.sharpness, rightRects) }
                _state.update { it.copy(currentPage = minOf(spread.left ?: Int.MAX_VALUE, spread.right ?: Int.MAX_VALUE), bitmap = left, secondBitmap = right) }
            } else {
                val first = source.render(target, width, settings.highQuality, settings.sharpness, leftHits)
                _state.update { it.copy(bitmap = first, secondBitmap = null) }
            }
        }
    }
    fun move(delta: Int) = render(_state.value.currentPage + (delta * if (_state.value.settings.layout == ViewerLayout.SPREAD) 2 else 1))
    fun toggleChrome() = _state.update { it.copy(chromeVisible = !it.chromeVisible) }
    fun dismissError() = _state.update { it.copy(errorKey = null) }
    fun search(query: String) = viewModelScope.launch {
        _state.update { it.copy(searchQuery = query) }; val source = _state.value.source ?: return@launch
        val results = source.search(query)
        _state.update { it.copy(searchResults = results, currentSearchIndex = if (results.isNotEmpty()) 0 else -1, errorKey = if (query.isNotBlank() && results.isEmpty()) "no_results" else null) }
        results.firstOrNull()?.let { render(it.pageIndex) }
    }
    fun navigateSearch(delta: Int) {
        val results = _state.value.searchResults
        if (results.isEmpty()) return
        val nextIndex = (_state.value.currentSearchIndex + delta).let {
            if (it < 0) results.size - 1 else if (it >= results.size) 0 else it
        }
        _state.update { it.copy(currentSearchIndex = nextIndex) }
        render(results[nextIndex].pageIndex)
    }
    fun updateSettings(transform: (ViewerSettings) -> ViewerSettings) = viewModelScope.launch {
        val value = transform(_state.value.settings); _state.update { it.copy(settings = value) }; preferences.save(value); render(_state.value.currentPage)
    }
    fun reset() = viewModelScope.launch { preferences.reset(); closeDocument(); _state.value = ViewerUiState() }
    fun closeDocument() { renderJob?.cancel(); _state.value.source?.close(); _state.update { it.copy(source = null, bitmap = null, secondBitmap = null, currentPage = 0, passwordRequested = false) } }
    override fun onCleared() { closeDocument(); billing.close() }
}
