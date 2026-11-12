package com.github.tyamada.mihirakipdfviewer_android.util

import org.junit.Assert.*
import org.junit.Test
import java.nio.file.Files
import java.io.File

class PdfCacheCleanupTest {
    @Test fun `startup removes abandoned document copies but preserves unrelated files`() {
        val dir = Files.createTempDirectory("pdf-cache-test").toFile()
        try {
            val stale = File(dir, "mihiraki_abandoned.pdf").apply { writeText("private document") }
            val other = File(dir, "user.pdf").apply { writeText("keep") }
            val wrongExtension = File(dir, "mihiraki_notes.txt").apply { writeText("keep") }
            val directory = File(dir, "mihiraki_directory.pdf").apply { mkdir() }
            val nested = File(directory, "keep.pdf").apply { writeText("keep") }
            cleanupStalePdfCopies(dir)
            cleanupStalePdfCopies(dir)
            assertFalse(stale.exists())
            assertTrue(other.exists())
            assertTrue(wrongExtension.exists())
            assertTrue(nested.exists())
        } finally { dir.deleteRecursively() }
    }
}
