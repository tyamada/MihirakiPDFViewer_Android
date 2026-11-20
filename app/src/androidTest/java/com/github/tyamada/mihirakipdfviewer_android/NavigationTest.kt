package com.github.tyamada.mihirakipdfviewer_android

import android.net.Uri
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.uiautomator.UiDevice
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import java.io.File

@RunWith(AndroidJUnit4::class)
class NavigationTest {
    @get:Rule val compose = createAndroidComposeRule<MainActivity>()
    // General search/password fixtures remain bundled; manga inputs are external and pinned.
    private fun copyAssetToCache(name: String): File {
        require(!name.startsWith("tameshibu")) { "Use VerifiedSampleInputs for manga PDFs" }
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        return File(context.cacheDir, name).also { target ->
            InstrumentationRegistry.getInstrumentation().context.assets.open(name).use { input ->
                target.outputStream().use { output -> input.copyTo(output) }
            }
        }
    }

    @Test fun launchShowsFilePickerEntry() { compose.onNodeWithText(compose.activity.getString(R.string.open_pdf)).assertExists() }
    @Test fun fileSelectionRouteIsClickable() { compose.onNodeWithText(compose.activity.getString(R.string.open_pdf)).assertHasClickAction() }
    @Test fun settingsHelpAndResetRoutes() {
        compose.onNodeWithContentDescription(compose.activity.getString(R.string.settings)).performClick()
        compose.onNodeWithText(compose.activity.getString(R.string.settings)).performClick()
        compose.onNodeWithText(compose.activity.getString(R.string.display_settings)).assertExists()
        
        // Scroll to and click Reset
        compose.onNodeWithText(compose.activity.getString(R.string.reset)).performScrollTo().performClick()
        compose.onNodeWithText(compose.activity.getString(R.string.reset_message)).assertExists()
    }


    @Test
    fun captureAllStoreScreenshotsJa() {
        val device = UiDevice.getInstance(InstrumentationRegistry.getInstrumentation())
        val outputDir = File("/sdcard/Download")
        outputDir.mkdirs()

        // 4. Settings screen (At initial launch before opening PDF)
        compose.onNodeWithContentDescription(compose.activity.getString(R.string.settings)).performClick()
        compose.onNodeWithText(compose.activity.getString(R.string.settings)).performClick()
        compose.onNodeWithText(compose.activity.getString(R.string.display_settings)).assertExists()
        compose.waitForIdle()
        Thread.sleep(1500)
        device.takeScreenshot(File(outputDir, "4_smartphone_screenshot_settings_ja.png"))

        // 5. Support / Help detail screen
        compose.onNodeWithContentDescription(compose.activity.getString(R.string.settings)).performClick()
        compose.onNodeWithText(compose.activity.getString(R.string.help)).performClick()
        compose.waitForIdle()
        Thread.sleep(1500)
        device.takeScreenshot(File(outputDir, "5_smartphone_screenshot_support_ja.png"))

        // Back to main
        compose.onNodeWithContentDescription(compose.activity.getString(R.string.back)).performClick()
        compose.waitForIdle()

        // 1. View / Spread view (pinned external Japanese PDF)
        val pdfFile = VerifiedSampleInputs.file("ep1_ja")
        val uri = Uri.fromFile(pdfFile)
        InstrumentationRegistry.getInstrumentation().runOnMainSync {
            compose.activity.viewer.open(uri)
        }
        compose.waitUntil(20000) {
            compose.activity.viewer.state.value.bitmap != null && compose.activity.viewer.state.value.errorKey == null
        }
        compose.waitForIdle()
        Thread.sleep(2000)
        device.takeScreenshot(File(outputDir, "1_smartphone_screenshot_view_ja.png"))

        // 2. Search screen (search_test_600_hits_jp.pdf)
        val searchPdf = copyAssetToCache("search_test_600_hits_jp.pdf")
        val searchUri = Uri.fromFile(searchPdf)
        InstrumentationRegistry.getInstrumentation().runOnMainSync {
            compose.activity.viewer.open(searchUri)
            compose.activity.viewer.search("1")
        }
        compose.waitUntil(20000) {
            compose.activity.viewer.state.value.bitmap != null && compose.activity.viewer.state.value.errorKey == null
        }
        compose.waitForIdle()
        Thread.sleep(3000)
        device.takeScreenshot(File(outputDir, "2_smartphone_screenshot_search_ja.png"))

        // 3. Password screen (R2L_Cover_password.pdf)
        val pwdPdf = copyAssetToCache("R2L_Cover_password.pdf")
        val pwdUri = Uri.fromFile(pwdPdf)
        InstrumentationRegistry.getInstrumentation().runOnMainSync {
            compose.activity.viewer.open(pwdUri)
        }
        Thread.sleep(2000)
        device.takeScreenshot(File(outputDir, "3_smartphone_screenshot_password_ja.png"))
    }

    @Test
    fun captureAllStoreScreenshotsEn() {
        val device = UiDevice.getInstance(InstrumentationRegistry.getInstrumentation())
        val outputDir = File("/sdcard/Download")
        outputDir.mkdirs()

        // 4. Settings screen (At initial launch before opening PDF)
        compose.onNodeWithContentDescription(compose.activity.getString(R.string.settings)).performClick()
        compose.onNodeWithText(compose.activity.getString(R.string.settings)).performClick()
        compose.onNodeWithText(compose.activity.getString(R.string.display_settings)).assertExists()
        compose.waitForIdle()
        Thread.sleep(1500)
        device.takeScreenshot(File(outputDir, "4_smartphone_screenshot_settings_en.png"))

        // 5. Support / Help detail screen
        compose.onNodeWithContentDescription(compose.activity.getString(R.string.settings)).performClick()
        compose.onNodeWithText(compose.activity.getString(R.string.help)).performClick()
        compose.waitForIdle()
        Thread.sleep(1500)
        device.takeScreenshot(File(outputDir, "5_smartphone_screenshot_support_en.png"))

        // Back to main
        compose.onNodeWithContentDescription(compose.activity.getString(R.string.back)).performClick()
        compose.waitForIdle()

        // 1. View / Spread view (pinned external English PDF)
        val pdfFile = VerifiedSampleInputs.file("ep1_en")
        val uri = Uri.fromFile(pdfFile)
        InstrumentationRegistry.getInstrumentation().runOnMainSync {
            compose.activity.viewer.open(uri)
        }
        compose.waitUntil(20000) {
            compose.activity.viewer.state.value.bitmap != null && compose.activity.viewer.state.value.errorKey == null
        }
        compose.waitForIdle()
        Thread.sleep(2000)
        device.takeScreenshot(File(outputDir, "1_smartphone_screenshot_view_en.png"))

        // 2. Search screen (search_test_600_hits_en.pdf)
        val searchPdf = copyAssetToCache("search_test_600_hits_en.pdf")
        val searchUri = Uri.fromFile(searchPdf)
        InstrumentationRegistry.getInstrumentation().runOnMainSync {
            compose.activity.viewer.open(searchUri)
            compose.activity.viewer.search("1")
        }
        compose.waitUntil(20000) {
            compose.activity.viewer.state.value.bitmap != null && compose.activity.viewer.state.value.errorKey == null
        }
        compose.waitForIdle()
        Thread.sleep(3000)
        device.takeScreenshot(File(outputDir, "2_smartphone_screenshot_search_en.png"))

        // 3. Password screen (R2L_Cover_password.pdf)
        val pwdPdf = copyAssetToCache("R2L_Cover_password.pdf")
        val pwdUri = Uri.fromFile(pwdPdf)
        InstrumentationRegistry.getInstrumentation().runOnMainSync {
            compose.activity.viewer.open(pwdUri)
        }
        Thread.sleep(2000)
        device.takeScreenshot(File(outputDir, "3_smartphone_screenshot_password_en.png"))
    }
}
