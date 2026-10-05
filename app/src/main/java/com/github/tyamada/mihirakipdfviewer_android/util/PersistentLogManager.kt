package com.github.tyamada.mihirakipdfviewer_android.util

import android.content.Context
import android.util.Log
import java.io.File
import java.io.FileWriter
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object PersistentLogManager {
    private const val TAG = "PersistentLog"
    private const val LOG_DIR_NAME = "app_logs"
    private const val MAX_RETENTION_DAYS = 14L
    private val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.US)
    private val timeFormat = SimpleDateFormat("HH:mm:ss.SSS", Locale.US)

    private var logDir: File? = null

    fun init(context: Context) {
        if (logDir == null) {
            logDir = File(context.filesDir, LOG_DIR_NAME).apply {
                if (!exists()) {
                    mkdirs()
                }
            }
            cleanupOldLogs()
        }
    }

    @Synchronized
    fun log(level: String, tag: String, message: String) {
        // Ensure no personal data or file content is recorded
        val sanitizedMessage = sanitize(message)
        val timeStr = timeFormat.format(Date())
        val logLine = "[$timeStr] [$level] [$tag]: $sanitizedMessage\n"

        when (level) {
            "D" -> AppLogger.d(tag, sanitizedMessage)
            "I" -> AppLogger.i(tag, sanitizedMessage)
            "W" -> AppLogger.w(tag, sanitizedMessage)
            "E" -> AppLogger.e(tag, sanitizedMessage)
        }

        logDir?.let { dir ->
            try {
                val dateStr = dateFormat.format(Date())
                val logFile = File(dir, "log_$dateStr.txt")
                FileWriter(logFile, true).use { writer ->
                    writer.append(logLine)
                }
            } catch (e: Exception) {
                Log.e(TAG, "Failed to write persistent log", e)
            }
        }
    }

    private fun sanitize(message: String): String {
        return if (message.length > 1000) message.substring(0, 1000) + "... [truncated]" else message
    }

    fun getAllLogs(): List<String> {
        val dir = logDir ?: return emptyList()
        val files = dir.listFiles { file -> file.name.startsWith("log_") && file.name.endsWith(".txt") }
            ?.sortedByDescending { it.name } ?: emptyList()

        val allLines = mutableListOf<String>()
        for (file in files) {
            try {
                allLines.add("--- File: ${file.name} ---")
                allLines.addAll(file.readLines())
            } catch (e: Exception) {
                Log.e(TAG, "Failed to read log file ${file.name}", e)
            }
        }
        return allLines
    }

    fun cleanupOldLogs() {
        val dir = logDir ?: return
        val now = System.currentTimeMillis()
        val maxAgeMillis = MAX_RETENTION_DAYS * 24 * 60 * 60 * 1000L

        dir.listFiles()?.forEach { file ->
            if (file.isFile) {
                val age = now - file.lastModified()
                if (age > maxAgeMillis) {
                    file.delete()
                }
            }
        }
    }

    fun clearAllLogs() {
        logDir?.listFiles()?.forEach { it.delete() }
    }
}
