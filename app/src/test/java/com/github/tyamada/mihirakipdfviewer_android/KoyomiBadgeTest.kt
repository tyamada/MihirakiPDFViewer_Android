package com.github.tyamada.mihirakipdfviewer_android

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.*
import com.github.tyamada.mihirakipdfviewer_android.data.*
import com.github.tyamada.mihirakipdfviewer_android.billing.TipTier
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import org.junit.Assert.*
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import java.io.File
import java.io.IOException
import java.time.*

class KoyomiBadgeTest {
    @get:Rule val temporary = TemporaryFolder()
    private val deadline = Instant.parse("2027-12-31T15:00:00Z")
    private fun clock(time: Instant) = Clock.fixed(time, ZoneOffset.UTC)
    private fun create(file: File, scope: CoroutineScope) = PreferenceDataStoreFactory.create(scope = scope, produceFile = { file })

    @Test fun exclusiveJstDeadlineIsAbsoluteAndIncludesTheLastNanosecondOfDecember31() {
        assertEquals(1830265200L, deadline.epochSecond)
        assertTrue(KoyomiBadgePolicy.isEligible(deadline.minusNanos(1)))
        assertFalse(KoyomiBadgePolicy.isEligible(deadline))
        assertFalse(KoyomiBadgePolicy.isEligible(deadline.plusNanos(1)))
        for (zone in listOf("Asia/Tokyo", "Pacific/Honolulu", "Europe/Paris", "UTC")) {
            assertEquals(KoyomiGrantResult.EXPIRED,
                KoyomiBadgePolicy.evaluate(clock(deadline).withZone(ZoneId.of(zone)).instant(), false, emptySet()))
        }
    }

    @Test fun existingAndNewUsersAreBothEligibleWithoutStoreAcquisitionMetadata() {
        val now = Instant.parse("2026-11-19T00:00:00Z")
        assertEquals(KoyomiGrantResult.NEWLY_SAVED, KoyomiBadgePolicy.evaluate(now, false, emptySet()))
        assertEquals(KoyomiGrantResult.NEWLY_SAVED, KoyomiBadgePolicy.evaluate(now, false, setOf("BRONZE", "GOLD")))
    }

    @Test fun grantPersistsOfflineAcrossRealDataStoreRecreationAndDoesNotReplayNotice() = runBlocking {
        val file = File(temporary.newFolder(), "settings.preferences_pb")
        val job = SupervisorJob(); val scope = CoroutineScope(job + Dispatchers.IO)
        val store = create(file, scope); val notice = KoyomiNoticeState()
        val result = KoyomiBadgeStore(store).grant(clock(deadline.minusNanos(1)))
        notice.onGrantResult(result)
        assertEquals(KoyomiGrantResult.NEWLY_SAVED, result); assertTrue(notice.pending.value)
        notice.dismiss(); assertFalse(notice.pending.value)
        assertTrue(store.data.first()[BadgePreferences.freeKoyomiGranted] == true)
        assertNull(store.data.first()[BadgePreferences.purchasedTiers]) // Not a fabricated purchase.
        assertNull(store.data.first()[BadgePreferences.purchasedTier])
        notice.onGrantResult(KoyomiBadgeStore(store).grant(clock(deadline.plusSeconds(1))))
        assertFalse(notice.pending.value)
        job.cancelAndJoin()
        val job2 = SupervisorJob()
        try {
            val reopened = create(file, CoroutineScope(job2 + Dispatchers.IO))
            val restartedNotice = KoyomiNoticeState()
            val after = KoyomiBadgeStore(reopened).grant(clock(deadline.plusSeconds(86400)))
            restartedNotice.onGrantResult(after)
            assertEquals(KoyomiGrantResult.ALREADY_SAVED, after)
            assertFalse(restartedNotice.pending.value)
            assertTrue(reopened.data.first()[BadgePreferences.freeKoyomiGranted] == true)
        } finally { job2.cancelAndJoin() }
    }

    @Test fun expiredFirstRunOrLostStorageDoesNotGrantBasedOnAnAccount() = runBlocking {
        val job = SupervisorJob()
        try {
            val store = create(File(temporary.newFolder(), "empty.preferences_pb"), CoroutineScope(job + Dispatchers.IO))
            assertEquals(KoyomiGrantResult.EXPIRED, KoyomiBadgeStore(store).grant(clock(deadline)))
            assertEquals(KoyomiGrantResult.EXPIRED, KoyomiBadgeStore(store).grant(clock(deadline.plusSeconds(1))))
            assertNull(store.data.first()[BadgePreferences.freeKoyomiGranted])
        } finally { job.cancelAndJoin() }
    }

    @Test fun oldSetAndSingleKeyRightsArePreservedAndNotDuplicatedOrRenotified() = runBlocking {
        for (single in listOf(false, true)) {
            val job = SupervisorJob()
            try {
                val store = create(File(temporary.newFolder(), "legacy.preferences_pb"), CoroutineScope(job + Dispatchers.IO))
                store.edit {
                    it[BadgePreferences.purchasedTiers] = setOf("BRONZE", "GOLD") + if (single) emptySet() else setOf("KOYOMI")
                    if (single) it[BadgePreferences.purchasedTier] = "KOYOMI"
                }
                val before = store.data.first()
                val result = KoyomiBadgeStore(store).grant(clock(deadline.minusSeconds(1)))
                assertEquals(KoyomiGrantResult.LEGACY_OWNED, result)
                assertEquals(before, store.data.first())
                assertEquals(KoyomiGrantResult.LEGACY_OWNED, KoyomiBadgeStore(store).grant(clock(deadline.plusSeconds(1))))
                val notice = KoyomiNoticeState(); notice.onGrantResult(result); assertFalse(notice.pending.value)
                assertEquals(1, ViewerSettings(purchasedTiers = setOf("KOYOMI", "BRONZE"), purchasedTier = "KOYOMI", freeKoyomiGranted = true).ownedBadgeIds.count { it == "KOYOMI" })
            } finally { job.cancelAndJoin() }
        }
    }

    @Test fun concurrentGrantChecksCommitOneFreeGrantOnly() = runBlocking {
        val job = SupervisorJob()
        try {
            val store = create(File(temporary.newFolder(), "concurrent.preferences_pb"), CoroutineScope(job + Dispatchers.IO))
            val results = (1..12).map { async { KoyomiBadgeStore(store).grant(clock(deadline.minusSeconds(1))) } }.awaitAll()
            assertEquals(1, results.count { it == KoyomiGrantResult.NEWLY_SAVED })
            assertEquals(11, results.count { it == KoyomiGrantResult.ALREADY_SAVED })
        } finally { job.cancelAndJoin() }
    }

    @Test fun persistenceFailureNeverReturnsANewGrantOrShowsAnAcquisitionNotice() = runBlocking {
        val failing = object : DataStore<Preferences> {
            override val data = flowOf(emptyPreferences())
            override suspend fun updateData(transform: suspend (Preferences) -> Preferences): Preferences {
                transform(emptyPreferences()) // Fail after a candidate NEWLY_SAVED decision, before commit.
                throw IOException("Disk unavailable")
            }
        }
        val notice = KoyomiNoticeState()
        try { notice.onGrantResult(KoyomiBadgeStore(failing).grant(clock(deadline.minusSeconds(1)))); fail("Save must fail") }
        catch (_: IOException) { }
        assertFalse(notice.pending.value)
    }

    @Test fun settingsResetKeepsFreeGrantAndAllLegacyRightsAfterDeadline() = runBlocking {
        val job = SupervisorJob()
        try {
            val store = create(File(temporary.newFolder(), "reset.preferences_pb"), CoroutineScope(job + Dispatchers.IO))
            store.edit {
                it[BadgePreferences.freeKoyomiGranted] = true
                it[BadgePreferences.purchasedTiers] = setOf("BRONZE", "GOLD", "LEGACY_UNKNOWN")
                it[BadgePreferences.purchasedTier] = "KOYOMI"
                it[stringPreferencesKey("direction")] = "R2L"
            }
            store.edit { resetPreferencesKeepingBadges(it) }
            val p = store.data.first()
            assertTrue(p[BadgePreferences.freeKoyomiGranted] == true)
            assertEquals(setOf("BRONZE", "GOLD", "LEGACY_UNKNOWN"), p[BadgePreferences.purchasedTiers])
            assertEquals("KOYOMI", p[BadgePreferences.purchasedTier]); assertNull(p[stringPreferencesKey("direction")])
            assertEquals(KoyomiGrantResult.ALREADY_SAVED, KoyomiBadgeStore(store).grant(clock(deadline.plusSeconds(1))))
        } finally { job.cancelAndJoin() }
    }

    @Test fun pendingNoticeWaitsWhileReadingOrBlockedAndDismissalDoesNotChangeBadgeOwnership() {
        val notice = KoyomiNoticeState(); notice.onGrantResult(KoyomiGrantResult.NEWLY_SAVED)
        assertFalse(canShowKoyomiNotice(notice.pending.value, "viewer", reading = true, blocked = false))
        assertFalse(canShowKoyomiNotice(notice.pending.value, "tutorial", false, false))
        assertFalse(canShowKoyomiNotice(notice.pending.value, "version", false, true))
        assertTrue(canShowKoyomiNotice(notice.pending.value, "version", false, false))
        notice.dismiss()
        assertFalse(canShowKoyomiNotice(notice.pending.value, "version", false, false))
        assertEquals(setOf("KOYOMI"), ViewerSettings(freeKoyomiGranted = true).ownedBadgeIds)
    }

    @Test fun freeBadgeAndLegacyProductIdentityAreSeparateFromThreePurchasePaths() {
        assertEquals(listOf(TipTier.BRONZE, TipTier.SILVER, TipTier.GOLD), TipTier.purchasableTiers)
        assertEquals(listOf("tip_100", "tip_500", "tip_1000"), TipTier.purchasableTiers.map { it.productId })
        assertEquals(TipTier.KOYOMI, TipTier.fromProductId("tip_v20_koyomi"))
        assertTrue(ViewerSettings(freeKoyomiGranted = true).allPurchasedTiers.isEmpty())
        assertEquals(setOf("KOYOMI"), ViewerSettings(freeKoyomiGranted = true).ownedBadgeIds)
    }
    @Test fun staleSettingsAndConfirmedPurchasesCannotEraseSingleKeyLegacyRightsOrFreeGrant() {
        val p = mutablePreferencesOf(BadgePreferences.purchasedTier to "KOYOMI", BadgePreferences.freeKoyomiGranted to true)
        recordPurchasedBadges(p, setOf("GOLD"))
        recordPurchasedBadges(p, emptySet())
        assertEquals(setOf("KOYOMI", "GOLD"), p[BadgePreferences.purchasedTiers])
        assertEquals("KOYOMI", p[BadgePreferences.purchasedTier])
        assertTrue(p[BadgePreferences.freeKoyomiGranted] == true)
    }

}
