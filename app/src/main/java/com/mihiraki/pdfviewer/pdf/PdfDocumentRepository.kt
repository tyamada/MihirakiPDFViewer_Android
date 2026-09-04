package com.mihiraki.pdfviewer.pdf

import android.content.ContentResolver
import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.pdf.PdfRenderer
import android.net.Uri
import android.os.ParcelFileDescriptor
import com.mihiraki.pdfviewer.data.DocumentInfo
import com.mihiraki.pdfviewer.data.SearchHit
import com.mihiraki.pdfviewer.data.SearchRect
import com.tom_roush.pdfbox.pdmodel.PDDocument
import com.tom_roush.pdfbox.text.PDFTextStripper
import com.tom_roush.pdfbox.text.TextPosition
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.Closeable
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.text.Normalizer

sealed class PdfOpenException(message: String, cause: Throwable? = null) : IOException(message, cause) {
    class PasswordRequired : PdfOpenException("password_required")
    class WrongPassword : PdfOpenException("wrong_password")
    class PermissionDenied(cause: Throwable? = null) : PdfOpenException("permission_denied", cause)
    class Corrupt(cause: Throwable? = null) : PdfOpenException("corrupt_pdf", cause)
}

interface PdfSource : Closeable {
    val pageCount: Int
    val info: DocumentInfo
    val directionHint: String?
    val layoutHint: String?
    suspend fun render(page: Int, width: Int, highQuality: Boolean, sharpness: Float = 0f, highlights: List<SearchRect> = emptyList()): Bitmap
    suspend fun search(query: String): List<SearchHit>
}

/** Copies a picked document into the private cache: providers need not expose seekable descriptors. */
class PdfDocumentRepository(private val context: Context) {
    suspend fun open(uri: Uri, password: String? = null): PdfSource = withContext(Dispatchers.IO) {
        val cached = copyToPrivateCache(context.contentResolver, uri)
        try {
            HybridPdfSource(cached, password)
        } catch (_: com.tom_roush.pdfbox.pdmodel.encryption.InvalidPasswordException) {
            cached.delete()
            if (password == null) throw PdfOpenException.PasswordRequired()
            throw PdfOpenException.WrongPassword()
        } catch (e: SecurityException) {
            cached.delete(); throw PdfOpenException.PermissionDenied(e)
        } catch (e: Exception) {
            cached.delete(); throw PdfOpenException.Corrupt(e)
        }
    }

    private fun copyToPrivateCache(resolver: ContentResolver, uri: Uri): File {
        val file = File.createTempFile("mihiraki_", ".pdf", context.cacheDir)
        try {
            resolver.openInputStream(uri)?.use { input -> FileOutputStream(file).use(input::copyTo) }
                ?: throw PdfOpenException.PermissionDenied()
        } catch (e: SecurityException) { file.delete(); throw PdfOpenException.PermissionDenied(e) }
        return file
    }
}

private class HybridPdfSource(private val file: File, password: String?) : PdfSource {
    private val pdfBox = PDDocument.load(file, password ?: "")
    private val descriptor = ParcelFileDescriptor.open(file, ParcelFileDescriptor.MODE_READ_ONLY)
    private val platformRenderer = runCatching { PdfRenderer(descriptor) }.getOrNull()
    private val pdfBoxRenderer = com.tom_roush.pdfbox.rendering.PDFRenderer(pdfBox)

    override val pageCount: Int = pdfBox.numberOfPages
    override val info = pdfBox.documentInformation.let {
        DocumentInfo(it.title.orEmpty(), it.author.orEmpty(), it.subject.orEmpty(), it.keywords.orEmpty(), pdfBox.version.toString())
    }
    override val directionHint: String? = runCatching {
        pdfBox.documentCatalog.cosObject.getDictionaryObject("ViewerPreferences")
            ?.let { it as? com.tom_roush.pdfbox.cos.COSDictionary }
            ?.getNameAsString("Direction")
    }.getOrNull()
    override val layoutHint: String? = runCatching {
        pdfBox.documentCatalog.cosObject.getNameAsString("PageLayout")
    }.getOrNull()

    override suspend fun render(page: Int, width: Int, highQuality: Boolean, sharpness: Float, highlights: List<SearchRect>): Bitmap = withContext(Dispatchers.IO) {
        require(page in 0 until pageCount)
        val quality = if (highQuality) 2f else 1f
        val rendered = platformRenderer?.let { renderer ->
            renderer.openPage(page).use { p ->
                val targetW = (width * quality).toInt().coerceAtLeast(1)
                val targetH = (targetW * p.height.toFloat() / p.width).toInt().coerceAtLeast(1)
                Bitmap.createBitmap(targetW, targetH, Bitmap.Config.ARGB_8888).also { bitmap ->
                    bitmap.eraseColor(Color.WHITE)
                    p.render(bitmap, null, null, PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY)
                    
                    if (highlights.isNotEmpty()) {
                        val canvas = Canvas(bitmap)
                        val paint = Paint().apply { color = 0xAAFFFF00.toInt(); style = Paint.Style.FILL }
                        val borderPaint = Paint().apply { color = Color.RED; style = Paint.Style.STROKE; strokeWidth = 2f }
                        val scale = targetW.toFloat() / p.width
                        highlights.forEach { rect ->
                            val l = rect.left * scale; val t = rect.top * scale
                            val r = rect.right * scale; val b = rect.bottom * scale
                            canvas.drawRect(l, t, r, b, paint)
                            canvas.drawRect(l, t, r, b, borderPaint)
                        }
                    }
                }
            }
        } ?: pdfBoxRenderer.renderImage(page, quality).also { bitmap ->
             if (highlights.isNotEmpty()) {
                 val canvas = Canvas(bitmap)
                 val paint = Paint().apply { color = 0xAAFFFF00.toInt(); style = Paint.Style.FILL }
                 val borderPaint = Paint().apply { color = Color.RED; style = Paint.Style.STROKE; strokeWidth = 2f }
                 val pdPage = pdfBox.getPage(page)
                 val box = pdPage.mediaBox
                 val scale = bitmap.width.toFloat() / box.width
                 highlights.forEach { rect ->
                     val l = rect.left * scale; val t = rect.top * scale
                     val r = rect.right * scale; val b = rect.bottom * scale
                     canvas.drawRect(l, t, r, b, paint)
                     canvas.drawRect(l, t, r, b, borderPaint)
                 }
             }
        }
        sharpen(rendered, sharpness)
    }

    override suspend fun search(query: String): List<SearchHit> = withContext(Dispatchers.IO) {
        if (query.isBlank()) return@withContext emptyList()
        val results = mutableListOf<SearchHit>()
        
        for (i in 0 until pageCount) {
            val locator = CoordinateFinder(query)
            locator.startPage = i + 1
            locator.endPage = i + 1
            try {
                locator.writeText(pdfBox, java.io.StringWriter())
            } catch (e: Exception) {
                android.util.Log.e("MihirakiSearch", "Error searching page $i", e)
            }
            if (locator.hits.isNotEmpty()) {
                results.add(SearchHit(i, locator.hits))
            }
        }
        results
    }

    override fun close() {
        platformRenderer?.close()
        if (platformRenderer == null) descriptor.close()
        pdfBox.close()
        file.delete()
    }

    private fun sharpen(source: Bitmap, amount: Float): Bitmap {
        if (amount <= 0.01f || source.width < 3 || source.height < 3) return source
        val w = source.width; val h = source.height
        val input = IntArray(w * h); val output = IntArray(w * h); source.getPixels(input, 0, w, 0, 0, w, h); input.copyInto(output)
        val a = amount.coerceIn(0f, 1f)
        fun channel(center: Int, neighbors: Int, shift: Int): Int {
            val c = center shr shift and 255
            return (c * (1f + 4f * a) - neighbors * a).toInt().coerceIn(0, 255)
        }
        for (y in 1 until h - 1) for (x in 1 until w - 1) {
            val i = y * w + x; val c = input[i]; val ns = intArrayOf(input[i - 1], input[i + 1], input[i - w], input[i + w])
            val r = channel(c, ns.sumOf { it shr 16 and 255 }, 16); val g = channel(c, ns.sumOf { it shr 8 and 255 }, 8); val b = channel(c, ns.sumOf { it and 255 }, 0)
            output[i] = (c and -0x1000000) or (r shl 16) or (g shl 8) or b
        }
        return Bitmap.createBitmap(output, w, h, Bitmap.Config.ARGB_8888).also { if (it !== source) source.recycle() }
    }
}

private class CoordinateFinder(private val query: String) : PDFTextStripper() {
    val hits = mutableListOf<SearchRect>()

    init {
        sortByPosition = true
    }

    override fun writeString(text: String, textPositions: List<TextPosition>) {
        var start = 0
        while (true) {
            val index = text.indexOf(query, start, ignoreCase = true)
            if (index == -1) break
            
            var minX = Float.MAX_VALUE; var minY = Float.MAX_VALUE
            var maxX = Float.MIN_VALUE; var maxY = Float.MIN_VALUE
            
            // Search for the corresponding TextPositions
            // Note: text and textPositions might not be 1-to-1 if there are complex glyphs,
            // but for simple search it's usually close enough.
            for (i in index until (index + query.length)) {
                if (i >= textPositions.size) break
                val pos = textPositions[i]
                
                val x = pos.xDirAdj
                val y = pos.yDirAdj
                val w = pos.widthDirAdj
                val h = pos.heightDir
                
                // yDirAdj is the baseline. 
                // We want to highlight the whole glyph height.
                val top = y - h
                val bottom = y + (h * 0.2f) // add a bit of descent
                
                minX = minOf(minX, x); minY = minOf(minY, top)
                maxX = maxOf(maxX, x + w); maxY = maxOf(maxY, bottom)
            }
            if (minX != Float.MAX_VALUE) {
                hits.add(SearchRect(minX, minY, maxX, maxY))
            }
            start = index + 1
        }
    }
}
