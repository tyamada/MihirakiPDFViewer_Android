package com.github.tyamada.mihirakipdfviewer_android

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class NavigationTest {
    @get:Rule val compose = createAndroidComposeRule<MainActivity>()
    @Test fun launchShowsFilePickerEntry() { compose.onNodeWithText(compose.activity.getString(R.string.open_pdf)).assertExists() }
    @Test fun fileSelectionRouteIsClickable() { compose.onNodeWithText(compose.activity.getString(R.string.open_pdf)).assertHasClickAction() }
    @Test fun settingsHelpAndResetRoutes() {
        compose.onNodeWithContentDescription(compose.activity.getString(R.string.settings)).performClick()
        compose.onNodeWithText(compose.activity.getString(R.string.display_settings)).assertExists()
        
        // Scroll to and click Help
        compose.onNodeWithText(compose.activity.getString(R.string.help)).performScrollTo().performClick()
        compose.onNodeWithText(compose.activity.getString(R.string.help)).assertExists()
        compose.onNodeWithContentDescription(compose.activity.getString(R.string.back)).performClick()
        
        // Scroll to and click Reset
        compose.onNodeWithText(compose.activity.getString(R.string.reset)).performScrollTo().performClick()
        compose.onNodeWithText(compose.activity.getString(R.string.reset_message)).assertExists()
    }
}
