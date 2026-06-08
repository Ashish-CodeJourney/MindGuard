package com.mindguard.shared.usecases

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class BlockCooldownTest {

    @Test
    fun allowsFirstBlock() {
        val cooldown = BlockCooldown(cooldownMs = 2000)
        val now = 1000L

        assertTrue(cooldown.canBlock(now))
    }

    @Test
    fun allowsBlockAfterCooldownExpires() {
        val cooldown = BlockCooldown(cooldownMs = 2000)

        assertTrue(cooldown.canBlock(1000L))
        cooldown.recordBlock(1000L)

        assertFalse(cooldown.canBlock(2500L)) // Within 2 second window
        assertTrue(cooldown.canBlock(3100L))  // After 2 second window
    }

    @Test
    fun debounceConsecutiveBlocksWithin2Seconds() {
        val cooldown = BlockCooldown(cooldownMs = 2000)

        assertTrue(cooldown.canBlock(1000L))
        cooldown.recordBlock(1000L)

        assertFalse(cooldown.canBlock(1500L))
        assertFalse(cooldown.canBlock(2000L))
        assertFalse(cooldown.canBlock(2500L))
        assertTrue(cooldown.canBlock(3100L))
    }

    @Test
    fun maintainsCooldownWindow() {
        val cooldown = BlockCooldown(cooldownMs = 2000)

        cooldown.recordBlock(1000L)

        assertFalse(cooldown.canBlock(2999L))
        assertTrue(cooldown.canBlock(3000L))
    }

    @Test
    fun allowsMultipleBlocksAfterCooldownResets() {
        val cooldown = BlockCooldown(cooldownMs = 2000)

        assertTrue(cooldown.canBlock(1000L))
        cooldown.recordBlock(1000L)

        assertFalse(cooldown.canBlock(2000L))

        assertTrue(cooldown.canBlock(3100L))
        cooldown.recordBlock(3100L)

        assertFalse(cooldown.canBlock(4000L))
        assertTrue(cooldown.canBlock(5200L))
    }

    @Test
    fun inMemoryOnlyNotPersisted() {
        val cooldown = BlockCooldown(cooldownMs = 2000)

        cooldown.recordBlock(1000L)

        // Create new instance - no persistence
        val newCooldown = BlockCooldown(cooldownMs = 2000)
        assertTrue(newCooldown.canBlock(1500L))
    }

    @Test
    fun trackLastBlockTime() {
        val cooldown = BlockCooldown(cooldownMs = 2000)

        assertEquals(-1L, cooldown.lastBlockTimeMs)

        cooldown.recordBlock(1000L)
        assertEquals(1000L, cooldown.lastBlockTimeMs)

        cooldown.recordBlock(5000L)
        assertEquals(5000L, cooldown.lastBlockTimeMs)
    }

    @Test
    fun respectsCustomCooldownDuration() {
        val cooldown = BlockCooldown(cooldownMs = 1000)

        cooldown.recordBlock(1000L)

        assertFalse(cooldown.canBlock(1500L))
        assertTrue(cooldown.canBlock(2100L))
    }

    @Test
    fun zeroCooldownAlwaysAllowsBlock() {
        val cooldown = BlockCooldown(cooldownMs = 0)
        cooldown.recordBlock(1000L)
        assertTrue(cooldown.canBlock(1000L), "0ms cooldown means every call is allowed immediately")
    }

    @Test
    fun veryLargeTimestampIsHandledCorrectly() {
        val cooldown = BlockCooldown(cooldownMs = 1000)
        val bigTime = 9_000_000_000_000L
        cooldown.recordBlock(bigTime)
        assertFalse(cooldown.canBlock(bigTime + 999))
        assertTrue(cooldown.canBlock(bigTime + 1000))
    }

    @Test
    fun cooldownWindowExactBoundary() {
        val cooldown = BlockCooldown(cooldownMs = 2000)

        cooldown.recordBlock(1000L)

        // Exactly at window boundary
        assertFalse(cooldown.canBlock(2999L))  // Still in window
        assertTrue(cooldown.canBlock(3000L))   // Outside window
    }

    @Test
    fun preventInfiniteLoops() {
        val cooldown = BlockCooldown(cooldownMs = 2000)

        var blockCount = 0
        // 101 events every 10ms from t=1000 to t=2000 — all within the 2s cooldown window
        for (i in 0..100) {
            val now = 1000L + (i * 10)
            if (cooldown.canBlock(now)) {
                blockCount++
                cooldown.recordBlock(now)
            }
        }

        // Only 1 block allowed: second slot opens at t=3000, beyond the loop's range
        assertEquals(1, blockCount)
    }
}
