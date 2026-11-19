package com.github.tyamada.mihirakipdfviewer_android.data

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.*
import java.time.Instant

internal object BadgePreferences {
    val freeKoyomiGranted = booleanPreferencesKey("koyomi_free_granted")
    // Existing keys and IDs are retained; a free grant never writes a purchase transaction.
    val purchasedTiers = stringSetPreferencesKey("purchased_tiers")
    val purchasedTier = stringPreferencesKey("purchased_tier")
}

enum class KoyomiGrantResult { NEWLY_SAVED, ALREADY_SAVED, LEGACY_OWNED, EXPIRED }

object KoyomiBadgePolicy {
    const val BADGE_ID = "KOYOMI"
    const val EXCLUSIVE_DEADLINE_EPOCH_SECONDS = 1830265200L
    fun isEligible(now: Instant): Boolean = now.isBefore(Instant.ofEpochSecond(EXCLUSIVE_DEADLINE_EPOCH_SECONDS))

    fun evaluate(now: Instant, saved: Boolean, purchased: Set<String>): KoyomiGrantResult = when {
        saved -> KoyomiGrantResult.ALREADY_SAVED
        BADGE_ID in purchased -> KoyomiGrantResult.LEGACY_OWNED
        isEligible(now) -> KoyomiGrantResult.NEWLY_SAVED
        else -> KoyomiGrantResult.EXPIRED
    }
}

internal class KoyomiBadgeStore(private val store: DataStore<Preferences>) {
    /** edit returns only after durable storage succeeds. Exceptions never create a notification. */
    suspend fun grant(clock: java.time.Clock = java.time.Clock.systemUTC()): KoyomiGrantResult {
        var result = KoyomiGrantResult.EXPIRED
        store.edit { p ->
            val purchased = p[BadgePreferences.purchasedTiers].orEmpty() + listOfNotNull(p[BadgePreferences.purchasedTier])
            result = KoyomiBadgePolicy.evaluate(clock.instant(), p[BadgePreferences.freeKoyomiGranted] == true, purchased)
            if (result == KoyomiGrantResult.NEWLY_SAVED) p[BadgePreferences.freeKoyomiGranted] = true
        }
        return result
    }
}

/** Reader-settings reset must not delete badges, including old single-key purchase records. */
internal fun resetPreferencesKeepingBadges(p: MutablePreferences) {
    val free = p[BadgePreferences.freeKoyomiGranted]
    val purchased = p[BadgePreferences.purchasedTiers]
    val legacy = p[BadgePreferences.purchasedTier]
    p.clear()
    free?.let { p[BadgePreferences.freeKoyomiGranted] = it }
    purchased?.let { p[BadgePreferences.purchasedTiers] = it }
    legacy?.let { p[BadgePreferences.purchasedTier] = it }
}

/** A pending, manually dismissible notice waits while reading or while a blocking screen is active. */
fun canShowKoyomiNotice(pending: Boolean, route: String?, reading: Boolean, blocked: Boolean): Boolean =
    pending && route != null && route !in setOf("tutorial", "reset") && !reading && !blocked

/** Session-only notice: durable grants are never re-notified in a new process. */
class KoyomiNoticeState {
    private val mutablePending = kotlinx.coroutines.flow.MutableStateFlow(false)
    val pending: kotlinx.coroutines.flow.StateFlow<Boolean> = mutablePending
    fun onGrantResult(result: KoyomiGrantResult) {
        if (result == KoyomiGrantResult.NEWLY_SAVED) mutablePending.value = true
    }
    fun dismiss() { mutablePending.value = false }
}

/** Merge only confirmed old/new purchase IDs; keep even the pre-set migration key. */
internal fun recordPurchasedBadges(p: MutablePreferences, tiers: Set<String>) {
    p[BadgePreferences.purchasedTiers] = p[BadgePreferences.purchasedTiers].orEmpty() +
        listOfNotNull(p[BadgePreferences.purchasedTier]) + tiers
}
