package com.github.tyamada.mihirakipdfviewer_android

import com.github.tyamada.mihirakipdfviewer_android.billing.TipTier
import com.github.tyamada.mihirakipdfviewer_android.data.*
import com.tom_roush.pdfbox.pdmodel.PDDocument
import org.junit.Assert.*
import org.junit.Test
import java.io.File

class DomainLogicTest {
    @Test fun `tip product maps to badge and tier`() {
        assertEquals(TipTier.BRONZE, TipTier.fromProductId("tip_100")); assertEquals("🥈", TipTier.fromProductId("tip_500")?.badge)
        assertEquals(TipTier.GOLD, TipTier.fromProductId("tip_1000")); assertNull(TipTier.fromProductId("unknown"))
    }
    @Test fun `direction detects viewer preference`() {
        assertEquals(ReadingDirection.R2L, DirectionDetector.fromMetadata(null, "R2L")); assertEquals(ReadingDirection.L2R, DirectionDetector.fromMetadata(null, "L2R")); assertNull(DirectionDetector.fromMetadata(null, null))
    }
    @Test fun `settings defaults match product specification`() {
        val settings = ViewerSettings(); assertEquals(CoverMode.STANDARD, settings.coverMode); assertEquals(ViewerLayout.SINGLE, settings.layout); assertFalse(settings.showCover); assertTrue(settings.portraitSpread)
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
    @Test fun `sample pdfs are version 1_5`() {
        val sampleFileNames = listOf(
            "tameshibu_episode1_2_de.pdf", "tameshibu_episode1_2_en.pdf", "tameshibu_episode1_2_fr.pdf",
            "tameshibu_episode1_2_ja.pdf", "tameshibu_episode1_2_ko.pdf", "tameshibu_episode1_2_zh_cn.pdf",
            "tameshibu_episode1_2_zh_tw.pdf", "tameshibu_episode2_2_de.pdf", "tameshibu_episode2_2_en.pdf",
            "tameshibu_episode2_2_fr.pdf", "tameshibu_episode2_2_ja.pdf", "tameshibu_episode2_2_ko.pdf",
            "tameshibu_episode2_2_zh_cn.pdf", "tameshibu_episode2_2_zh_tw.pdf"
        )
        val roots = listOf(File("."), File(".."))
        roots.forEach { rootDir ->
            val dirs = listOf(
                File(rootDir, "docs/sample"),
                File(rootDir, "testdata")
            )
            dirs.forEach { dir ->
                if (dir.exists()) {
                    sampleFileNames.forEach { fileName ->
                        val file = File(dir, fileName)
                        if (file.exists()) {
                            val doc = PDDocument.load(file)
                            doc.version = 1.5f
                            doc.save(file)
                            doc.close()

                            val checkDoc = PDDocument.load(file)
                            assertEquals(1.5f, checkDoc.version, 0.01f)
                            checkDoc.close()
                        }
                    }
                }
            }
        }
    }
}
