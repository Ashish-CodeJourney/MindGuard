package com.mindguard.shared.integration

import com.mindguard.shared.models.BlockAction
import com.mindguard.shared.models.ScreenSnapshot
import com.mindguard.shared.rules.InstagramReelRule
import com.mindguard.shared.rules.RuleEngine
import com.mindguard.shared.usecases.BlockCooldown
import com.mindguard.shared.usecases.DetectBlockedContentUseCase
import com.mindguard.shared.usecases.isPausedAt
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class FocusModeIntegrationTest {

    private val now = 100_000L

    // ── isPausedAt boundary conditions ───────────────────────────────────

    @Test
    fun isPausedBlockingDelegatesToPauseLogic_notPausedWhenZero() {
        assertFalse(isPausedAt(pauseUntilMs = 0L, currentTimeMs = now))
    }

    @Test
    fun isPausedBlockingDelegatesToPauseLogic_pausedBeforeDeadline() {
        assertTrue(isPausedAt(pauseUntilMs = now + 30 * 60_000L, currentTimeMs = now))
    }

    @Test
    fun isPausedBlockingDelegatesToPauseLogic_notPausedAtExactDeadline() {
        assertFalse(isPausedAt(pauseUntilMs = now, currentTimeMs = now))
    }

    @Test
    fun isPausedBlockingDelegatesToPauseLogic_notPausedAfterDeadline() {
        assertFalse(isPausedAt(pauseUntilMs = now - 1L, currentTimeMs = now))
    }

    // ── Pause and RuleEngine are independent ─────────────────────────────

    @Test
    fun pauseAndDetectionAreIndependent() {
        val engine = RuleEngine(listOf(InstagramReelRule()))
        val useCase = DetectBlockedContentUseCase(engine)
        val snapshot = ScreenSnapshot(
            packageName = "com.instagram.android",
            screenText = emptyList(),
            contentDescriptions = emptyList(),
            resourceIds = listOf("com.instagram.android:id/reel_pager"),
            timestampMillis = now
        )

        val paused = isPausedAt(pauseUntilMs = now + 30 * 60_000L, currentTimeMs = now)
        val detectionResult = useCase.execute(snapshot)

        assertTrue(paused, "Focus mode should be paused")
        assertTrue(detectionResult.shouldBlock, "RuleEngine is unaware of pause — it always fires")
        assertEquals(BlockAction.CLICK_SAFE_TAB, detectionResult.action)
    }

    // ── Cooldown boundaries ───────────────────────────────────────────────

    @Test
    fun cooldownRemainsActiveAcrossMultipleDetectionCalls() {
        val engine = RuleEngine(listOf(InstagramReelRule()))
        val useCase = DetectBlockedContentUseCase(engine)
        val cooldown = BlockCooldown(cooldownMs = 500)
        val snapshot = ScreenSnapshot(
            packageName = "com.instagram.android",
            screenText = emptyList(),
            contentDescriptions = emptyList(),
            resourceIds = listOf("com.instagram.android:id/reel_pager"),
            timestampMillis = now
        )
        var blockCount = 0

        for (i in 0..9) {
            val t = now + (i * 40L)
            if (cooldown.canBlock(t)) {
                val result = useCase.execute(snapshot)
                if (result.shouldBlock) {
                    blockCount++
                    cooldown.recordBlock(t)
                }
            }
        }

        assertEquals(1, blockCount, "Only 1 block should occur within the 500ms cooldown window")
    }

    @Test
    fun cooldownResetsAfterWindow() {
        val cooldown = BlockCooldown(cooldownMs = 500)

        cooldown.recordBlock(now)
        assertFalse(cooldown.canBlock(now + 499), "Should still be in cooldown at 499ms")
        assertTrue(cooldown.canBlock(now + 501), "Should be allowed again after 501ms")
    }

    @Test
    fun cooldownAllowsImmediateBlockWithNoHistory() {
        val cooldown = BlockCooldown(cooldownMs = 500)
        assertTrue(cooldown.canBlock(now))
    }
}
