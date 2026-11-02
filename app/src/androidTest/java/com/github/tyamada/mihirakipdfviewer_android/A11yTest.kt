package com.github.tyamada.mihirakipdfviewer_android

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.test.espresso.accessibility.AccessibilityChecks
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.compose.ui.unit.dp
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class A11yTest {
    companion object {
        init {
            AccessibilityChecks.enable()
                .setRunChecksFromRootView(true)
        }
    }

    @get:Rule val compose = createAndroidComposeRule<MainActivity>()

    @Test
    fun verifyCoreActionContentDescriptions() {
        compose.waitForIdle()
        val skip = compose.onAllNodesWithText(compose.activity.getString(R.string.skip))
        if (skip.fetchSemanticsNodes().isNotEmpty()) skip[0].performClick()
        compose.waitUntil(60_000) { !compose.activity.viewer.state.value.loading }
        androidx.test.platform.app.InstrumentationRegistry.getInstrumentation().runOnMainSync {
            if (!compose.activity.viewer.state.value.chromeVisible) compose.activity.viewer.toggleChrome()
        }
        // Verify settings button content description exists and is accessible
        compose.onNodeWithTag("btn_settings").assertExists()
        compose.onNodeWithTag("btn_settings").assertHasClickAction()

        // Verify sample PDFs button content description exists
        compose.onNodeWithContentDescription(compose.activity.getString(R.string.sample_pdfs)).assertExists()
        compose.onNodeWithContentDescription(compose.activity.getString(R.string.sample_pdfs)).assertHasClickAction()
        compose.onNodeWithTag("btn_top_sample_pdfs").performClick()
        val toggle = compose.onNodeWithTag("btn_toggle_order")
        toggle.assertHasClickAction().assertIsDisplayed()
        toggle.assertHeightIsAtLeast(48.dp)
    }
}
