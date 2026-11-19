package com.github.tyamada.mihirakipdfviewer_android.data

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Environment
import java.io.File

sealed class DownloadState {
    object Idle : DownloadState()
    data class Downloading(val progress: Float) : DownloadState()
    object Success : DownloadState()
    data class Error(val message: String, val integrityFailure: Boolean = false) : DownloadState()
}

class SamplePdfRepository(private val context: Context) {
    private val sampleDirectory = File(
        context.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS) ?: File(context.filesDir, "Documents"),
        "MihirakiSamplePdfs"
    )
    private val cache = SamplePdfCache(sampleDirectory)
    private val downloads = SamplePdfDownloadManager(cache)
    private val manifest = SamplePdfManifestStore(File(context.filesDir, "sample-pdf-manifest/catalog.json"))
    val catalogItems = manifest.items
    suspend fun loadCatalog() = manifest.load()
    suspend fun refreshCatalog() = manifest.refresh()

    fun getDownloadedFile(item: SamplePdfItem): File? = cache.downloadedFile(item)
    fun isDownloaded(item: SamplePdfItem): Boolean = getDownloadedFile(item) != null
    fun deletePdf(item: SamplePdfItem): Boolean = cache.delete(item)
    // Reset only manages the current catalog's cache. Never recursively delete legacy/user files.
    fun deleteAllDownloadedPdfs() {
        catalogItems.value.forEach { cache.delete(it) }
    }

    fun downloadPdf(item: SamplePdfItem) = downloads.download(item)

    fun isOnCellularNetwork(): Boolean {
        val manager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as? ConnectivityManager ?: return false
        val network = manager.activeNetwork ?: return false
        val capabilities = manager.getNetworkCapabilities(network) ?: return false
        return capabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) &&
            !capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) &&
            !capabilities.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET)
    }
}
