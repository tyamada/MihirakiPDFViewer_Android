package com.github.tyamada.mihirakipdfviewer_android

import com.github.tyamada.mihirakipdfviewer_android.billing.TipTier
import com.github.tyamada.mihirakipdfviewer_android.data.*
import org.junit.Assert.*
import org.junit.Test

class DomainLogicTest {
    @Test fun `tip product maps to badge and tier`() {
        assertEquals(TipTier.BRONZE, TipTier.fromProductId("tip_100")); assertEquals("🥈", TipTier.fromProductId("tip_500")?.badge)
        assertEquals(TipTier.GOLD, TipTier.fromProductId("tip_1000")); assertNull(TipTier.fromProductId("unknown"))
    }
    @Test fun `direction detects viewer preference`() {
        assertEquals(ReadingDirection.R2L, DirectionDetector.fromMetadata(null, "R2L")); assertEquals(ReadingDirection.L2R, DirectionDetector.fromMetadata(null, "L2R")); assertNull(DirectionDetector.fromMetadata(null, null))
    }
    @Test fun `settings defaults match product specification`() {
        val settings = ViewerSettings(); assertEquals(CoverMode.STANDARD, settings.coverMode); assertEquals(ViewerLayout.SINGLE, settings.layout); assertFalse(settings.showCover)
    }
    @Test fun `l2r odd final page is left aligned`() {
        assertEquals(PageSpread(2, null), SpreadPlanner.plan(3, ReadingDirection.L2R, false, CoverMode.STANDARD).last())
    }
    @Test fun `r2l odd final page is right aligned`() {
        assertEquals(PageSpread(null, 2), SpreadPlanner.plan(3, ReadingDirection.R2L, false, CoverMode.STANDARD).last())
    }
    @Test fun `standard cover is standalone on binding side`() {
        val spreads = SpreadPlanner.plan(4, ReadingDirection.R2L, true, CoverMode.STANDARD)
        assertEquals(PageSpread(null, 0), spreads.first()); assertEquals(PageSpread(2, 1), spreads[1])
    }
    @Test fun `compatibility cover starts normal pairing`() {
        assertEquals(PageSpread(1, 0), SpreadPlanner.plan(3, ReadingDirection.R2L, true, CoverMode.COMPATIBILITY).first())
    }
}
