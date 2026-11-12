package com.github.tyamada.mihirakipdfviewer_android.util

import java.io.File

/** Called once at process startup, before any document can be opened. */
internal fun cleanupStalePdfCopies(cacheDir: File) {
    cacheDir.listFiles()?.filter { file ->
        file.isFile && ((file.name.startsWith("mihiraki_") && file.name.endsWith(".pdf")))
    }?.forEach { it.delete() }
}
