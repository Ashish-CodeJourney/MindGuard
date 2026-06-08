package com.mindguard.shared.usecases

import com.mindguard.shared.models.BlockAction
import com.mindguard.shared.models.DetectionResult
import com.mindguard.shared.models.ScreenSnapshot
import com.mindguard.shared.rules.BlockingRule
import com.mindguard.shared.rules.RuleEngine
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class DetectBlockedContentUseCaseTest {

    private fun createSnapshot(
        packageName: String = "com.instagram.android",
        screenText: List<String> = emptyList()
    ): ScreenSnapshot {
        return ScreenSnapshot(
            packageName = packageName,
            screenText = screenText,
            contentDescriptions = emptyList(),
            resourceIds = emptyList(),
            timestampMillis = 1000L
        )
    }

    @Test
    fun detectsBlockedContent() {
        val blockingRule = FakeRule(shouldBlock = true)
        val engine = RuleEngine(listOf(blockingRule))
        val useCase = DetectBlockedContentUseCase(engine)

        val snapshot = createSnapshot()
        val result = useCase.execute(snapshot)

        assertTrue(result.shouldBlock)
        assertEquals(BlockAction.GO_BACK, result.action)
    }

    @Test
    fun returnsNoneForNormalContent() {
        val blockingRule = FakeRule(shouldBlock = false)
        val engine = RuleEngine(listOf(blockingRule))
        val useCase = DetectBlockedContentUseCase(engine)

        val snapshot = createSnapshot()
        val result = useCase.execute(snapshot)

        assertFalse(result.shouldBlock)
        assertEquals(BlockAction.NONE, result.action)
    }

    @Test
    fun logsDetectionReason() {
        val blockingRule = FakeRule(shouldBlock = true, reason = "Test detection")
        val engine = RuleEngine(listOf(blockingRule))
        val logCapture = mutableListOf<String>()
        val useCase = DetectBlockedContentUseCase(engine) { message -> logCapture.add(message) }

        val snapshot = createSnapshot()
        val result = useCase.execute(snapshot)

        assertTrue(result.shouldBlock)
        assertTrue(logCapture.isNotEmpty())
        assertTrue(logCapture[0].contains("Detected"))
        assertTrue(logCapture[0].contains("Test detection"))
    }

    @Test
    fun logsPackageNameInDetection() {
        val blockingRule = FakeRule(shouldBlock = true)
        val engine = RuleEngine(listOf(blockingRule))
        val logCapture = mutableListOf<String>()
        val useCase = DetectBlockedContentUseCase(engine) { message -> logCapture.add(message) }

        val snapshot = createSnapshot(packageName = "com.youtube.android")
        val result = useCase.execute(snapshot)

        assertTrue(result.shouldBlock)
        assertTrue(logCapture.isNotEmpty())
        assertTrue(logCapture[0].contains("com.youtube.android"))
    }

    @Test
    fun delegatesToRuleEngine() {
        val blockingRule = FakeRule(shouldBlock = true, reason = "Rule matched")
        val engine = RuleEngine(listOf(blockingRule))
        val useCase = DetectBlockedContentUseCase(engine)

        val snapshot = createSnapshot()
        val result = useCase.execute(snapshot)

        assertEquals(1, blockingRule.evaluationCount)
        assertTrue(result.shouldBlock)
        assertEquals("Rule matched", result.reason)
    }

    @Test
    fun isPureFunction() {
        val blockingRule = FakeRule(shouldBlock = true)
        val engine = RuleEngine(listOf(blockingRule))
        val useCase = DetectBlockedContentUseCase(engine)

        val snapshot = createSnapshot()
        val result1 = useCase.execute(snapshot)
        val result2 = useCase.execute(snapshot)

        assertEquals(result1.shouldBlock, result2.shouldBlock)
        assertEquals(result1.action, result2.action)
    }

    @Test
    fun logsWithoutReasonIfNull() {
        val blockingRule = FakeRule(shouldBlock = true, reason = null)
        val engine = RuleEngine(listOf(blockingRule))
        val logCapture = mutableListOf<String>()
        val useCase = DetectBlockedContentUseCase(engine) { message -> logCapture.add(message) }

        val snapshot = createSnapshot()
        useCase.execute(snapshot)

        assertTrue(logCapture.isNotEmpty())
        assertFalse(logCapture[0].contains("null"))
    }

    private class FakeRule(
        private val shouldBlock: Boolean,
        private val reason: String? = null
    ) : BlockingRule {
        var evaluationCount = 0

        override fun evaluate(snapshot: ScreenSnapshot): DetectionResult {
            evaluationCount++
            return if (shouldBlock) {
                DetectionResult(shouldBlock = true, action = BlockAction.GO_BACK, reason = reason)
            } else {
                DetectionResult(shouldBlock = false, action = BlockAction.NONE, reason = null)
            }
        }
    }
}
