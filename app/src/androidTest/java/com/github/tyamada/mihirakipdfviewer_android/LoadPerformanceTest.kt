package com.github.tyamada.mihirakipdfviewer_android

import android.net.Uri
import android.util.Log
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.test.platform.app.InstrumentationRegistry
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import java.io.File
import java.io.FileOutputStream

class LoadPerformanceTest {
    @get:Rule val compose = createAndroidComposeRule<MainActivity>()

    @Before fun setUp() {
        InstrumentationRegistry.getInstrumentation().runOnMainSync {
            compose.activity.viewer.reset()
        }
        compose.waitForIdle()
    }

    private fun copyAssetToCache(fileName: String): File {
        val targetContext = InstrumentationRegistry.getInstrumentation().targetContext
        val testContext = InstrumentationRegistry.getInstrumentation().context
        val file = File(targetContext.cacheDir, fileName)
        testContext.assets.open("testdata/$fileName").use { input ->
            FileOutputStream(file).use { output -> input.copyTo(output) }
        }
        return file
    }

    @Test fun testLoadPerformance1100Pages() {
        val fileName = "load_test_1100_pages.pdf"
        val file = copyAssetToCache(fileName)
        val uri = Uri.fromFile(file)

        val startTime = System.currentTimeMillis()
        InstrumentationRegistry.getInstrumentation().runOnMainSync {
            compose.activity.viewer.open(uri)
        }

        compose.waitUntil(15000) { 
            compose.activity.viewer.state.value.source != null 
        }
        val loadTime = System.currentTimeMillis() - startTime

        Log.d("LoadPerformanceTest", "Loaded $fileName in ${loadTime}ms")
        assertTrue("Load time was non-positive", loadTime > 0)
    }

    @Test fun testSearchPerformance600Hits() {
        val fileName = "search_test_600_hits_en.pdf"
        val file = copyAssetToCache(fileName)
        val uri = Uri.fromFile(file)

        InstrumentationRegistry.getInstrumentation().runOnMainSync {
            compose.activity.viewer.open(uri)
        }
        compose.waitUntil(10000) { compose.activity.viewer.state.value.source != null }

        val startTime = System.currentTimeMillis()
        InstrumentationRegistry.getInstrumentation().runOnMainSync {
            compose.activity.viewer.search("the")
        }

        compose.waitUntil(15000) {
            compose.activity.viewer.state.value.searchResults.isNotEmpty()
        }
        val searchTime = System.currentTimeMillis() - startTime

        Log.d("LoadPerformanceTest", "Search completed in ${searchTime}ms with ${compose.activity.viewer.state.value.searchResults.size} hits")
        assertTrue("Search time was non-positive", searchTime > 0)
    }
}
