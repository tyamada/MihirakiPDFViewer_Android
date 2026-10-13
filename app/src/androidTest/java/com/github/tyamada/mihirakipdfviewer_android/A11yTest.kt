package com.github.tyamada.mihirakipdfviewer_android

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.test.espresso.accessibility.AccessibilityChecks
import androidx.test.ext.junit.runners.AndroidJUnit4
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
        // Verify settings button content description exists and is accessible
        compose.onNodeWithContentDescription(compose.activity.getString(R.string.settings)).assertExists()
        compose.onNodeWithContentDescription(compose.activity.getString(R.string.settings)).assertHasClickAction()

        // Verify sample PDFs button content description exists
        compose.onNodeWithContentDescription(compose.activity.getString(R.string.sample_pdfs)).assertExists()
        compose.onNodeWithContentDescription(compose.activity.getString(R.string.sample_pdfs)).assertHasClickAction()
    }
}
