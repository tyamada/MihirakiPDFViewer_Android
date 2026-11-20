package com.github.tyamada.mihirakipdfviewer_android

import androidx.test.platform.app.InstrumentationRegistry
import com.github.tyamada.mihirakipdfviewer_android.data.SamplePdfCatalog
import java.io.File
import java.security.MessageDigest

/** Explicit integration input; never silently fall back to bundled or user PDFs. */
object VerifiedSampleInputs {
    fun file(id: String): File {
        val directory = requireNotNull(InstrumentationRegistry.getArguments().getString("samplePdfFixtureDir")) {
            "Integration input missing: stage pinned PDFs and supply samplePdfFixtureDir"
        }
        val item = SamplePdfCatalog.items.single { it.id == id }
        val file = File(directory, item.fileName)
        require(file.isFile && file.length() == item.bytes) { "Missing/wrong-size integration input: $id" }
        val digest = MessageDigest.getInstance("SHA-256")
        file.inputStream().use { stream ->
            val buffer = ByteArray(64 * 1024)
            while (true) { val size = stream.read(buffer); if (size < 0) break; digest.update(buffer, 0, size) }
        }
        val actual = digest.digest().joinToString("") { "%02x".format(it.toInt() and 0xff) }
        require(actual == item.sha256) { "Integration input checksum mismatch: $id" }
        return file
    }
}
