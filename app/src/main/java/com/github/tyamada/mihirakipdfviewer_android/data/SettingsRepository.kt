package com.github.tyamada.mihirakipdfviewer_android.data

import android.content.Context
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore by preferencesDataStore("viewer_settings")

class SettingsRepository(private val context: Context) {
    private object Keys {
        val direction = stringPreferencesKey("direction"); val coverMode = stringPreferencesKey("cover_mode")
        val layout = stringPreferencesKey("layout"); val cover = booleanPreferencesKey("cover")
        val highQuality = booleanPreferencesKey("high_quality"); val sharpness = floatPreferencesKey("sharpness")
        val mouseNavigation = booleanPreferencesKey("mouse_navigation")
        val portraitSpread = booleanPreferencesKey("portrait_spread")
        val samplePdfSortOrder = stringPreferencesKey("sample_pdf_sort_order")
        val purchasedTier = stringPreferencesKey("purchased_tier")
        val purchasedTiers = stringSetPreferencesKey("purchased_tiers")
        val lastUri = stringPreferencesKey("last_uri")
        val lastPage = intPreferencesKey("last_page")
        val showTutorialOnStartup = booleanPreferencesKey("show_tutorial_on_startup")
    }
    val settings: Flow<ViewerSettings> = context.dataStore.data.map { p ->
        val tiersSet = p[Keys.purchasedTiers] ?: p[Keys.purchasedTier]?.let { setOf(it) } ?: emptySet()
        val isDefaultDesktop = !context.packageManager.hasSystemFeature(android.content.pm.PackageManager.FEATURE_TOUCHSCREEN) ||
            context.resources.configuration.touchscreen == android.content.res.Configuration.TOUCHSCREEN_NOTOUCH
        ViewerSettings(
            enumOr(p[Keys.direction], ReadingDirection.L2R), enumOr(p[Keys.coverMode], CoverMode.STANDARD),
            enumOr(p[Keys.layout], ViewerLayout.SINGLE), p[Keys.cover] ?: false,
            p[Keys.highQuality] ?: false, p[Keys.sharpness] ?: 0f,
            p[Keys.mouseNavigation] ?: isDefaultDesktop,
            p[Keys.portraitSpread] ?: true,
            enumOr(p[Keys.samplePdfSortOrder], SamplePdfSortOrder.DESC),
            tiersSet, p[Keys.purchasedTier], p[Keys.lastUri], p[Keys.lastPage] ?: 0,
            p[Keys.showTutorialOnStartup] ?: true,
        )
    }
    suspend fun save(s: ViewerSettings) = context.dataStore.edit { p ->
        p[Keys.direction] = s.direction.name; p[Keys.coverMode] = s.coverMode.name; p[Keys.layout] = s.layout.name
        p[Keys.cover] = s.showCover; p[Keys.highQuality] = s.highQuality; p[Keys.sharpness] = s.sharpness
        p[Keys.mouseNavigation] = s.mouseNavigation; p[Keys.portraitSpread] = s.portraitSpread
        p[Keys.purchasedTiers] = s.allPurchasedTiers
        s.purchasedTier?.let { p[Keys.purchasedTier] = it }
        if (s.lastUri != null) p[Keys.lastUri] = s.lastUri else p.remove(Keys.lastUri)
        p[Keys.lastPage] = s.lastPage
        p[Keys.showTutorialOnStartup] = s.showTutorialOnStartup
    }
    // Sorting belongs to the sample screen; stale viewer snapshots must not overwrite it.
    suspend fun toggleSamplePdfSortOrder() = context.dataStore.edit { p ->
        val current = enumOr(p[Keys.samplePdfSortOrder], SamplePdfSortOrder.DESC)
        p[Keys.samplePdfSortOrder] = if (current == SamplePdfSortOrder.DESC) {
            SamplePdfSortOrder.ASC.name
        } else {
            SamplePdfSortOrder.DESC.name
        }
    }
    suspend fun reset() = context.dataStore.edit { it.clear() }
    private inline fun <reified T : Enum<T>> enumOr(value: String?, fallback: T) = runCatching { enumValueOf<T>(value!!) }.getOrDefault(fallback)
}
