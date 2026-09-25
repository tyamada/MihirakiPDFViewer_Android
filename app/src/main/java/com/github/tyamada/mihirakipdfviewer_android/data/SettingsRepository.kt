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
        val purchasedTier = stringPreferencesKey("purchased_tier")
        val purchasedTiers = stringSetPreferencesKey("purchased_tiers")
        val lastUri = stringPreferencesKey("last_uri")
        val lastPage = intPreferencesKey("last_page")
    }
    val settings: Flow<ViewerSettings> = context.dataStore.data.map { p ->
        val tiersSet = p[Keys.purchasedTiers] ?: p[Keys.purchasedTier]?.let { setOf(it) } ?: emptySet()
        ViewerSettings(
            enumOr(p[Keys.direction], ReadingDirection.L2R), enumOr(p[Keys.coverMode], CoverMode.STANDARD),
            enumOr(p[Keys.layout], ViewerLayout.SINGLE), p[Keys.cover] ?: false,
            p[Keys.highQuality] ?: false, p[Keys.sharpness] ?: 0f,
            tiersSet, p[Keys.purchasedTier], p[Keys.lastUri], p[Keys.lastPage] ?: 0,
        )
    }
    suspend fun save(s: ViewerSettings) = context.dataStore.edit { p ->
        p[Keys.direction] = s.direction.name; p[Keys.coverMode] = s.coverMode.name; p[Keys.layout] = s.layout.name
        p[Keys.cover] = s.showCover; p[Keys.highQuality] = s.highQuality; p[Keys.sharpness] = s.sharpness
        p[Keys.purchasedTiers] = s.allPurchasedTiers
        s.purchasedTier?.let { p[Keys.purchasedTier] = it }
        if (s.lastUri != null) p[Keys.lastUri] = s.lastUri else p.remove(Keys.lastUri)
        p[Keys.lastPage] = s.lastPage
    }
    suspend fun reset() = context.dataStore.edit { it.clear() }
    private inline fun <reified T : Enum<T>> enumOr(value: String?, fallback: T) = runCatching { enumValueOf<T>(value!!) }.getOrDefault(fallback)
}
