package com.github.tyamada.mihirakipdfviewer_android

import android.content.res.Configuration
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.v2.createAndroidComposeRule
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.dp
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.uiautomator.UiDevice
import com.github.tyamada.mihirakipdfviewer_android.ui.screens.TipScreen
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import java.util.Locale

@RunWith(AndroidJUnit4::class)
class TipScreenLargeTextTest {
    @get:Rule val compose = createAndroidComposeRule<MainActivity>()

    @Test fun japaneseAtDoubleFontScrollsToLastTierAndBack() = verify("ja", 2f)
    @Test fun englishAtDoubleFontScrollsToLastTierAndBack() = verify("en", 2f)
    @Test fun koreanAtDoubleFontScrollsToLastTierAndBack() = verify("ko", 2f)
    @Test fun japaneseAtNormalFontScrollsToLastTierAndBack() = verify("ja", 1f)

    private fun verify(language: String, fontScale: Float) {
        val activity = compose.activity
        val config = Configuration(activity.resources.configuration).apply { setLocale(Locale.forLanguageTag(language)) }
        val context = activity.createConfigurationContext(config)
        compose.runOnUiThread {
            activity.setContent {
                val density = LocalDensity.current.density
                CompositionLocalProvider(
                    LocalContext provides context,
                    LocalConfiguration provides config,
                    LocalDensity provides Density(density, fontScale)
                ) {
                    MaterialTheme {
                        Box(Modifier.requiredSize(320.dp, 480.dp)) {
                            TipScreen(activity.viewer) { }
                        }
                    }
                }
            }
        }
        val lastTitle = context.getString(R.string.koyomi_badge_title)
        val title = compose.onNodeWithText(lastTitle)
        title.performScrollTo().assertIsDisplayed()
        // No purchase is performed: reachability and bounds are the regression target.
        compose.onAllNodes((hasText(context.getString(R.string.purchase)) or hasText(context.getString(R.string.purchased))) and hasClickAction())
            .onLast().performScrollTo().assertIsDisplayed().assertWidthIsAtLeast(200.dp)
        val device = UiDevice.getInstance(InstrumentationRegistry.getInstrumentation())
        device.executeShellCommand("screencap -p /sdcard/Download/viewer-support-$language-$fontScale.png")
        compose.onNodeWithText(context.getString(R.string.tip_message)).performScrollTo().assertIsDisplayed()
        compose.onNodeWithContentDescription(context.getString(R.string.back)).assertIsDisplayed().assertHasClickAction()
    }
}
