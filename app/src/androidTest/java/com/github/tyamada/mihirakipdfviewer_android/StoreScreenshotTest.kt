package com.github.tyamada.mihirakipdfviewer_android

import android.content.res.Configuration
import android.graphics.Bitmap
import android.graphics.Color
import android.net.Uri
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.uiautomator.UiSelector
import androidx.test.uiautomator.UiDevice
import com.github.tyamada.mihirakipdfviewer_android.data.*
import org.json.JSONObject
import org.junit.Assert.*
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import java.io.File
import java.security.MessageDigest

/** One independently reproducible scene per invocation. The host owns rotation and locale. */
@RunWith(AndroidJUnit4::class)
class StoreScreenshotTest {
    @get:Rule val compose = createAndroidComposeRule<MainActivity>()
    private val instrumentation = InstrumentationRegistry.getInstrumentation()
    private val device = UiDevice.getInstance(instrumentation)
    private val args get() = InstrumentationRegistry.getArguments()
    private val vm get() = compose.activity.viewer
    private fun main(action: () -> Unit) = instrumentation.runOnMainSync(action)
    private fun waitFor(condition: () -> Boolean) {
        compose.waitUntil(60_000) {
            val state = vm.state.value
            check(state.errorKey == null) { "Viewer error: ${state.errorKey}" }
            condition()
        }
        compose.waitForIdle()
    }
    private fun label(id: Int) = compose.activity.getString(id)
    private fun chrome() {
        main { if (!vm.state.value.chromeVisible) vm.toggleChrome() }
        compose.waitForIdle()
    }

    @Test fun captureStoreScreenshots() {
        org.junit.Assume.assumeTrue("Use the screenshot runner to supply capture arguments", args.containsKey("captureLanguage"))
        val lang = requireNotNull(args.getString("captureLanguage"))
        val scene = requireNotNull(args.getString("captureScene"))
        val portrait = args.getString("captureOrientation") == "portrait"
        val locale = compose.activity.resources.configuration.locales[0].toLanguageTag()
        val expectedLocale = requireNotNull(args.getString("captureLocale"))
        val expected = java.util.Locale.forLanguageTag(expectedLocale)
        val actual = java.util.Locale.forLanguageTag(locale)
        assertEquals("System/app language mismatch", expected.language, actual.language)
        assertEquals("System/app country mismatch", expected.country, actual.country)
        val desktop = args.getString("captureDesktop") == "true"
        assertEquals(args.getString("captureVersionCode")!!.toInt(), BuildConfig.VERSION_CODE)
        if (desktop) {
            // Desktop caption belongs to the system, not Compose.
            val maximize = device.findObject(UiSelector().descriptionMatches("(?i).*(maximize|最大化|최대화|最大化窗口).*"))
            if (maximize.waitForExists(3000)) maximize.click()
            device.waitForIdle()
            val bounds = compose.activity.windowManager.currentWindowMetrics.bounds
            if (bounds.width() < device.displayWidth * 0.95) {
                // Desktop API 34 exposes task resizing to the adb shell UID.
                device.executeShellCommand("am task resize ${compose.activity.taskId} 0 0 ${device.displayWidth} ${device.displayHeight}")
                compose.waitUntil(15_000) {
                    compose.activity.windowManager.currentWindowMetrics.bounds.width() >= device.displayWidth * 0.95
                }
            }
        }
        // UiAutomation holds the physical display rotation during the test. Some
        // tablet images revert host-side rotation when the test activity launches.
        val naturalPortrait = if (device.displayRotation % 2 == 0)
            device.displayHeight > device.displayWidth else device.displayWidth > device.displayHeight
        if (!desktop) {
            if (portrait == naturalPortrait) device.setOrientationNatural() else device.setOrientationLeft()
            main { compose.activity.requestedOrientation = if (portrait)
                android.content.pm.ActivityInfo.SCREEN_ORIENTATION_PORTRAIT else android.content.pm.ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE }
        }
        compose.waitUntil(15_000) { portrait == (compose.activity.resources.configuration.orientation == Configuration.ORIENTATION_PORTRAIT) }
        val pdf = File(compose.activity.filesDir, "store_capture/$lang.pdf")
        assertTrue("Verified transferred PDF missing", pdf.isFile)
        val digest = MessageDigest.getInstance("SHA-256").digest(pdf.readBytes()).joinToString("") { "%02x".format(it) }
        assertEquals(args.getString("capturePdfSha256"), digest)
        main { vm.closeDocument() }
        main { vm.open(Uri.fromFile(pdf), 5) }
        waitFor { vm.state.value.pageCount == args.getString("capturePageCount")!!.toInt() && vm.state.value.bitmap != null }
        main { vm.updateSettings { it.copy(direction = ReadingDirection.R2L, layout = ViewerLayout.SPREAD,
            showCover = true, portraitSpread = true, mouseNavigation = false, highQuality = false, sharpness = 0f) } }
        waitFor { vm.state.value.settings.direction == ReadingDirection.R2L && vm.state.value.settings.layout == ViewerLayout.SPREAD }
        // A newly rendered target after settings are committed, rather than a fixed sleep.
        val previous = vm.state.value.bitmap
        main { vm.render(5) }
        waitFor { vm.state.value.bitmap != null && vm.state.value.bitmap !== previous &&
            (if (portrait) vm.state.value.currentPage == 5 else vm.state.value.secondBitmap != null) }
        chrome()
        var query = ""
        var outlineVerified = false
        var desktopInputVerified = false
        fun openSettings() {
            compose.onNodeWithTag("btn_settings").performClick()
            compose.onNodeWithText(label(R.string.settings)).performClick()
        }
        when (scene) {
            "01_reader" -> assertEquals(ViewerLayout.SINGLE, vm.state.value.effectiveLayout)
            "02_spread" -> {
                assertEquals(ViewerLayout.SPREAD, vm.state.value.effectiveLayout)
                main { vm.toggleChrome() }
            }
            "03_reading_settings" -> {
                openSettings()
                compose.onNodeWithText(label(R.string.reading_direction)).performScrollTo().assertIsDisplayed()
                compose.onNodeWithText("R2L").assertIsDisplayed()
            }
            "04_search" -> {
                val candidates = mapOf("ja" to listOf("ひと息マップ", "完成", "今"), "en" to listOf("Break-Time", "arrow"),
                    "ko" to listOf("한숨 돌림 지도", "화살표"), "zh_cn" to listOf("歇口气地图", "箭头"),
                    "zh_tw" to listOf("歇口氣地圖", "箭頭"),
                    "de" to listOf("Pausen", "Pfeil", "Karte"),
                    "fr" to listOf("détente", "flèche"),
                    "es" to listOf("descanso", "flecha", "mapa"),
                    "pt" to listOf("pausa", "seta", "mapa"),
                    "vi" to listOf("nghỉ", "mũi tên", "bản đồ"),
                    "fil" to listOf("pahinga", "palaso", "mapa"),
                    "ne" to listOf("आराम", "तीर", "नक्सा"),
                    "id" to listOf("istirahat", "panah", "peta"),
                    "it" to listOf("mappa", "pause", "frecce")).getValue(lang)
                // Probe the real PDF text engine; a title-only match is insufficient.
                val source = requireNotNull(vm.state.value.source)
                val selected = kotlinx.coroutines.runBlocking {
                    candidates.firstOrNull { candidate -> source.search(candidate).any { hit -> hit.pageIndex == 5 &&
                        // These A5 fixtures have dialogue between y=40 and y=560.
                        hit.rects.any { it.top > 40f && it.bottom < 560f } } }
                }
                query = requireNotNull(selected) { "No page 6 search hit for $lang" }
                main { vm.search(query) }
                waitFor { vm.state.value.searchQuery == query && vm.state.value.searchResults.any { it.pageIndex == 5 } }
                val index = vm.state.value.searchResults.indexOfFirst { it.pageIndex == 5 }
                main { vm.navigateSearch(index - vm.state.value.currentSearchIndex) }
                waitFor { vm.state.value.searchResults[vm.state.value.currentSearchIndex].pageIndex == 5 }
                // Force and await the highlight-bearing render, including on cached devices.
                val before = vm.state.value.bitmap
                main { vm.render(5) }
                waitFor { vm.state.value.bitmap != null && (vm.state.value.bitmap !== before || hasHighlight(vm.state.value.bitmap!!)) }
                assertTrue("Search highlight absent", listOfNotNull(vm.state.value.bitmap, vm.state.value.secondBitmap).any(::hasHighlight))
            }
            "05_page_navigation" -> {
                if (desktop) {
                    main { vm.updateSettings { it.copy(mouseNavigation = true) } }
                    compose.waitForIdle()
                    compose.onNodeWithTag("viewer_content").performSemanticsAction(androidx.compose.ui.semantics.SemanticsActions.RequestFocus) { it() }
                    val start = vm.state.value.currentPage
                    device.pressDPadRight()
                    waitFor { vm.state.value.currentPage != start }
                    val afterKey = vm.state.value.currentPage
                    compose.onNodeWithTag("viewer_content").performMouseInput {
                        click(androidx.compose.ui.geometry.Offset(width * 0.1f, height / 2f))
                    }
                    compose.mainClock.advanceTimeBy(500)
                    waitFor { vm.state.value.currentPage != afterKey }
                    desktopInputVerified = true
                    chrome()
                }
                val before = vm.state.value.bitmap
                main { vm.render(3) }
                waitFor { vm.state.value.bitmap != null && vm.state.value.bitmap !== before && vm.state.value.currentPage == 3 }
            }
            "07_outline" -> {
                waitFor { vm.state.value.outline.isNotEmpty() }
                val first = vm.state.value.outline.first()
                assertEquals(args.getString("captureOutlineFirst"), first.title)
                assertEquals(8, vm.state.value.outline.size)
                assertEquals((0 until 8).toList(), vm.state.value.outline.map { it.pageIndex })
                val destination = vm.state.value.outline[2]
                val expectedPage = if (portrait) destination.pageIndex else {
                    val settings = vm.state.value.settings
                    val spread = SpreadPlanner.plan(8, settings.direction, settings.showCover, settings.coverMode)
                        .first { it.left == destination.pageIndex || it.right == destination.pageIndex }
                    minOf(spread.left ?: Int.MAX_VALUE, spread.right ?: Int.MAX_VALUE)
                }
                compose.onNodeWithTag("btn_outline").performClick()
                compose.onNodeWithText(destination.title).performClick()
                waitFor { vm.state.value.currentPage == expectedPage &&
                    (vm.state.value.bitmap != null || vm.state.value.secondBitmap != null) }
                chrome()
                compose.onNodeWithTag("btn_outline").performClick()
                compose.onNodeWithText(first.title).assertIsDisplayed()
                compose.onNodeWithText(label(R.string.table_of_contents)).assertIsDisplayed()
                outlineVerified = true
            }
            "06_display_settings" -> {
                openSettings()
                compose.onNodeWithText(label(R.string.reset)).performScrollTo().assertIsDisplayed()
                compose.onNodeWithText(label(if (desktop) R.string.mouse_navigation else R.string.portrait_display)).assertIsDisplayed()
            }
            else -> error("Unknown capture scene: $scene")
        }
        compose.waitUntil(15_000) { compose.onAllNodesWithTag("swipe_hint").fetchSemanticsNodes().isEmpty() }
        compose.waitForIdle()
        device.waitForIdle()
        assertEquals(portrait, device.displayHeight > device.displayWidth)
        assertTrue(device.isScreenOn)
        assertEquals(compose.activity.packageName, device.currentPackageName)
        assertEquals(portrait, compose.activity.resources.configuration.orientation == Configuration.ORIENTATION_PORTRAIT)
        assertNull(vm.state.value.errorKey)
        assertFalse(vm.state.value.passwordRequested)
        assertFalse(vm.state.value.loading)
        val bounds = android.graphics.Rect()
        compose.activity.window.decorView.getWindowVisibleDisplayFrame(bounds)
        val maximized = !desktop || (bounds.width() >= device.displayWidth * 0.95 && bounds.height() >= device.displayHeight * 0.80)
        assertTrue("Desktop window not maximized: $bounds", maximized)
        val dir = File(compose.activity.filesDir, "store_capture")
        assertTrue(device.takeScreenshot(File(dir, "${args.getString("captureId")}.png")))
        val state = vm.state.value
        File(dir, "${args.getString("captureId")}.json").writeText(JSONObject().apply {
            put("desktopInputVerified", desktopInputVerified)
            put("outlinePages", org.json.JSONArray(vm.state.value.outline.map { it.pageIndex }))
            put("outlineVerified", outlineVerified); put("maximized", maximized)
            put("activityBounds", bounds.toShortString())
            put("captureId", args.getString("captureId")); put("scene", scene); put("language", lang); put("locale", locale); put("portrait", portrait)
            put("pdfSha256", digest); put("page", state.currentPage + 1); put("pageCount", state.pageCount)
            put("query", query); put("searchResultCount", state.searchResults.size)
            put("page6SearchRects", org.json.JSONArray().apply {
                state.searchResults.filter { it.pageIndex == 5 }.flatMap { it.rects }.forEach {
                    put(JSONObject().put("left", it.left).put("top", it.top).put("right", it.right).put("bottom", it.bottom))
                }
            })
            put("direction", state.settings.direction.name); put("layout", state.effectiveLayout.name)
            put("showCover", state.settings.showCover); put("version", BuildConfig.VERSION_NAME)
            put("versionCode", BuildConfig.VERSION_CODE); put("package", compose.activity.packageName)
        }.toString(2))
    }

    private fun hasHighlight(bitmap: Bitmap): Boolean {
        if (bitmap.isRecycled) return false
        var count = 0
        for (y in 0 until bitmap.height step 2) for (x in 0 until bitmap.width step 2) {
            val p = bitmap.getPixel(x, y)
            if (Color.red(p) > 180 && Color.green(p) < 100 && Color.blue(p) < 100) count++
        }
        return count > 8
    }
}
