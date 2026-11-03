package com.github.tyamada.mihirakipdfviewer_android

import android.net.Uri
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import androidx.test.platform.app.InstrumentationRegistry
import com.github.tyamada.mihirakipdfviewer_android.data.SamplePdfCatalog
import com.github.tyamada.mihirakipdfviewer_android.pdf.PdfDocumentRepository
import com.tom_roush.pdfbox.android.PDFBoxResourceLoader
import com.tom_roush.pdfbox.pdmodel.PDDocument
import com.tom_roush.pdfbox.text.PDFTextStripper
import kotlinx.coroutines.runBlocking
import org.json.JSONArray
import org.json.JSONObject
import org.junit.Assert.*
import org.junit.Assume.assumeTrue
import org.junit.Test
import java.io.File

/** Opt-in full release fixtures; never writes to or deletes user PDFs. */
class SamplePdfV2DeviceTest {
    @get:org.junit.Rule val activity = androidx.test.ext.junit.rules.ActivityScenarioRule(MainActivity::class.java)
    @Test fun all51RenderInPageOrderAndHaveSearchableCopyableCoverText() = runBlocking {
        val fixture = InstrumentationRegistry.getArguments().getString("samplePdfFixtureDir")
        assumeTrue("Supply the independently verified v2 PDFs", fixture != null)
        val instrumentation = InstrumentationRegistry.getInstrumentation()
        val context = instrumentation.targetContext
        PDFBoxResourceLoader.init(context)
        val output = File(context.getExternalFilesDir(null), "v2-qa-results").apply { mkdirs() }
        val queries = mapOf("ja" to "ためし部", "ko" to "해봄부", "zh_cn" to "试试社", "zh_tw" to "試試社", "zh_hk" to "試試社", "de" to "PROBIERCLUB", "en" to "CLUB", "es" to "CLUB", "es_419" to "CLUB", "fr" to "CLUB", "id" to "KLUB", "it" to "CLUB", "ne" to "प्रयोग", "pt" to "CLUBE", "pt_pt" to "CLUBE", "fil" to "CLUB", "vi" to "CÂU")
        val results = JSONArray()
        try {
            SamplePdfCatalog.items.forEach { item ->
                val file = File(fixture!!, item.fileName)
                assertEquals(item.id, item.bytes, file.length())
                PdfDocumentRepository(context).open(Uri.fromFile(file)).use { source ->
                    assertEquals(item.id, item.pageCount, source.pageCount)
                    for (page in 0 until source.pageCount) {
                        val bitmap = source.render(page, 360, false)
                        assertTrue(item.id, bitmap.width > 0 && bitmap.height > 0)
                        val pixels = IntArray(bitmap.width * bitmap.height)
                        bitmap.getPixels(pixels, 0, bitmap.width, 0, 0, bitmap.width, bitmap.height)
                        assertTrue("${item.id} page $page blank", pixels.any { android.graphics.Color.alpha(it) > 0 && it != android.graphics.Color.WHITE })
                        if (page == 0) File(output, "${item.id}-cover.png").outputStream().use { bitmap.compress(android.graphics.Bitmap.CompressFormat.PNG, 100, it) }
                        bitmap.recycle()
                    }
                    val query = queries.getValue(item.languageCode)
                    val hits = source.search(query)
                    assertTrue("${item.id} query=$query", hits.any { it.pageIndex == 0 && it.rects.isNotEmpty() })
                    val text = PDDocument.load(file).use { doc -> PDFTextStripper().apply { startPage=1; endPage=1 }.getText(doc) }
                    assertTrue(item.id, text.isNotBlank())
                    instrumentation.runOnMainSync {
                        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                        clipboard.setPrimaryClip(ClipData.newPlainText("v2 QA", text))
                        assertEquals(text, clipboard.primaryClip!!.getItemAt(0).text.toString())
                    }
                    results.put(JSONObject().put("id",item.id).put("pages_rendered",source.pageCount).put("query",query).put("cover_search_hits",hits.count { it.pageIndex==0 }).put("extracted_text_clipboard_roundtrip",true))
                }
            }
        } finally { File(output,"pdf-engine.json").writeText(results.toString(2)) }
        Unit
    }
}
