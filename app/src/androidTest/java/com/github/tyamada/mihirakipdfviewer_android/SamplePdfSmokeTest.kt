package com.github.tyamada.mihirakipdfviewer_android

import android.content.res.Configuration
import android.net.Uri
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.test.platform.app.InstrumentationRegistry
import com.github.tyamada.mihirakipdfviewer_android.data.SamplePdfCatalog
import com.github.tyamada.mihirakipdfviewer_android.data.ViewerLayout
import org.junit.Assert.*
import org.junit.Rule
import org.junit.Test
import java.io.File
import java.util.Locale

/** Host stages catalog PDFs in files/sample_smoke; no network or store images are changed. */
class SamplePdfSmokeTest {
    @get:Rule val compose = createAndroidComposeRule<MainActivity>()
    private val instrumentation = InstrumentationRegistry.getInstrumentation()
    private fun main(action: () -> Unit) = instrumentation.runOnMainSync(action)
    private val labels = linkedMapOf(
        "es" to "Spanish (Español)", "it" to "Italian (Italiano)",
        "pt" to "Portuguese (Português)", "vi" to "Vietnamese (Tiếng Việt)",
        "id" to "Indonesian (Bahasa Indonesia)", "fil" to "Filipino (Filipino)",
        "ne" to "Nepali (नेपाली)"
    )

    @Test fun addedLanguagesAppearAndFilterToTheirEpisodes() {
        compose.waitUntil(60_000) { !compose.activity.viewer.state.value.loading }
        main { if (!compose.activity.viewer.state.value.chromeVisible) compose.activity.viewer.toggleChrome() }
        compose.onNodeWithTag("btn_top_sample_pdfs").performClick()
        labels.forEach { (language, label) ->
            compose.onNodeWithTag("sample_language_filter").performClick()
            compose.onNodeWithText(label).performScrollTo().performClick()
            val episodes = SamplePdfCatalog.items.filter { it.languageCode == language }
            compose.waitUntil(10_000) { compose.onAllNodesWithText(episodes[0].title).fetchSemanticsNodes().isNotEmpty() }
            episodes.forEach { compose.onNodeWithText(it.title).assertExists() }
            compose.onAllNodesWithTag("btn_download").assertCountEquals(2)
        }
    }

    @Test fun addedLanguagePdfsRenderSearchAndNavigateOutline() {
        org.junit.Assume.assumeTrue("Stage PDFs with the host smoke runner", InstrumentationRegistry.getArguments().getString("sampleSmoke") == "true")
        val queries = mapOf("es" to "mapa", "it" to "mappa", "pt" to "mapa", "vi" to "nghỉ", "fil" to "mapa", "ne" to "आराम", "id" to "peta")
        val viewer = compose.activity.viewer
        SamplePdfCatalog.items.filter { it.languageCode in labels }.forEach { item ->
            val file = File(compose.activity.filesDir, "sample_smoke/${item.fileName}")
            assertTrue(file.toString(), file.isFile)
            main { viewer.closeDocument(); viewer.open(Uri.fromFile(file), 5) }
            compose.waitUntil(60_000) {
                check(viewer.state.value.errorKey == null) { "${item.id}: ${viewer.state.value.errorKey}" }
                viewer.state.value.bitmap != null && viewer.state.value.pageCount == 8
            }
            assertEquals(item.id, 8, viewer.state.value.outline.size)
            assertEquals(item.id, (0 until 8).toList(), viewer.state.value.outline.map { it.pageIndex })
            lateinit var settingsJob: kotlinx.coroutines.Job
            main { settingsJob = viewer.updateSettings { it.copy(layout = ViewerLayout.SINGLE) } }
            kotlinx.coroutines.runBlocking { settingsJob.join() }
            if (item.id.startsWith("ep1_")) {
                val query = queries.getValue(item.languageCode)
                val beforeSearch = viewer.state.value.bitmap
                main { viewer.search(query) }
                compose.waitUntil(60_000) { viewer.state.value.searchQuery == query &&
                    viewer.state.value.searchResults.any { it.pageIndex == 5 } &&
                    viewer.state.value.currentPage == viewer.state.value.searchResults.first().pageIndex &&
                    viewer.state.value.bitmap != null && viewer.state.value.bitmap !== beforeSearch }
                assertTrue(item.id, viewer.state.value.searchResults.filter { it.pageIndex == 5 }.any { it.rects.isNotEmpty() })
            }
            val destination = viewer.state.value.outline[2].pageIndex
            main { viewer.render(destination) }
            compose.waitUntil(60_000) { viewer.state.value.currentPage == destination && viewer.state.value.bitmap != null }
            android.util.Log.i("SamplePdfSmoke", "PASS ${item.id}: render, outline and page navigation")
        }
    }

    @Test fun italianResourcesResolve() {
        val config = Configuration(compose.activity.resources.configuration)
        config.setLocale(Locale.ITALIAN)
        val context = compose.activity.createConfigurationContext(config)
        assertEquals("Cerca nel PDF", context.getString(R.string.search))
        assertEquals("Tabella dei contenuti", context.getString(R.string.table_of_contents))
        assertEquals("Impostazioni", context.getString(R.string.settings))
    }
}
