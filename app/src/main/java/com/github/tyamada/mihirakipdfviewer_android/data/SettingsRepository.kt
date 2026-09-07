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
    }
    val settings: Flow<ViewerSettings> = context.dataStore.data.map { p ->
        ViewerSettings(
            enumOr(p[Keys.direction], ReadingDirection.L2R), enumOr(p[Keys.coverMode], CoverMode.STANDARD),
            enumOr(p[Keys.layout], ViewerLayout.SINGLE), p[Keys.cover] ?: false,
            p[Keys.highQuality] ?: false, p[Keys.sharpness] ?: 0f,
            p[Keys.purchasedTier],
        )
    }
    suspend fun save(s: ViewerSettings) = context.dataStore.edit { p ->
        p[Keys.direction] = s.direction.name; p[Keys.coverMode] = s.coverMode.name; p[Keys.layout] = s.layout.name
        p[Keys.cover] = s.showCover; p[Keys.highQuality] = s.highQuality; p[Keys.sharpness] = s.sharpness
        s.purchasedTier?.let { p[Keys.purchasedTier] = it }
    }
    suspend fun reset() = context.dataStore.edit { it.clear() }
    private inline fun <reified T : Enum<T>> enumOr(value: String?, fallback: T) = runCatching { enumValueOf<T>(value!!) }.getOrDefault(fallback)
}
