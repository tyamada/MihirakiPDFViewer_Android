package com.github.tyamada.mihirakipdfviewer_android

import android.graphics.Bitmap
import android.net.Uri
import androidx.compose.ui.graphics.asAndroidBitmap
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.uiautomator.UiDevice
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import java.io.File
import java.io.FileOutputStream
import java.util.Locale

@RunWith(AndroidJUnit4::class)
class StoreScreenshotTest {

    @get:Rule
    val compose = createAndroidComposeRule<MainActivity>()

    private val device = UiDevice.getInstance(InstrumentationRegistry.getInstrumentation())

    @Test
    fun captureStoreScreenshots() {
        compose.waitForIdle()
        Thread.sleep(3000)

        // 1. 01_library_or_cover (PDF未オープンの初期ライブラリ/カバー画面)
        compose.waitUntil(10000) {
            compose.activity.viewer.state.value.source == null
        }
        Thread.sleep(1500)
        takeScreenshot("01_library_or_cover")

        // Determine language suffix based on current device locale
        val lang = Locale.getDefault().language.lowercase()
        val region = Locale.getDefault().country.lowercase()
        val fileLang = if (lang == "zh" && region == "cn") "zh_cn" else lang
        val pdfName = when (fileLang) {
            "ja" -> "tameshibu_episode1_2_ja.pdf"
            "en" -> "tameshibu_episode1_2_en.pdf"
            "ko" -> "tameshibu_episode1_2_ko.pdf"
            "zh_cn" -> "tameshibu_episode1_2_zh_cn.pdf"
            else -> "tameshibu_episode1_2_en.pdf"
        }

        // 2. 02_two_page_spread (PDF見開き表示画面)
        val pdfFile = copyAssetToCache(pdfName)
        val uri = Uri.fromFile(pdfFile)
        InstrumentationRegistry.getInstrumentation().runOnMainSync {
            compose.activity.viewer.open(uri)
        }
        compose.waitUntil(20000) {
            val state = compose.activity.viewer.state.value
            if (state.errorKey != null) {
                throw AssertionError("FATAL: PDF failed to open in test with errorKey='${state.errorKey}'")
            }
            state.source != null
        }
        compose.waitForIdle()
        Thread.sleep(4000) // Allow time for page rendering & layout
        takeScreenshot("02_two_page_spread")

        // 3. 03_thumbnail_view (ページめくり状態 - 次のページへ移動)
        InstrumentationRegistry.getInstrumentation().runOnMainSync {
            compose.activity.viewer.movePage(1)
        }
        compose.waitUntil(20000) {
            val state = compose.activity.viewer.state.value
            if (state.errorKey != null) {
                throw AssertionError("FATAL: PDF navigation failed with errorKey='${state.errorKey}'")
            }
            state.source != null
        }
        compose.waitForIdle()
        Thread.sleep(2500)
        takeScreenshot("03_thumbnail_view")

        // 4. 04_text_search (テキスト検索画面 - キーワード: 'ひと息マップ')
        InstrumentationRegistry.getInstrumentation().runOnMainSync {
            compose.activity.viewer.search("ひと息マップ")
        }
        compose.waitUntil(20000) {
            val state = compose.activity.viewer.state.value
            if (state.errorKey != null) {
                throw AssertionError("FATAL: PDF search failed with errorKey='${state.errorKey}'")
            }
            state.source != null
        }
        compose.waitForIdle()
        Thread.sleep(3000)
        takeScreenshot("04_text_search")

        // 5. 05_settings (設定画面 - 最後に設定ボタンをクリックして撮影、戻らない)
        runCatching {
            device.click(device.displayWidth / 2, device.displayHeight / 2)
            compose.waitForIdle()
            Thread.sleep(1000)

            compose.onNode(hasTestTag("btn_settings")).performClick()
            compose.waitForIdle()
            Thread.sleep(2500)
        }
        takeScreenshot("05_settings")
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

    private fun takeScreenshot(name: String) {
        val bitmap = compose.onRoot().captureToImage().asAndroidBitmap()
        val targetContext = InstrumentationRegistry.getInstrumentation().targetContext
        val filesDir = targetContext.getExternalFilesDir(null) ?: targetContext.cacheDir
        if (!filesDir.exists()) {
            filesDir.mkdirs()
        }
        val path = File(filesDir, "$name.png")
        val fos = FileOutputStream(path)
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, fos)
        fos.flush()
        fos.close()
        println("Captured Compose screenshot to external files dir: ${path.absolutePath}")
    }
}
