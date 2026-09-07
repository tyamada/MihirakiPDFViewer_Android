package com.github.tyamada.mihirakipdfviewer_android

import android.net.Uri
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.test.platform.app.InstrumentationRegistry
import com.github.tyamada.mihirakipdfviewer_android.data.ReadingDirection
import com.github.tyamada.mihirakipdfviewer_android.data.ViewerLayout
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.Before
import java.io.File
import java.io.FileOutputStream

class MetadataDetectionTest {
    @get:Rule val compose = createAndroidComposeRule<MainActivity>()

    @Before fun setUp() {
        InstrumentationRegistry.getInstrumentation().runOnMainSync {
            compose.activity.viewer.reset()
        }
        compose.waitForIdle()
    }

    private fun openAsset(fileName: String) {
        val targetContext = InstrumentationRegistry.getInstrumentation().targetContext
        val testContext = InstrumentationRegistry.getInstrumentation().context
        val file = File(targetContext.cacheDir, fileName)
        testContext.assets.open("testdata/$fileName").use { input ->
            FileOutputStream(file).use { output -> input.copyTo(output) }
        }
        val uri = Uri.fromFile(file)
        InstrumentationRegistry.getInstrumentation().runOnMainSync {
            compose.activity.viewer.open(uri)
        }
        // Give it some time to load and update state
        compose.waitUntil(5000) { compose.activity.viewer.state.value.source != null }
    }

    private fun assertSettings(direction: ReadingDirection, layout: ViewerLayout, showCover: Boolean) {
        val state = compose.activity.viewer.state.value
        assertEquals("Direction mismatch", direction, state.settings.direction)
        assertEquals("Layout mismatch", layout, state.settings.layout)
        assertEquals("ShowCover mismatch", showCover, state.settings.showCover)
    }

    @Test fun dumpMetadata() {
        val targetContext = InstrumentationRegistry.getInstrumentation().targetContext
        val testContext = InstrumentationRegistry.getInstrumentation().context
        val files = testContext.assets.list("testdata") ?: emptyArray()
        for (fileName in files) {
            if (!fileName.endsWith(".pdf")) continue
            val file = File(targetContext.cacheDir, fileName)
            testContext.assets.open("testdata/$fileName").use { input ->
                FileOutputStream(file).use { output -> input.copyTo(output) }
            }
            val pdfBox = com.tom_roush.pdfbox.pdmodel.PDDocument.load(file)
            val layout = pdfBox.documentCatalog.pageLayout
            val prefs = pdfBox.documentCatalog.viewerPreferences
            val dir = prefs?.cosObject?.getNameAsString("Direction")
            android.util.Log.d("MetadataDump", "$fileName: Layout=$layout, Dir=$dir")
            pdfBox.close()
        }
    }

    @Test fun testSearchHighlighting() {
        val targetContext = InstrumentationRegistry.getInstrumentation().targetContext
        val testContext = InstrumentationRegistry.getInstrumentation().context
        val fileName = "L2R_Cover.pdf" // This test PDF usually has "Page" or "Mihiraki"
        val file = File(targetContext.cacheDir, fileName)
        testContext.assets.open("testdata/$fileName").use { input ->
            FileOutputStream(file).use { output -> input.copyTo(output) }
        }
        val uri = Uri.fromFile(file)
        InstrumentationRegistry.getInstrumentation().runOnMainSync {
            compose.activity.viewer.open(uri)
        }
        compose.waitUntil(5000) { compose.activity.viewer.state.value.source != null }
        
        InstrumentationRegistry.getInstrumentation().runOnMainSync {
            compose.activity.viewer.search("Page")
        }
        compose.waitUntil(5000) { compose.activity.viewer.state.value.searchResults.isNotEmpty() }
        
        val hits = compose.activity.viewer.state.value.searchResults
        android.util.Log.d("SearchTest", "Found ${hits.size} hits for 'Page'")
        hits.forEach { hit ->
            android.util.Log.d("SearchTest", "Page ${hit.pageIndex} has ${hit.rects.size} rects")
        }
        assert(hits.isNotEmpty())
    }
    @Test fun testL2R_NoCover() { openAsset("L2R_NoCover.pdf"); assertSettings(ReadingDirection.L2R, ViewerLayout.SPREAD, false) }
    @Test fun testL2R_SinglePage() { openAsset("L2R_Single.pdf"); assertSettings(ReadingDirection.L2R, ViewerLayout.SINGLE, false) }
    @Test fun testL2R_OneColumn() { openAsset("L2R_OneColumn.pdf"); assertSettings(ReadingDirection.L2R, ViewerLayout.SINGLE, false) }
    @Test fun testL2R_TwoColumnLeft() { openAsset("L2R_TwoColumnLeft.pdf"); assertSettings(ReadingDirection.L2R, ViewerLayout.SPREAD, false) }
    @Test fun testL2R_TwoColumnRight() { openAsset("L2R_TwoColumnRight.pdf"); assertSettings(ReadingDirection.L2R, ViewerLayout.SPREAD, true) }
    @Test fun testL2R_TwoPageLeft() { openAsset("L2R_TwoPageLeft.pdf"); assertSettings(ReadingDirection.L2R, ViewerLayout.SPREAD, false) }
    @Test fun testL2R_TwoPageRight() { openAsset("L2R_TwoPageRight.pdf"); assertSettings(ReadingDirection.L2R, ViewerLayout.SPREAD, true) }

    @Test fun testR2L_Cover() { openAsset("R2L_Cover.pdf"); assertSettings(ReadingDirection.R2L, ViewerLayout.SPREAD, true) }
    @Test fun testR2L_NoCover() { openAsset("R2L_NoCover.pdf"); assertSettings(ReadingDirection.R2L, ViewerLayout.SPREAD, false) }
    @Test fun testR2L_SinglePage() { openAsset("R2L_Single.pdf"); assertSettings(ReadingDirection.R2L, ViewerLayout.SINGLE, false) }
    @Test fun testR2L_OneColumn() { openAsset("R2L_OneColumn.pdf"); assertSettings(ReadingDirection.R2L, ViewerLayout.SINGLE, false) }
    @Test fun testR2L_TwoColumnLeft() { openAsset("R2L_TwoColumnLeft.pdf"); assertSettings(ReadingDirection.R2L, ViewerLayout.SPREAD, false) }
    @Test fun testR2L_TwoColumnRight() { openAsset("R2L_TwoColumnRight.pdf"); assertSettings(ReadingDirection.R2L, ViewerLayout.SPREAD, true) }
    @Test fun testR2L_TwoPageLeft() { openAsset("R2L_TwoPageLeft.pdf"); assertSettings(ReadingDirection.R2L, ViewerLayout.SPREAD, false) }
    @Test fun testR2L_TwoPageRight() { openAsset("R2L_TwoPageRight.pdf"); assertSettings(ReadingDirection.R2L, ViewerLayout.SPREAD, true) }
}
