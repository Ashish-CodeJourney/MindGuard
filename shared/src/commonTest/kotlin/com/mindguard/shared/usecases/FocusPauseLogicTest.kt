package com.mindguard.shared.usecases

import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class FocusPauseLogicTest {

    @Test
    fun notPausedWhenPauseUntilIsZero() {
        assertFalse(isPausedAt(pauseUntilMs = 0L, currentTimeMs = 1000L))
    }

    @Test
    fun pausedWhenCurrentTimeIsBeforePauseDeadline() {
        assertTrue(isPausedAt(pauseUntilMs = 5000L, currentTimeMs = 3000L))
    }

    @Test
    fun notPausedWhenCurrentTimeExceedsPauseDeadline() {
        assertFalse(isPausedAt(pauseUntilMs = 3000L, currentTimeMs = 5000L))
    }

    @Test
    fun notPausedAtExactPauseDeadline() {
        assertFalse(isPausedAt(pauseUntilMs = 3000L, currentTimeMs = 3000L))
    }

    @Test
    fun notPausedWhenPauseUntilIsNegative() {
        assertFalse(isPausedAt(pauseUntilMs = -1L, currentTimeMs = 0L))
    }

    @Test
    fun pauseAppliesForFullDuration() {
        val pauseUntil = 1_800_000L  // 30 min from t=0
        assertTrue(isPausedAt(pauseUntilMs = pauseUntil, currentTimeMs = 0L))
        assertTrue(isPausedAt(pauseUntilMs = pauseUntil, currentTimeMs = 1_799_999L))
        assertFalse(isPausedAt(pauseUntilMs = pauseUntil, currentTimeMs = 1_800_000L))
    }
}
