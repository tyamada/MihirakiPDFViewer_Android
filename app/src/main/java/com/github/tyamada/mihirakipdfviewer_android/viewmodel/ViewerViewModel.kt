package com.github.tyamada.mihirakipdfviewer_android.viewmodel

import android.app.ActivityManager
import android.app.Application
import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import android.os.Build
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.github.tyamada.mihirakipdfviewer_android.billing.*
import com.github.tyamada.mihirakipdfviewer_android.data.*
import com.github.tyamada.mihirakipdfviewer_android.pdf.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.io.File

data class ViewerUiState(
    val loading: Boolean = false, val source: PdfSource? = null, val uri: Uri? = null,
    val currentPage: Int = 0, val bitmap: Bitmap? = null, val secondBitmap: Bitmap? = null,
    val chromeVisible: Boolean = false, val passwordRequested: Boolean = false,
    val errorKey: String? = null, val searchQuery: String = "", val searchResults: List<SearchHit> = emptyList(),
    val currentSearchIndex: Int = -1,
    val settings: ViewerSettings = ViewerSettings(),
    val isPortrait: Boolean = true,
) {
    val pageCount get() = source?.pageCount ?: 0
    val info get() = source?.info ?: DocumentInfo()
    val outline get() = source?.outline ?: emptyList()
    val effectiveLayout: ViewerLayout
        get() = if (isPortrait && settings.portraitSpread) ViewerLayout.SINGLE else settings.layout
}

class ViewerViewModel(app: Application) : AndroidViewModel(app) {
    private val pdfs = PdfDocumentRepository(app)
    private val preferences = SettingsRepository(app)
    val billing = BillingManager(app)
    private val _state = MutableStateFlow(ViewerUiState())
    val state: StateFlow<ViewerUiState> = _state.asStateFlow()
    private var renderJob: Job? = null
    private var searchJob: Job? = null

    private val activityManager = app.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
    private val memoryInfo = ActivityManager.MemoryInfo().also { activityManager.getMemoryInfo(it) }
    private val totalRamGb = memoryInfo.totalMem / (1024.0 * 1024.0 * 1024.0)
    private val isLowRamDevice = activityManager.isLowRamDevice || totalRamGb < 4.0
    private data class RenderKey(val page: Int, val width: Int, val highQuality: Boolean,
                                 val sharpness: Float, val highlights: List<SearchRect>)
    private val bitmapCache = object : LinkedHashMap<RenderKey, Bitmap>(4, 0.75f, true) {
        override fun removeEldestEntry(eldest: MutableMap.MutableEntry<RenderKey, Bitmap>): Boolean {
            return size > if (isLowRamDevice) 0 else 2
        }
    }

    private fun getEffectiveLayout(): ViewerLayout {
        return _state.value.effectiveLayout
    }

    fun setOrientation(isPortrait: Boolean) {
        if (_state.value.isPortrait != isPortrait) {
            _state.update { it.copy(isPortrait = isPortrait) }
            if (_state.value.source != null) {
                render(_state.value.currentPage)
            }
        }
    }

    private suspend fun getOrRender(source: PdfSource, page: Int, width: Int, highQuality: Boolean, sharpness: Float, highlights: List<SearchRect>): Bitmap {
        val targetWidth = if (isLowRamDevice) minOf(width, 720) else width
        val useHighQ = if (isLowRamDevice) false else highQuality
        val useSharp = if (isLowRamDevice) 0f else sharpness
        val key = RenderKey(page, targetWidth, useHighQ, useSharp, highlights.toList())

        if (!isLowRamDevice) {
            synchronized(bitmapCache) {
                bitmapCache[key]?.let { if (!it.isRecycled) return it }
            }
        }
        val bmp = try {
            source.render(page, targetWidth, useHighQ, useSharp, highlights)
        } catch (e: OutOfMemoryError) {
            Log.e("MihirakiPDF", "OOM during render", e)
            source.render(page, targetWidth / 2, false, 0f, highlights)
        }
        if (!isLowRamDevice) {
            synchronized(bitmapCache) {
                bitmapCache[key] = bmp
            }
        }
        return bmp
    }

    init {
        viewModelScope.launch {
            val initial = preferences.settings.first()
            _state.update { it.copy(settings = initial) }
            
            // Auto-resume if no file is currently being opened (e.g. via Intent)
            if (!_state.value.loading && _state.value.source == null) {
                initial.lastUri?.let { uriStr ->
                    val uri = Uri.parse(uriStr)
                    val isValid = if (uri.scheme == "file") {
                        val f = File(uri.path ?: "")
                        f.exists() && f.length() > 0
                    } else true

                    if (isValid) {
                        open(uri, initial.lastPage)
                    } else {
                        val cleared = initial.copy(lastUri = null, lastPage = 0)
                        preferences.save(cleared)
                        _state.update { it.copy(settings = cleared) }
                    }
                }
            }

            // Sync further settings changes
            preferences.settings.collect { s -> _state.update { it.copy(settings = s) } }
        }
    }

    fun open(uri: Uri, startPage: Int = 0, password: String? = null) = viewModelScope.launch {
        if (_state.value.uri == uri && _state.value.source != null) return@launch
        closeDocument(); _state.update { it.copy(loading = true, uri = uri, errorKey = null, passwordRequested = false) }
        runCatching { pdfs.open(uri, password) }.onSuccess { source ->
            val direction = DirectionDetector.fromMetadata(source.layoutHint, source.directionHint) ?: ReadingDirection.L2R
            val layout = LayoutDetector.fromMetadata(source.layoutHint) ?: ViewerLayout.SINGLE
            val showCover = LayoutDetector.shouldShowCover(source.layoutHint) ?: false

            val newSettings = _state.value.settings.copy(
                direction = direction,
                layout = layout,
                showCover = showCover,
                lastUri = uri.toString(),
                lastPage = startPage
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
            render(startPage)
        }.onFailure { e ->
            // Clear lastUri on failure so app doesn't loop opening a corrupted or missing file on restart
            val clearedSettings = _state.value.settings.copy(lastUri = null, lastPage = 0)
            preferences.save(clearedSettings)
            _state.update { it.copy(settings = clearedSettings) }

            when (e) {
                is PdfOpenException.PasswordRequired -> _state.update { it.copy(loading = false, passwordRequested = true) }
                is PdfOpenException.WrongPassword -> _state.update { it.copy(loading = false, passwordRequested = true, errorKey = "wrong_password") }
                is PdfOpenException.PermissionDenied -> _state.update { it.copy(loading = false, errorKey = "permission_denied") }
                else -> _state.update { it.copy(loading = false, errorKey = "cannot_open_pdf") }
            }
        }
    }

    fun render(page: Int, width: Int = if (isLowRamDevice) 720 else 1080) {
        val source = _state.value.source ?: return
        val target = page.coerceIn(0, source.pageCount - 1)
        
        // Save last viewed page
        if (_state.value.currentPage != target) {
            viewModelScope.launch {
                val updated = _state.value.settings.copy(lastPage = target)
                _state.update { it.copy(settings = updated) }
                preferences.save(updated)
            }
        }

        renderJob?.cancel(); renderJob = viewModelScope.launch {
            val settings = _state.value.settings
            val hits = _state.value.searchResults

            // Compose may still draw the previous frame after a state update.
            // Let GC release displaced bitmaps instead of recycling UI-owned images.

            if (getEffectiveLayout() == ViewerLayout.SPREAD) {
                val spreads = SpreadPlanner.plan(source.pageCount, settings.direction, settings.showCover, settings.coverMode)
                val spread = spreads.firstOrNull { (it.left == target) || (it.right == target) } ?: spreads.first()
                
                val leftTarget = spread.left
                val rightTarget = spread.right
                val leftRects = hits.asSequence().filter { it.pageIndex == leftTarget }.flatMap { it.rects }.toList()
                val rightRects = hits.asSequence().filter { it.pageIndex == rightTarget }.flatMap { it.rects }.toList()

                val left = leftTarget?.let { getOrRender(source, it, width / 2, settings.highQuality, settings.sharpness, leftRects) }
                val right = rightTarget?.let { getOrRender(source, it, width / 2, settings.highQuality, settings.sharpness, rightRects) }
                
                _state.update { it.copy(currentPage = minOf(spread.left ?: Int.MAX_VALUE, spread.right ?: Int.MAX_VALUE), bitmap = left, secondBitmap = right) }
            } else {
                val leftHits = hits.asSequence().filter { it.pageIndex == target }.flatMap { it.rects }.toList()
                val first = getOrRender(source, target, width, settings.highQuality, settings.sharpness, leftHits)
                
                _state.update { it.copy(currentPage = target, bitmap = first, secondBitmap = null) }
            }
        }
    }
    fun move(delta: Int) = render(_state.value.currentPage + (delta * if (getEffectiveLayout() == ViewerLayout.SPREAD) 2 else 1))
    fun movePage(delta: Int) = render(_state.value.currentPage + delta)
    fun toggleChrome() = _state.update { it.copy(chromeVisible = !it.chromeVisible) }
    fun dismissError() = _state.update { it.copy(errorKey = null) }
    fun search(query: String) {
        searchJob?.cancel()
        searchJob = viewModelScope.launch {
            _state.update { it.copy(searchQuery = query) }
            val source = _state.value.source ?: return@launch
            val results = source.search(query)
            ensureActive()
            _state.update { 
                it.copy(
                    searchResults = results, 
                    currentSearchIndex = if (results.isNotEmpty()) 0 else -1, 
                    errorKey = if (query.isNotBlank() && results.isEmpty()) "no_results" else null
                ) 
            }
            results.firstOrNull()?.let { render(it.pageIndex) }
        }
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
    fun reset() = viewModelScope.launch {
        preferences.reset()
        closeDocument()
        SamplePdfRepository(getApplication()).deleteAllDownloadedPdfs()
        _state.value = ViewerUiState()
    }
    fun closeDocument() {
        val previousRender = renderJob
        val previousSearch = searchJob
        val previousSource = _state.value.source
        previousRender?.cancel()
        previousSearch?.cancel()
        // Native PDF rendering can still be in flight after cancel(). Do not close
        // its document until both jobs finish. This cleanup must outlive onCleared.
        if (previousSource != null) CoroutineScope(Dispatchers.IO).launch {
            previousRender?.join()
            previousSearch?.join()
            runCatching { previousSource.close() }
        }
        val cleared = _state.value.settings.copy(lastUri = null, lastPage = 0)
        _state.update { it.copy(source = null, bitmap = null, secondBitmap = null, currentPage = 0, passwordRequested = false, settings = cleared) }
        
        synchronized(bitmapCache) {
            bitmapCache.clear()
        }

        viewModelScope.launch { preferences.save(cleared) }
    }
    override fun onCleared() { closeDocument(); billing.close() }
}
