package com.github.tyamada.mihirakipdfviewer_android

import androidx.test.platform.app.InstrumentationRegistry
import com.github.tyamada.mihirakipdfviewer_android.data.*
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.first
import org.junit.Assert.*
import org.junit.Test

class SampleSortPersistenceTest {
    @Test fun atomicToggleSurvivesStaleViewerSaveAndRepositoryRecreation() = runBlocking {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val repository = SettingsRepository(context)
        val initial = repository.settings.first()
        try {
            repository.toggleSamplePdfSortOrder()
            val expected = if (initial.samplePdfSortOrder == SamplePdfSortOrder.DESC) SamplePdfSortOrder.ASC else SamplePdfSortOrder.DESC
            repository.save(initial.copy(sharpness = 0.2f))
            assertEquals(expected, SettingsRepository(context).settings.first().samplePdfSortOrder)
            assertEquals(0.2f, repository.settings.first().sharpness)
            coroutineScope { repeat(20) { launch { repository.toggleSamplePdfSortOrder() } } }
            assertEquals(expected, repository.settings.first().samplePdfSortOrder)
        } finally {
            if (repository.settings.first().samplePdfSortOrder != initial.samplePdfSortOrder) repository.toggleSamplePdfSortOrder()
            repository.save(initial)
        }
    }
}
