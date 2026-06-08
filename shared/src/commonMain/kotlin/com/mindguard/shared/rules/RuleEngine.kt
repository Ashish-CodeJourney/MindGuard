package com.mindguard.shared.rules

import com.mindguard.shared.models.BlockAction
import com.mindguard.shared.models.DetectionResult
import com.mindguard.shared.models.ScreenSnapshot

class RuleEngine(private val rules: List<BlockingRule>) {

    fun evaluate(snapshot: ScreenSnapshot): DetectionResult {
        for (rule in rules) {
            val result = rule.evaluate(snapshot)
            if (result.shouldBlock) {
                return result
            }
        }
        return DetectionResult(shouldBlock = false, action = BlockAction.NONE, reason = null)
    }
}
