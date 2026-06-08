package com.mindguard.shared.rules

import com.mindguard.shared.models.BlockAction
import com.mindguard.shared.models.DetectionResult
import com.mindguard.shared.models.ScreenSnapshot
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class RuleEngineTest {

    private fun createTestSnapshot(packageName: String = "com.test"): ScreenSnapshot {
        return ScreenSnapshot(
            packageName = packageName,
            screenText = emptyList(),
            contentDescriptions = emptyList(),
            resourceIds = emptyList(),
            timestampMillis = 1000L
        )
    }

    @Test
    fun evaluatesRulesInOrder() {
        val rule1 = FakeRule(shouldMatch = false)
        val rule2 = FakeRule(shouldMatch = true)
        val rule3 = FakeRule(shouldMatch = true)

        val engine = RuleEngine(listOf(rule1, rule2, rule3))
        val snapshot = createTestSnapshot()

        val result = engine.evaluate(snapshot)

        assertTrue(result.shouldBlock)
        assertEquals(BlockAction.GO_BACK, result.action)
        assertEquals(1, rule2.evaluationCount) // rule2 should be evaluated
        assertEquals(0, rule3.evaluationCount) // rule3 should NOT be evaluated (short-circuit)
    }

    @Test
    fun returnsNoneWhenNoRulesMatch() {
        val rule1 = FakeRule(shouldMatch = false)
        val rule2 = FakeRule(shouldMatch = false)

        val engine = RuleEngine(listOf(rule1, rule2))
        val snapshot = createTestSnapshot()

        val result = engine.evaluate(snapshot)

        assertFalse(result.shouldBlock)
        assertEquals(BlockAction.NONE, result.action)
        assertEquals(2, rule1.evaluationCount)
        assertEquals(2, rule2.evaluationCount)
    }

    @Test
    fun handlesEmptyRuleList() {
        val engine = RuleEngine(emptyList())
        val snapshot = createTestSnapshot()

        val result = engine.evaluate(snapshot)

        assertFalse(result.shouldBlock)
        assertEquals(BlockAction.NONE, result.action)
    }

    @Test
    fun firstMatchingRuleWins() {
        val rule1 = FakeRule(shouldMatch = true, action = BlockAction.GO_HOME_AND_REOPEN_APP)
        val rule2 = FakeRule(shouldMatch = true, action = BlockAction.GO_BACK)

        val engine = RuleEngine(listOf(rule1, rule2))
        val snapshot = createTestSnapshot()

        val result = engine.evaluate(snapshot)

        assertTrue(result.shouldBlock)
        assertEquals(BlockAction.GO_HOME_AND_REOPEN_APP, result.action)
        assertEquals(1, rule1.evaluationCount)
        assertEquals(0, rule2.evaluationCount) // short-circuit
    }

    @Test
    fun passesSnapshotToAllRulesUntilMatch() {
        val capturedSnapshots1 = mutableListOf<ScreenSnapshot>()
        val capturedSnapshots2 = mutableListOf<ScreenSnapshot>()

        val rule1 = FakeRule(
            shouldMatch = false,
            onEvaluate = { snapshot -> capturedSnapshots1.add(snapshot) }
        )
        val rule2 = FakeRule(
            shouldMatch = false,
            onEvaluate = { snapshot -> capturedSnapshots2.add(snapshot) }
        )

        val engine = RuleEngine(listOf(rule1, rule2))
        val snapshot = createTestSnapshot(packageName = "com.instagram.android")

        engine.evaluate(snapshot)

        assertEquals(1, capturedSnapshots1.size)
        assertEquals("com.instagram.android", capturedSnapshots1[0].packageName)
        assertEquals(1, capturedSnapshots2.size)
        assertEquals("com.instagram.android", capturedSnapshots2[0].packageName)
    }

    private class FakeRule(
        private val shouldMatch: Boolean,
        private val action: BlockAction = BlockAction.GO_BACK,
        private val onEvaluate: (ScreenSnapshot) -> Unit = {}
    ) : BlockingRule {
        var evaluationCount = 0

        override fun evaluate(snapshot: ScreenSnapshot): DetectionResult {
            evaluationCount++
            onEvaluate(snapshot)
            return if (shouldMatch) {
                DetectionResult(shouldBlock = true, action = action, reason = "Fake rule matched")
            } else {
                DetectionResult(shouldBlock = false, action = BlockAction.NONE, reason = null)
            }
        }
    }
}
