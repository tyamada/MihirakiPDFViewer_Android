package com.github.tyamada.mihirakipdfviewer_android

import android.net.Uri
import androidx.test.platform.app.InstrumentationRegistry
import com.github.tyamada.mihirakipdfviewer_android.pdf.PdfDocumentRepository
import com.github.tyamada.mihirakipdfviewer_android.pdf.PdfOpenException
import com.tom_roush.pdfbox.android.PDFBoxResourceLoader
import com.tom_roush.pdfbox.pdmodel.*
import com.tom_roush.pdfbox.pdmodel.font.PDType1Font
import com.tom_roush.pdfbox.pdmodel.encryption.*
import com.tom_roush.pdfbox.pdmodel.interactive.documentnavigation.outline.*
import kotlinx.coroutines.runBlocking
import org.junit.Assert.*
import org.junit.Test
import java.io.File

class PdfRenderingSmokeTest {
    @Test fun encryptedPdfRendersSearchesAndResolvesOutline() = runBlocking {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        PDFBoxResourceLoader.init(context)
        val file = File.createTempFile("release-smoke", ".pdf", context.cacheDir)
        try {
            PDDocument().use { doc ->
                val page = PDPage(); doc.addPage(page)
                PDPageContentStream(doc, page).use { stream ->
                    stream.beginText(); stream.setFont(PDType1Font.HELVETICA, 18f)
                    stream.newLineAtOffset(50f, 700f); stream.showText("Release verification text"); stream.endText()
                }
                val outline = PDDocumentOutline()
                val entry = PDOutlineItem(); entry.title = "Smoke chapter"; entry.setDestination(page)
                outline.addLast(entry); doc.documentCatalog.documentOutline = outline
                doc.protect(StandardProtectionPolicy("test-owner", "test-reader", AccessPermission()).apply { encryptionKeyLength = 128 })
                doc.save(file)
            }
            val repo = PdfDocumentRepository(context)
            try { repo.open(Uri.fromFile(file)); fail("Password must be required") } catch (_: PdfOpenException.PasswordRequired) { }
            try { repo.open(Uri.fromFile(file), "wrong"); fail("Wrong password must be rejected") } catch (_: PdfOpenException.WrongPassword) { }
            repo.open(Uri.fromFile(file), "test-reader").use { source ->
                assertEquals(1, source.pageCount)
                assertEquals("Smoke chapter", source.outline.single().title)
                assertEquals(0, source.outline.single().pageIndex)
                assertTrue(source.search("verification").isNotEmpty())
                val bitmap = source.render(0, 600, false)
                assertTrue(bitmap.width > 0); assertTrue(bitmap.height > 0)
                val pixels = IntArray(bitmap.width * bitmap.height)
                bitmap.getPixels(pixels, 0, bitmap.width, 0, 0, bitmap.width, bitmap.height)
                assertTrue("Rendered page must contain visible content", pixels.any { android.graphics.Color.alpha(it) > 0 && it != android.graphics.Color.WHITE })
                bitmap.recycle()
            }
        } finally { file.delete() }
    }
}
