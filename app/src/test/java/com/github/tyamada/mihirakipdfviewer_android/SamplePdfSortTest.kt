package com.github.tyamada.mihirakipdfviewer_android

import com.github.tyamada.mihirakipdfviewer_android.data.*
import org.junit.Assert.*
import org.junit.Test

class SamplePdfSortTest {
    private val items = SamplePdfCatalog.items.map { SamplePdfItemState(it) }
    private fun episode(item: SamplePdfItemState) = item.item.id.removePrefix("ep").substringBefore("_").toInt()

    @Test fun defaultIsLatestFirst() {
        assertEquals(SamplePdfSortOrder.DESC, ViewerSettings().samplePdfSortOrder)
    }
    @Test fun bothOrdersAndLanguageTies() {
        for (order in SamplePdfSortOrder.entries) {
            val sorted = sortSamplePdfs(items.reversed(), order)
            val episodes = sorted.map(::episode)
            assertEquals(if (order == SamplePdfSortOrder.ASC) episodes.sorted() else episodes.sortedDescending(), episodes)
            sorted.groupBy(::episode).values.forEach { group ->
                assertEquals(SamplePdfCatalog.getAvailableLanguages(), group.map { it.item.languageCode })
            }
            assertEquals(items.map { it.item.id }.toSet(), sorted.map { it.item.id }.toSet())
        }
    }
    @Test fun filteringAndDownloadRefreshPreserveOrder() {
        for (order in SamplePdfSortOrder.entries) {
            val expected = sortSamplePdfs(items, order).filter { it.item.languageCode == "en" }.map { it.item.id }
            val changed = items.filter { it.item.languageCode == "en" }.mapIndexed { i, it -> it.copy(isDownloaded = i % 2 == 0, progress = 0.5f) }
            assertEquals(expected, sortSamplePdfs(changed, order).map { it.item.id })
        }
    }
}
