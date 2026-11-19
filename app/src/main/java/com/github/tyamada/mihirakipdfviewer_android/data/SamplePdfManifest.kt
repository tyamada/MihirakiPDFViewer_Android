package com.github.tyamada.mihirakipdfviewer_android.data

import com.google.gson.JsonElement
import com.google.gson.JsonObject
import com.google.gson.JsonParser
import com.google.gson.Strictness
import com.google.gson.stream.JsonReader
import com.google.gson.stream.JsonToken
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import java.io.File
import java.io.IOException
import java.io.StringReader
import java.net.URI
import java.net.URL
import java.nio.file.Files
import java.nio.file.StandardCopyOption
import java.util.Locale
import javax.net.ssl.HttpsURLConnection

/** Trust only the manifest repository and pinned PDF release assets, never latest/download. */
internal object SamplePdfUrls {
    const val MANIFEST = "https://raw.githubusercontent.com/tyamada/manga-prompt-kit/main/releases/pdf-manifest.json"
    private val filename = Regex("[A-Za-z0-9][A-Za-z0-9_.-]*\\.pdf")
    fun safeFilename(value: String) = value.matches(filename) && !value.contains("..")
    fun pinnedPdf(value: String, expectedFilename: String? = null): Boolean = try {
        val uri = URI(value)
        val parts = uri.path.split('/')
        uri.scheme == "https" && uri.host == "github.com" && uri.rawAuthority == "github.com" &&
            uri.rawQuery == null && uri.rawFragment == null && uri.rawPath == uri.path &&
            parts.size == 7 && parts[1] == "tyamada" && parts[2] == "manga-prompt-kit" &&
            parts[3] == "releases" && parts[4] == "download" &&
            parts[5].matches(Regex("[A-Za-z0-9][A-Za-z0-9_.-]*")) && parts[5] != "latest" && !parts[5].contains("..") &&
            safeFilename(parts[6]) && (expectedFilename == null || parts[6] == expectedFilename)
    } catch (_: Exception) { false }

    fun pdfTransfer(value: String): Boolean = try {
        val uri = URI(value)
        pinnedPdf(value) || (uri.scheme == "https" && uri.port == -1 && uri.userInfo == null &&
            uri.fragment == null && uri.host in setOf("release-assets.githubusercontent.com", "objects.githubusercontent.com") &&
            uri.path.startsWith("/github-production-release-asset/"))
    } catch (_: Exception) { false }
}

/** Reject the entire candidate before it can replace the last known good catalog. */
internal object SamplePdfManifest {
    const val MAX_BYTES = 1024 * 1024
    fun parse(json: String, previous: List<SamplePdfItem> = SamplePdfCatalog.items): List<SamplePdfItem> {
        require(json.toByteArray(Charsets.UTF_8).size <= MAX_BYTES)
        val root = JsonReader(StringReader(json)).use { reader ->
            reader.setStrictness(Strictness.STRICT)
            val element = JsonParser.parseReader(reader)
            require(reader.peek() == JsonToken.END_DOCUMENT)
            element.asJsonObject
        }
        require(root.integer("schema_version") == 1L)
        val entries = root.getAsJsonArray("entries")
        require(entries.size() in 1..1000)
        val known = previous.associateBy { it.id }
        val ids = mutableSetOf<String>()
        val result = entries.map { element ->
            val entry = element.asJsonObject
            val id = entry.string("sample_id")
            val language = entry.string("language_key")
            val episode = entry.integer("episode")
            require(episode in 1..10000 && id == "ep${episode}_$language" && ids.add(id))
            require(SamplePdfLanguages.entries.any { it.code == language })
            val remoteName = entry.string("filename")
            require(SamplePdfUrls.safeFilename(remoteName))
            val url = entry.string("new_download_url")
            require(SamplePdfUrls.pinnedPdf(url, remoteName))
            val bytes = entry.integer("bytes").also { require(it in 5..(512L * 1024 * 1024)) }
            val pages = entry.integer("pages").also { require(it in 1..10000) }.toInt()
            val hash = entry.string("sha256").also { require(it.matches(Regex("[0-9a-f]{64}"))) }
            require(entry.string("license") == "CC-BY-4.0")
            val locale = entry.string("locale").also { require(it.matches(Regex("[A-Za-z]{2,3}(-[A-Za-z0-9]{2,8})*"))) }
            val title = entry.string("title").also { require(it.isNotBlank() && it.length <= 500 && it.none(Char::isISOControl)) }
            val old = known[id]
            require(old == null || (old.languageCode == language && old.locale == locale))
            val aliases = entry.getAsJsonArray("legacy").map { legacy ->
                val name = legacy.asJsonObject.string("filename")
                require(SamplePdfUrls.safeFilename(name))
                name
            }.distinct()
            SamplePdfItem(id, title, old?.fileName ?: remoteName, url,
                String.format(Locale.US, "%.1f MiB", bytes / 1048576.0), language,
                SamplePdfLanguages.displayName(language), bytes, hash, pages, locale,
                (aliases + remoteName + old?.legacyFileNames.orEmpty()).distinct())
        }
        // A truncated or accidentally reduced list must not hide existing entries.
        require(ids.containsAll(known.keys))
        require(result.map { it.fileName }.distinct().size == result.size)
        return result
    }

    private fun JsonObject.string(key: String): String {
        val value = get(key)
        require(value != null && value.isJsonPrimitive && value.asJsonPrimitive.isString)
        return value.asString
    }
    private fun JsonObject.integer(key: String): Long {
        val value: JsonElement = get(key) ?: error("Missing $key")
        require(value.isJsonPrimitive && value.asJsonPrimitive.isNumber && value.asString.matches(Regex("[0-9]+")))
        return value.asString.toLong()
    }
}

internal fun interface SamplePdfManifestTransfer { fun fetch(): String }
internal class HttpsSamplePdfManifestTransfer : SamplePdfManifestTransfer {
    override fun fetch(): String {
        val connection = URL(SamplePdfUrls.MANIFEST).openConnection() as HttpsURLConnection
        connection.instanceFollowRedirects = false
        connection.connectTimeout = 10_000
        connection.readTimeout = 15_000
        try {
            if (connection.responseCode != 200) throw IOException("Manifest HTTP ${connection.responseCode}")
            if (connection.contentLengthLong > SamplePdfManifest.MAX_BYTES) throw IOException("Manifest too large")
            return connection.inputStream.use { stream ->
                val output = java.io.ByteArrayOutputStream()
                val buffer = ByteArray(8192)
                while (true) {
                    val count = stream.read(buffer)
                    if (count < 0) break
                    require(output.size() + count <= SamplePdfManifest.MAX_BYTES)
                    output.write(buffer, 0, count)
                }
                val bytes = output.toByteArray()
                require(bytes.size <= SamplePdfManifest.MAX_BYTES)
                Charsets.UTF_8.newDecoder().decode(java.nio.ByteBuffer.wrap(bytes)).toString()
            }
        } finally { connection.disconnect() }
    }
}

internal class SamplePdfManifestStore(
    private val file: File,
    private val transfer: SamplePdfManifestTransfer = HttpsSamplePdfManifestTransfer(),
    private val bundled: List<SamplePdfItem> = SamplePdfCatalog.items
) {
    private val mutex = Mutex()
    private val mutableItems = MutableStateFlow(bundled)
    val items = mutableItems.asStateFlow()

    suspend fun load() = withContext(Dispatchers.IO) {
        mutex.withLock {
            try {
                require(file.length() in 1..SamplePdfManifest.MAX_BYTES.toLong())
                mutableItems.value = SamplePdfManifest.parse(file.readText(), bundled)
            } catch (_: Exception) { /* Keep bundled catalog; don't overwrite a bad cache. */ }
        }
    }

    suspend fun refresh(): Boolean = withContext(Dispatchers.IO) {
        mutex.withLock {
            var temporary: File? = null
            try {
                val json = transfer.fetch()
                val candidate = SamplePdfManifest.parse(json, mutableItems.value)
                val parent = requireNotNull(file.parentFile)
                if (!parent.isDirectory && !parent.mkdirs()) throw IOException("Manifest directory unavailable")
                temporary = File.createTempFile("manifest-", ".tmp", parent)
                temporary.outputStream().use { output ->
                    output.write(json.toByteArray(Charsets.UTF_8))
                    output.fd.sync()
                }
                Files.move(temporary.toPath(), file.toPath(), StandardCopyOption.ATOMIC_MOVE, StandardCopyOption.REPLACE_EXISTING)
                mutableItems.value = candidate
                true
            } catch (cancelled: CancellationException) { throw cancelled
            } catch (_: Exception) { false
            } finally { temporary?.delete() }
        }
    }
}
