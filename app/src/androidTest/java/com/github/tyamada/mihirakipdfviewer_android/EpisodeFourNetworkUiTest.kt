package com.github.tyamada.mihirakipdfviewer_android

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.test.platform.app.InstrumentationRegistry
import com.github.tyamada.mihirakipdfviewer_android.data.SamplePdfRepository
import kotlinx.coroutines.runBlocking
import org.json.JSONObject
import org.junit.Assert.*
import org.junit.Assume.assumeTrue
import org.junit.Rule
import org.junit.Test
import java.io.File

/** Opt-in live-network test; run only in a disposable emulator, never against user data. */
class EpisodeFourNetworkUiTest {
    @get:Rule val compose = createAndroidComposeRule<MainActivity>()
    @Test fun refreshJapaneseListDownloadVerifyAndOpenEpisodeFour() {
        assumeTrue(InstrumentationRegistry.getArguments().getString("episodeFourNetwork") == "true")
        val context = compose.activity
        compose.waitForIdle()
        val skip = compose.onAllNodesWithText(context.getString(R.string.skip))
        if (skip.fetchSemanticsNodes().isNotEmpty()) skip[0].performClick()
        compose.waitUntil(60_000) { !context.viewer.state.value.loading }
        InstrumentationRegistry.getInstrumentation().runOnMainSync {
            if (!context.viewer.state.value.chromeVisible) context.viewer.toggleChrome()
        }
        compose.onNodeWithTag("btn_top_sample_pdfs").performClick()
        compose.onNodeWithTag("sample_language_filter").performClick()
        compose.waitUntil(60_000) {
            compose.onAllNodesWithTag("sample_language_ja").fetchSemanticsNodes().isNotEmpty()
        }
        compose.onNodeWithTag("sample_language_ja").performScrollTo().performClick()
        compose.waitUntil(60_000) {
            compose.onAllNodesWithTag("sample_pdf_ep4_ja").fetchSemanticsNodes().isNotEmpty()
        }
        compose.onNodeWithText("ためし部 第4話 風と、なかよく。").assertExists()
        val card = hasAnyAncestor(hasTestTag("sample_pdf_ep4_ja"))
        if (compose.onAllNodes(hasTestTag("btn_open") and card).fetchSemanticsNodes().isEmpty())
            compose.onNode(hasTestTag("btn_download") and card).performClick()
        compose.waitUntil(300_000) {
            compose.onAllNodes(hasTestTag("btn_open") and card).fetchSemanticsNodes().isNotEmpty()
        }
        val repository = SamplePdfRepository(context)
        runBlocking { repository.loadCatalog() }
        val item = repository.catalogItems.value.single { it.id == "ep4_ja" }
        val file = requireNotNull(repository.getDownloadedFile(item))
        assertEquals(99981486L, file.length())
        val hash = file.inputStream().use { input ->
            val digest = java.security.MessageDigest.getInstance("SHA-256")
            val buffer = ByteArray(65536)
            while (true) { val n = input.read(buffer); if (n < 0) break; digest.update(buffer, 0, n) }
            digest.digest().joinToString("") { "%02x".format(it) }
        }
        assertEquals("7c4bde39dc25800aa097854c6e47f31934abe12b3a05d97450fb73523af3c3dc", hash)
        compose.onNode(hasTestTag("btn_open") and card).performClick()
        compose.waitUntil(90_000) {
            check(context.viewer.state.value.errorKey == null) { context.viewer.state.value.errorKey.toString() }
            context.viewer.state.value.pageCount == 8 && context.viewer.state.value.bitmap != null
        }
        val viewer = context.viewer
        InstrumentationRegistry.getInstrumentation().runOnMainSync {
            context.requestedOrientation = android.content.pm.ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
        }
        compose.waitUntil(30_000) { !viewer.state.value.isPortrait }
        InstrumentationRegistry.getInstrumentation().runOnMainSync { viewer.render(1) }
        compose.waitUntil(90_000) {
            !viewer.state.value.loading && viewer.state.value.currentPage == 1 &&
                viewer.state.value.bitmap != null && viewer.state.value.secondBitmap != null
        }
        val state = viewer.state.value
        compose.waitForIdle()
        val screenshot = InstrumentationRegistry.getInstrumentation().uiAutomation.takeScreenshot()
        File(context.getExternalFilesDir(null), "episode-four-landscape.png").outputStream().use {
            screenshot.compress(android.graphics.Bitmap.CompressFormat.PNG, 100, it)
        }
        File(context.getExternalFilesDir(null), "episode-four-ui.json").writeText(JSONObject()
            .put("sample_id", item.id).put("catalog_count", repository.catalogItems.value.size)
            .put("url", item.url).put("bytes", file.length()).put("sha256", hash)
            .put("pdf_page_count", state.pageCount).put("rendered", state.bitmap != null)
            .put("effective_layout", state.effectiveLayout.name)
            .put("two_pages_rendered", state.bitmap != null && state.secondBitmap != null)
            .put("current_page", state.currentPage).put("settings", state.settings.toString())
            .put("passed", true).toString(2))
    }
}
