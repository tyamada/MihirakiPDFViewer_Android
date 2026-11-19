package com.github.tyamada.mihirakipdfviewer_android

import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.github.tyamada.mihirakipdfviewer_android.data.*
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.runBlocking
import org.json.JSONObject
import org.junit.Assert.*
import org.junit.Assume.assumeTrue
import org.junit.Test
import org.junit.runner.RunWith
import java.io.File
import java.io.IOException
import java.util.UUID

@RunWith(AndroidJUnit4::class)
class SamplePdfManifestNetworkTest {
    @Test fun liveManifestPdfAndOfflineRestartKeepVerifiedData() = runBlocking {
        assumeTrue(InstrumentationRegistry.getArguments().getString("sampleManifestNetwork") == "true")
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val root = File(context.cacheDir, "manifest-qa-${UUID.randomUUID()}").apply { mkdirs() }
        val report = JSONObject().put("manifest_url", SamplePdfUrls.MANIFEST)
        try {
            val manifestFile = File(root, "catalog.json")
            val store = SamplePdfManifestStore(manifestFile)
            assertTrue("Live manifest refresh failed", store.refresh())
            val items = store.items.value
            assertEquals(51, items.size)
            report.put("live_manifest_count", items.size)
            val item = items.single { it.id == "ep3_ja" }
            val cache = SamplePdfCache(File(root, "pdfs"))
            assertEquals(DownloadState.Success, SamplePdfDownloadManager(cache).download(item).toList().last())
            val downloaded = requireNotNull(cache.downloadedFile(item))
            assertTrue(cache.matches(downloaded, item))
            report.put("pdf_url", item.url).put("bytes", item.bytes).put("sha256", item.sha256)
            report.put("pdf_integrity_verified", true)
            val savedManifest = manifestFile.readBytes()
            val stamp = downloaded.lastModified()
            val offline = SamplePdfManifestStore(manifestFile, SamplePdfManifestTransfer { throw IOException("Simulated offline") })
            offline.load()
            assertFalse(offline.refresh())
            assertEquals(items, offline.items.value)
            assertArrayEquals(savedManifest, manifestFile.readBytes())
            val reused = offline.items.value.single { it.id == item.id }
            val noNetwork = SamplePdfDownloadManager(cache, SamplePdfTransfer { _, _, _ -> fail("Verified PDF must not be downloaded again") })
            assertEquals(DownloadState.Success, noNetwork.download(reused).toList().last())
            assertEquals(downloaded, cache.downloadedFile(reused)); assertEquals(stamp, downloaded.lastModified())
            report.put("offline_manifest_restart", true).put("offline_pdf_reused_without_transfer", true)
            report.put("offline_mode", "Injected network failure; device network settings unchanged")
            val invalid = SamplePdfManifestStore(manifestFile, SamplePdfManifestTransfer { "{broken" })
            invalid.load(); assertFalse(invalid.refresh())
            assertEquals(items, invalid.items.value); assertArrayEquals(savedManifest, manifestFile.readBytes())
            report.put("invalid_refresh_preserved_previous", true).put("passed", true)
        } finally {
            File(context.getExternalFilesDir(null), "sample-manifest-network.json").writeText(report.toString(2))
            root.deleteRecursively() // Only this test's unique synthetic files, not existing PDFs.
        }
        Unit
    }
}
