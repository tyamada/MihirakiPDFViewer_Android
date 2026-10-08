package com.github.tyamada.mihirakipdfviewer_android.util

import android.util.Log
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

data class LogEntry(
    val timestamp: String,
    val level: String,
    val tag: String,
    val message: String
)

object AppLogger {
    private const val MAX_LOGS = 200
    private val dateFormat = SimpleDateFormat("MM-dd HH:mm:ss.SSS", Locale.US)

    private val _logs = MutableStateFlow<List<LogEntry>>(emptyList())
    val logs: StateFlow<List<LogEntry>> = _logs.asStateFlow()

    fun d(tag: String, message: String) {
        Log.d(tag, message)
        addLog("D", tag, message)
    }

    fun i(tag: String, message: String) {
        Log.i(tag, message)
        addLog("I", tag, message)
    }

    fun w(tag: String, message: String) {
        Log.w(tag, message)
        addLog("W", tag, message)
    }

    fun e(tag: String, message: String, throwable: Throwable? = null) {
        val fullMessage = if (throwable != null) "$message\n${Log.getStackTraceString(throwable)}" else message
        Log.e(tag, message, throwable)
        addLog("E", tag, fullMessage)
    }

    @Synchronized
    private fun addLog(level: String, tag: String, message: String) {
        val entry = LogEntry(
            timestamp = dateFormat.format(Date()),
            level = level,
            tag = tag,
            message = message
        )
        val current = _logs.value.toMutableList()
        current.add(entry)
        if (current.size > MAX_LOGS) {
            current.removeAt(0)
        }
        _logs.value = current
    }

    fun clear() {
        _logs.value = emptyList()
    }

    fun exportLogs(): String {
        return _logs.value.joinToString("\n") { "[${it.timestamp}] [${it.level}] [${it.tag}] ${it.message}" }
    }
}
