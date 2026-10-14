package com.example.personallife

import com.example.personallife.ui.viewmodel.MainUiState
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class StartupAndIconTest {

    @Test
    fun testInitialUiStateShowsInitializing() {
        val state = MainUiState()
        assertTrue("Initial UI state must indicate initializing for splash screen", state.isInitializing)
    }

    @Test
    fun testUiStateAfterInitializationCompletes() {
        val state = MainUiState(isInitializing = false)
        assertFalse("Completed UI state must indicate not initializing", state.isInitializing)
        assertEquals(0, state.currentNavTab)
    }
}
