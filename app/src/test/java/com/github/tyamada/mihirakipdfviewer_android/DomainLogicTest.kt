package com.github.tyamada.mihirakipdfviewer_android

import com.github.tyamada.mihirakipdfviewer_android.billing.TipTier
import com.github.tyamada.mihirakipdfviewer_android.data.*
import com.tom_roush.pdfbox.pdmodel.PDDocument
import org.junit.Assert.*
import org.junit.Test
import java.io.File

class DomainLogicTest {
    @Test
    fun `tip product maps to badge and tier`() {
        assertEquals(TipTier.BRONZE, TipTier.fromProductId("tip_100"))
        assertEquals("🥈", TipTier.fromProductId("tip_500")?.badge)
        assertEquals(TipTier.GOLD, TipTier.fromProductId("tip_1000"))
        assertEquals(TipTier.KOYOMI, TipTier.fromProductId("tip_v20_koyomi"))
        assertEquals("🌸", TipTier.fromProductId("tip_v20_koyomi")?.badge)
        assertNull(TipTier.fromProductId("unknown"))
    }

    @Test
    fun `direction detects viewer preference`() {
        assertEquals(ReadingDirection.R2L, DirectionDetector.fromMetadata(null, "R2L"))
        assertEquals(ReadingDirection.L2R, DirectionDetector.fromMetadata(null, "L2R"))
        assertNull(DirectionDetector.fromMetadata(null, null))
    }

    @Test
    fun `settings defaults match product specification`() {
        val settings = ViewerSettings()
        assertEquals(CoverMode.STANDARD, settings.coverMode)
        assertEquals(ViewerLayout.SINGLE, settings.layout)
        assertFalse(settings.showCover)
        assertTrue(settings.portraitSpread)
    }

    @Test
    fun `l2r odd final page is left aligned`() {
        assertEquals(PageSpread(2, null), SpreadPlanner.plan(3, ReadingDirection.L2R, false, CoverMode.STANDARD).last())
    }

    @Test
    fun `r2l odd final page is right aligned`() {
        assertEquals(PageSpread(null, 2), SpreadPlanner.plan(3, ReadingDirection.R2L, false, CoverMode.STANDARD).last())
    }

    @Test
    fun `standard cover is standalone on binding side`() {
        val spreads = SpreadPlanner.plan(4, ReadingDirection.R2L, true, CoverMode.STANDARD)
        assertEquals(PageSpread(null, 0), spreads.first())
        assertEquals(PageSpread(2, 1), spreads[1])
    }

    @Test
    fun `compatibility cover starts normal pairing`() {
        assertEquals(PageSpread(1, 0), SpreadPlanner.plan(3, ReadingDirection.R2L, true, CoverMode.COMPATIBILITY).first())
    }

    @Test
    fun `small PDF fixture can roundtrip as version 1_5`() {
        val tempFile = java.nio.file.Files.createTempFile("pdf-version-fixture", ".pdf").toFile()
        try {
            PDDocument().use { doc ->
                doc.addPage(com.tom_roush.pdfbox.pdmodel.PDPage())
                doc.version = 1.5f
                doc.save(tempFile)
            }
            PDDocument.load(tempFile).use { check ->
                assertEquals(1, check.numberOfPages)
                assertEquals(1.5f, check.version, 0.01f)
            }
        } finally {
            tempFile.delete()
        }
    }
}
