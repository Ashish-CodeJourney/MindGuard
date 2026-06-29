package com.mindguard.accessibility

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class WatchdogIntervalTest {

    @Test
    fun watchdogIntervalIs300ms() {
        assertEquals(300L, MindGuardAccessibilityService.WATCHDOG_INTERVAL_MS)
    }

    @Test
    fun watchdogIntervalIsUnder500ms() {
        assertTrue(MindGuardAccessibilityService.WATCHDOG_INTERVAL_MS < 500L,
            "Watchdog must respond within 500ms to feel instant to users")
    }

    @Test
    fun postBlockPauseIsLongerThanWatchdogInterval() {
        assertTrue(MindGuardAccessibilityService.POST_BLOCK_PAUSE_MS > MindGuardAccessibilityService.WATCHDOG_INTERVAL_MS,
            "POST_BLOCK_PAUSE must be longer than watchdog interval to prevent re-trigger on same frame")
    }
}
