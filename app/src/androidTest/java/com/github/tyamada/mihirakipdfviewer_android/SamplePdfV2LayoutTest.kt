package com.github.tyamada.mihirakipdfviewer_android

import android.net.Uri
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.test.platform.app.InstrumentationRegistry
import com.github.tyamada.mihirakipdfviewer_android.data.*
import kotlinx.coroutines.runBlocking
import org.junit.Assert.*
import org.junit.Assume.assumeTrue
import org.junit.Rule
import org.junit.Test
import java.io.File

class SamplePdfV2LayoutTest {
    @get:Rule val compose = createAndroidComposeRule<MainActivity>()
    @org.junit.Before fun dismissFirstRunGuide() {
        compose.waitForIdle()
        val skip = compose.onAllNodesWithText(compose.activity.getString(R.string.skip))
        if (skip.fetchSemanticsNodes().isNotEmpty()) skip[0].performClick()
        compose.waitForIdle()
    }

    @Test fun regionalEditionsSearchAndDisplaySingleAndSpread() {
        val fixture = InstrumentationRegistry.getArguments().getString("samplePdfFixtureDir")
        assumeTrue("Supply verified v2 PDFs", fixture != null)
        val instrument = InstrumentationRegistry.getInstrumentation()
        val viewer = compose.activity.viewer
        val initial = viewer.state.value.settings
        fun main(action: () -> Unit) = instrument.runOnMainSync(action)
        val output = File(compose.activity.getExternalFilesDir(null), "v2-qa-results").apply { mkdirs() }
        try {
            listOf("es_419", "pt_pt", "zh_hk").forEach { language ->
                val item = SamplePdfCatalog.items.single { it.id == "ep1_$language" }
                main { viewer.closeDocument(); viewer.open(Uri.fromFile(File(fixture!!, item.fileName))) }
                compose.waitUntil(90_000) { check(viewer.state.value.errorKey == null) { "${item.id}: ${viewer.state.value.errorKey}" }; !viewer.state.value.loading && viewer.state.value.source != null && (viewer.state.value.bitmap != null || viewer.state.value.secondBitmap != null) }
                assertNull(item.id, viewer.state.value.errorKey)
                lateinit var job: kotlinx.coroutines.Job
                main { job=viewer.updateSettings { it.copy(layout=ViewerLayout.SINGLE, portraitSpread=false, showCover=false) } }
                compose.waitUntil(60_000) { job.isCompleted }
                compose.waitUntil(60_000) { viewer.state.value.bitmap != null && viewer.state.value.secondBitmap == null }
                main { if (!viewer.state.value.chromeVisible) viewer.toggleChrome() }
                val query = if(language=="zh_hk") "試試社" else if(language=="pt_pt") "CLUBE" else "CLUB"
                val beforeSearch = viewer.state.value.bitmap
                compose.onNode(hasSetTextAction()).performTextReplacement(query)
                compose.waitUntil(60_000) { viewer.state.value.bitmap !== beforeSearch && viewer.state.value.searchQuery == query && viewer.state.value.searchResults.any { it.pageIndex==0 && it.rects.isNotEmpty() } }
                compose.waitForIdle()
                instrument.uiAutomation.takeScreenshot().let { bitmap -> File(output,"$language-search.png").outputStream().use { bitmap.compress(android.graphics.Bitmap.CompressFormat.PNG,100,it) };bitmap.recycle() }
                main {
                    val ime = compose.activity.getSystemService(android.content.Context.INPUT_METHOD_SERVICE) as android.view.inputmethod.InputMethodManager
                    ime.hideSoftInputFromWindow(compose.activity.window.decorView.windowToken, 0)
                    viewer.search("")
                }
                compose.waitUntil(60_000) { viewer.state.value.searchQuery.isEmpty() && viewer.state.value.searchResults.isEmpty() }
                main { job=viewer.updateSettings { it.copy(layout=ViewerLayout.SPREAD, portraitSpread=false,showCover=false) } }
                compose.waitUntil(60_000) { job.isCompleted }
                main { viewer.render(2,720) }
                compose.waitUntil(60_000) { viewer.state.value.effectiveLayout==ViewerLayout.SPREAD && viewer.state.value.bitmap!=null && viewer.state.value.secondBitmap!=null && viewer.state.value.currentPage==2 }
                compose.waitForIdle()
                instrument.uiAutomation.takeScreenshot().let { bitmap -> File(output,"$language-spread.png").outputStream().use { bitmap.compress(android.graphics.Bitmap.CompressFormat.PNG,100,it) };bitmap.recycle() }
                main { viewer.move(1) }
                compose.waitUntil(60_000) { viewer.state.value.currentPage==4 && viewer.state.value.bitmap!=null && viewer.state.value.secondBitmap!=null }
            }
        } finally {
            main { viewer.closeDocument(); viewer.updateSettings { initial } }
        }
    }
}
