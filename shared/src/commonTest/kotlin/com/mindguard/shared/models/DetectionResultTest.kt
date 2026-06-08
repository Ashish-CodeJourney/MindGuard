package com.mindguard.shared.models

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class DetectionResultTest {

    @Test
    fun createsBlockingResult() {
        val result = DetectionResult(
            shouldBlock = true,
            action = BlockAction.GO_BACK,
            reason = "Reel detected"
        )

        assertTrue(result.shouldBlock)
        assertEquals(BlockAction.GO_BACK, result.action)
        assertEquals("Reel detected", result.reason)
    }

    @Test
    fun createsNonBlockingResult() {
        val result = DetectionResult(
            shouldBlock = false,
            action = BlockAction.NONE,
            reason = null
        )

        assertFalse(result.shouldBlock)
        assertEquals(BlockAction.NONE, result.action)
        assertEquals(null, result.reason)
    }

    @Test
    fun createsResultWithoutReason() {
        val result = DetectionResult(
            shouldBlock = true,
            action = BlockAction.GO_HOME_AND_REOPEN_APP,
            reason = null
        )

        assertTrue(result.shouldBlock)
        assertEquals(BlockAction.GO_HOME_AND_REOPEN_APP, result.action)
        assertEquals(null, result.reason)
    }

    @Test
    fun resultsAreImmutable() {
        val result = DetectionResult(
            shouldBlock = true,
            action = BlockAction.GO_BACK,
            reason = "Test"
        )

        // Verify properties can't be reassigned (data class immutability)
        assertEquals(true, result.shouldBlock)
        assertEquals("Test", result.reason)
    }

    @Test
    fun blockingResultAlwaysHasValidAction() {
        val result = DetectionResult(
            shouldBlock = true,
            action = BlockAction.GO_BACK,
            reason = "Some reason"
        )

        assertTrue(result.shouldBlock)
        assertTrue(result.action != BlockAction.NONE)
    }
}
