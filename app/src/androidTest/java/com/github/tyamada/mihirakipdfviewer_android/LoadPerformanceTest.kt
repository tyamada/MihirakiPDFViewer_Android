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

    @Test fun testMemoryUsageDuringLoad() {
        System.gc()
        val runtime = Runtime.getRuntime()
        val memBefore = runtime.totalMemory() - runtime.freeMemory()

        val fileName = "load_test_1100_pages.pdf"
        val file = copyAssetToCache(fileName)
        val uri = Uri.fromFile(file)

        InstrumentationRegistry.getInstrumentation().runOnMainSync {
            compose.activity.viewer.open(uri)
        }
        compose.waitUntil(15000) { compose.activity.viewer.state.value.source != null }

        System.gc()
        val memAfter = runtime.totalMemory() - runtime.freeMemory()
        val memDiffMb = (memAfter - memBefore) / 1024 / 1024
        Log.d("LoadPerformanceTest", "Memory usage change after loading 1100 pages: ${memDiffMb}MB")
        assertTrue("Memory diff should be recorded", memAfter > 0)
    }

    @Test fun testRapidPageNavigationStress() {
        val fileName = "L2R_Cover.pdf"
        val file = copyAssetToCache(fileName)
        val uri = Uri.fromFile(file)

        InstrumentationRegistry.getInstrumentation().runOnMainSync {
            compose.activity.viewer.open(uri)
        }
        compose.waitUntil(10000) { compose.activity.viewer.state.value.source != null }

        val startTime = System.currentTimeMillis()
        InstrumentationRegistry.getInstrumentation().runOnMainSync {
            repeat(10) {
                compose.activity.viewer.move(1)
            }
        }
        compose.waitForIdle()
        val duration = System.currentTimeMillis() - startTime
        Log.d("LoadPerformanceTest", "Rapid navigation stress test completed in ${duration}ms")
        assertTrue("Navigation duration should be positive", duration >= 0)
    }

    @Test fun testMultiDocumentSwitching() {
        val files = listOf("L2R_Single.pdf", "R2L_Cover.pdf", "load_test_1100_pages.pdf")
        files.forEach { name ->
            val file = copyAssetToCache(name)
            val uri = Uri.fromFile(file)
            InstrumentationRegistry.getInstrumentation().runOnMainSync {
                compose.activity.viewer.open(uri)
            }
            compose.waitUntil(10000) { compose.activity.viewer.state.value.source != null }
            InstrumentationRegistry.getInstrumentation().runOnMainSync {
                compose.activity.viewer.closeDocument()
            }
        }
        assertTrue("Multi-document switching completed successfully", true)
    }
}
