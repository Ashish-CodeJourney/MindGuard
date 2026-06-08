package com.mindguard.shared.integration

import com.mindguard.shared.models.BlockAction
import com.mindguard.shared.models.ScreenSnapshot
import com.mindguard.shared.rules.InstagramReelRule
import com.mindguard.shared.rules.RuleEngine
import com.mindguard.shared.usecases.BlockCooldown
import com.mindguard.shared.usecases.DetectBlockedContentUseCase
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class DetectionIntegrationTest {

    private fun createInstagramSnapshot(
        screenText: List<String> = listOf("Reels"),
        resourceIds: List<String> = listOf("com.instagram.android:id/reel_pager")
    ): ScreenSnapshot {
        return ScreenSnapshot(
            packageName = "com.instagram.android",
            screenText = screenText,
            contentDescriptions = emptyList(),
            resourceIds = resourceIds,
            timestampMillis = 1000L
        )
    }

    @Test
    fun fullDetectionFlowForReel() {
        val rule = InstagramReelRule()
        val engine = RuleEngine(listOf(rule))
        val useCase = DetectBlockedContentUseCase(engine)
        val cooldown = BlockCooldown(cooldownMs = 2000)

        val snapshot = createInstagramSnapshot()
        val now = 1000L

        // Flow: Detect → Check Cooldown → Execute Action
        val detectionResult = useCase.execute(snapshot)

        assertTrue(detectionResult.shouldBlock)
        assertEquals(BlockAction.GO_BACK, detectionResult.action)
        assertTrue(cooldown.canBlock(now))

        // Record block, cooldown should prevent immediate re-block
        cooldown.recordBlock(now)
        assertFalse(cooldown.canBlock(now + 1000)) // 1 second later - blocked
        assertTrue(cooldown.canBlock(now + 2100))  // 2.1 seconds later - allowed
    }

    @Test
    fun dontBlockFeedWhenNoReel() {
        val rule = InstagramReelRule()
        val engine = RuleEngine(listOf(rule))
        val useCase = DetectBlockedContentUseCase(engine)

        val snapshot = createInstagramSnapshot(
            screenText = listOf("Explore"),
            resourceIds = listOf("com.instagram.android:id/feed_pager")
        )

        val result = useCase.execute(snapshot)

        assertFalse(result.shouldBlock)
        assertEquals(BlockAction.NONE, result.action)
    }

    @Test
    fun preventInfiniteBlockLoopsWithCooldown() {
        val rule = InstagramReelRule()
        val engine = RuleEngine(listOf(rule))
        val useCase = DetectBlockedContentUseCase(engine)
        val cooldown = BlockCooldown(cooldownMs = 2000)

        val snapshot = createInstagramSnapshot()
        var blockCount = 0
        var successfulBlocks = 0

        // Simulate rapid events within 2-second window (0..9 → t=1000..2800, before cooldown expires at 3000)
        for (i in 0..9) {
            val now = 1000L + (i * 200) // 200ms between events
            val canBlock = cooldown.canBlock(now)

            if (canBlock) {
                blockCount++
                val result = useCase.execute(snapshot)
                if (result.shouldBlock) {
                    successfulBlocks++
                    cooldown.recordBlock(now)
                }
            }
        }

        // Should only allow one block within 2-second window
        assertEquals(1, successfulBlocks)
        assertTrue(blockCount >= 1)
    }

    @Test
    fun multipleRulesCanBeEvaluated() {
        val instagramRule = InstagramReelRule()
        val customRule = CustomDetectionRule()
        val engine = RuleEngine(listOf(instagramRule, customRule))
        val useCase = DetectBlockedContentUseCase(engine)

        val snapshot = createInstagramSnapshot()

        val result = useCase.execute(snapshot)

        // Instagram rule should match first
        assertTrue(result.shouldBlock)
        assertEquals(BlockAction.GO_BACK, result.action)
    }

    @Test
    fun ruleEngineShortCircuitsOnFirstMatch() {
        var instagramRuleExecuted = false
        var customRuleExecuted = false

        val instagramRule = object : com.mindguard.shared.rules.BlockingRule {
            override fun evaluate(snapshot: ScreenSnapshot): com.mindguard.shared.models.DetectionResult {
                instagramRuleExecuted = true
                return com.mindguard.shared.models.DetectionResult(
                    shouldBlock = true,
                    action = BlockAction.GO_BACK,
                    reason = "Instagram"
                )
            }
        }

        val customRule = object : com.mindguard.shared.rules.BlockingRule {
            override fun evaluate(snapshot: ScreenSnapshot): com.mindguard.shared.models.DetectionResult {
                customRuleExecuted = true
                return com.mindguard.shared.models.DetectionResult(shouldBlock = false, action = BlockAction.NONE, reason = null)
            }
        }

        val engine = RuleEngine(listOf(instagramRule, customRule))
        val snapshot = createInstagramSnapshot()

        engine.evaluate(snapshot)

        assertTrue(instagramRuleExecuted)
        assertFalse(customRuleExecuted) // Should not execute - short-circuit
    }

    private class CustomDetectionRule : com.mindguard.shared.rules.BlockingRule {
        override fun evaluate(snapshot: ScreenSnapshot): com.mindguard.shared.models.DetectionResult {
            return com.mindguard.shared.models.DetectionResult(
                shouldBlock = false,
                action = BlockAction.NONE,
                reason = null
            )
        }
    }
}
