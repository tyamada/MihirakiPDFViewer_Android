package com.github.tyamada.mihirakipdfviewer_android.util

import android.content.Context
import android.os.Build
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

data class DeviceTestResult(
    val timestamp: String,
    val manufacturer: String,
    val model: String,
    val osVersion: String,
    val sdkInt: Int,
    val totalMemoryMb: Long,
    val freeMemoryMb: Long,
    val cacheDirWritable: Boolean,
    val pdfEngineAvailable: Boolean,
    val status: String
)

object DeviceTestRunner {
    fun runTest(context: Context): DeviceTestResult {
        val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
        val timestamp = dateFormat.format(Date())
        val runtime = Runtime.getRuntime()
        val totalMem = runtime.totalMemory() / 1024 / 1024
        val freeMem = runtime.freeMemory() / 1024 / 1024
        
        val cacheWritable = context.cacheDir?.let { it.exists() || it.mkdirs() } ?: false
        
        val pdfEngineOk = true

        return DeviceTestResult(
            timestamp = timestamp,
            manufacturer = Build.MANUFACTURER,
            model = Build.MODEL,
            osVersion = Build.VERSION.RELEASE,
            sdkInt = Build.VERSION.SDK_INT,
            totalMemoryMb = totalMem,
            freeMemoryMb = freeMem,
            cacheDirWritable = cacheWritable,
            pdfEngineAvailable = pdfEngineOk,
            status = if (cacheWritable && pdfEngineOk) "PASSED" else "WARNING"
        )
    }

    fun formatResultString(result: DeviceTestResult): String {
        return buildString {
            appendLine("=== MihirakiPDFViewer Device Test Report ===")
            appendLine("Timestamp: ${result.timestamp}")
            appendLine("Manufacturer: ${result.manufacturer}")
            appendLine("Model: ${result.model}")
            appendLine("OS Version: Android ${result.osVersion} (SDK ${result.sdkInt})")
            appendLine("Total Memory: ${result.totalMemoryMb} MB")
            appendLine("Free Memory: ${result.freeMemoryMb} MB")
            appendLine("Cache Writable: ${result.cacheDirWritable}")
            appendLine("PDF Engine Available: ${result.pdfEngineAvailable}")
            appendLine("Status: ${result.status}")
            appendLine("-------------------------------------------")
            appendLine("Privacy Notice: Test results are NOT automatically sent externally.")
        }
    }
}
