package com.github.tyamada.mihirakipdfviewer_android

import androidx.compose.runtime.saveable.SaverScope
import com.github.tyamada.mihirakipdfviewer_android.ui.screens.BadgeDetailsState
import org.junit.Assert.*
import org.junit.Test

class BadgeDetailsStateTest {
    private val scope = object : SaverScope { override fun canBeSaved(value: Any) = true }
    private fun recreate(state: BadgeDetailsState): BadgeDetailsState {
        val saved = with(BadgeDetailsState.Saver) { scope.save(state) }!!
        return BadgeDetailsState.Saver.restore(saved)!!
    }
    @Test fun closingAfterRecreationReturnsToTheOriginalSource() {
        val state = BadgeDetailsState().apply { open("KOYOMI", 101) }
        val restored = recreate(state)
        assertEquals("KOYOMI", restored.badge)
        assertEquals(-1, restored.pendingReturnId)
        restored.close()
        assertNull(restored.badge)
        assertEquals(101, restored.pendingReturnId)
    }
    @Test fun theSameBadgeCanReturnToDifferentSourceButtons() {
        val state = BadgeDetailsState().apply { open("KOYOMI", 101); close(); finishReturn(); open("KOYOMI", 202) }
        recreate(state).apply { close(); assertEquals(202, pendingReturnId) }
    }
    @Test fun pendingCloseSurvivesAConcurrentRecreation() {
        val state = BadgeDetailsState().apply { open("BRONZE", 303); close() }
        val restored = recreate(state)
        assertNull(restored.badge)
        assertEquals(303, restored.pendingReturnId)
    }
    @Test fun reopeningCancelsThePreviousReturnRequest() {
        val state = BadgeDetailsState().apply { open("BRONZE", 101); close(); open("GOLD", 202) }
        assertEquals(-1, state.pendingReturnId)
        state.close()
        assertEquals(202, state.pendingReturnId)
    }
    @Test fun completedReturnIsNotRepeatedOnRecompositionOrRecreation() {
        val state = BadgeDetailsState().apply { open("SILVER", 101); close(); finishReturn() }
        assertEquals(-1, recreate(state).pendingReturnId)
    }
    @Test fun normalStartupDoesNotRequestAnyFocus() {
        assertNull(BadgeDetailsState().badge)
        assertEquals(-1, recreate(BadgeDetailsState()).pendingReturnId)
    }
}
