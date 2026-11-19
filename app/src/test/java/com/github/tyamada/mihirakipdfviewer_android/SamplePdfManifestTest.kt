package com.github.tyamada.mihirakipdfviewer_android

import com.github.tyamada.mihirakipdfviewer_android.data.*
import com.google.gson.JsonParser
import kotlinx.coroutines.runBlocking
import org.junit.Assert.*
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import java.io.File
import java.io.IOException

class SamplePdfManifestTest {
    @get:Rule val temporary = TemporaryFolder()
    private fun fixture() = javaClass.getResource("/sample-pdf-manifest-v1.json")!!.readText()
    private fun changed(edit: (com.google.gson.JsonObject) -> Unit): String =
        JsonParser.parseString(fixture()).asJsonObject.also(edit).toString()
    private fun rejects(json: String) {
        try { SamplePdfManifest.parse(json); fail("Invalid manifest accepted") } catch (_: Exception) { }
    }

    @Test fun publishedManifestKeepsAllIdentitiesNamesAndIntegrityValues() {
        val items = SamplePdfManifest.parse(fixture())
        assertEquals(51, items.size)
        SamplePdfCatalog.items.forEach { bundled ->
            val item = items.single { it.id == bundled.id }
            assertEquals(bundled.copy(legacyFileNames = item.legacyFileNames), item)
        }
        val fil = items.single { it.id == "ep1_fil" }
        assertEquals("fil", fil.languageCode)
        assertTrue(fil.legacyFileNames.contains("tameshibu_episode1_3_fil.pdf"))
        assertTrue(fil.url.endsWith("_tl.pdf"))
    }

    @Test fun malformedUnsupportedDuplicateIncompleteOrUntrustedCandidatesAreRejected() {
        listOf("not json", fixture() + " garbage", fixture().replace("\"schema_version\": 1", "\"schema_version\": 2"),
            changed { it.getAsJsonArray("entries").add(it.getAsJsonArray("entries")[0]) },
            changed { it.getAsJsonArray("entries").remove(0) },
            changed { it.getAsJsonArray("entries")[0].asJsonObject.addProperty("bytes", -1) },
            changed { it.getAsJsonArray("entries")[0].asJsonObject.addProperty("bytes", "24217661") },
            changed { it.getAsJsonArray("entries")[0].asJsonObject.addProperty("sha256", "invalid") },
            changed { it.getAsJsonArray("entries")[0].asJsonObject.addProperty("filename", "../escape.pdf") },
            changed { it.getAsJsonArray("entries")[0].asJsonObject.addProperty("language_key", "tl") },
            changed { it.getAsJsonArray("entries")[0].asJsonObject.addProperty("license", "unknown") }
        ).forEach(::rejects)
        for (url in listOf("http://github.com/tyamada/manga-prompt-kit/releases/download/tag/file.pdf",
            "https://evil.example/file.pdf", "https://github.com/tyamada/other/releases/download/tag/file.pdf",
            "https://github.com/tyamada/manga-prompt-kit/releases/latest/download/file.pdf",
            "https://github.com:443/tyamada/manga-prompt-kit/releases/download/tag/file.pdf",
            "https://github.com/tyamada/manga-prompt-kit/releases/download/tag/file.pdf?x=1")) {
            rejects(changed { it.getAsJsonArray("entries")[0].asJsonObject.addProperty("new_download_url", url) })
        }
    }

    @Test fun refreshPersistsOnlyCompleteValidCatalogAndOfflineRestartRetainsIt() = runBlocking {
        val file = File(temporary.newFolder(), "manifest.json")
        var response = fixture()
        val store = SamplePdfManifestStore(file, SamplePdfManifestTransfer { response })
        assertTrue(store.refresh())
        response = changed { it.getAsJsonArray("entries")[0].asJsonObject.addProperty("title", "Updated manifest title") }
        assertTrue(store.refresh())
        assertEquals("Updated manifest title", store.items.value.single { it.id == "ep1_ja" }.title)
        val saved = file.readBytes()
        val previous = store.items.value
        response = "{broken"
        assertFalse(store.refresh())
        assertArrayEquals(saved, file.readBytes()); assertEquals(previous, store.items.value)
        val offline = SamplePdfManifestStore(file, SamplePdfManifestTransfer { throw IOException("offline") })
        offline.load(); assertEquals(previous, offline.items.value)
        assertFalse(offline.refresh()); assertEquals(previous, offline.items.value)
        assertArrayEquals(saved, file.readBytes())
    }

    @Test fun badDiskCacheAndFetchFailureUseBundledCatalogWithoutReplacingDisk() = runBlocking {
        val file = File(temporary.newFolder(), "manifest.json").apply { writeText("corrupt") }
        val store = SamplePdfManifestStore(file, SamplePdfManifestTransfer { throw IOException("offline") })
        store.load(); assertFalse(store.refresh())
        assertEquals(SamplePdfCatalog.items, store.items.value); assertEquals("corrupt", file.readText())
    }

    @Test fun renamedRemoteAssetKeepsExistingLocalFilename() {
        val json = changed {
            val entry = it.getAsJsonArray("entries")[0].asJsonObject
            entry.addProperty("filename", "revised.pdf")
            entry.addProperty("new_download_url", "https://github.com/tyamada/manga-prompt-kit/releases/download/new-tag/revised.pdf")
        }
        val item = SamplePdfManifest.parse(json).single { it.id == "ep1_ja" }
        assertEquals(SamplePdfCatalog.items.single { it.id == item.id }.fileName, item.fileName)
        assertTrue(item.url.endsWith("/new-tag/revised.pdf"))
    }

    @Test fun redirectTrustRejectsCredentialsDowngradesAndUnrelatedHosts() {
        assertTrue(SamplePdfUrls.pdfTransfer("https://release-assets.githubusercontent.com/github-production-release-asset/1/abc?token=signed"))
        listOf("http://release-assets.githubusercontent.com/github-production-release-asset/1/a",
            "https://evil.example/github-production-release-asset/1/a",
            "https://user@release-assets.githubusercontent.com/github-production-release-asset/1/a",
            "https://release-assets.githubusercontent.com/other/a").forEach { assertFalse(SamplePdfUrls.pdfTransfer(it)) }
    }
}
