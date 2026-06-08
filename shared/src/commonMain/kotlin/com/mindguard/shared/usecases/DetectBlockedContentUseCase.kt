package com.mindguard.shared.usecases

import com.mindguard.shared.models.DetectionResult
import com.mindguard.shared.models.ScreenSnapshot
import com.mindguard.shared.rules.RuleEngine

class DetectBlockedContentUseCase(
    private val ruleEngine: RuleEngine,
    private val logger: (String) -> Unit = {}
) {

    fun execute(snapshot: ScreenSnapshot): DetectionResult {
        val result = ruleEngine.evaluate(snapshot)

        if (result.shouldBlock) {
            val message = buildLogMessage(snapshot, result)
            logger(message)
        }

        return result
    }

    private fun buildLogMessage(snapshot: ScreenSnapshot, result: DetectionResult): String {
        return buildString {
            append("Detected blocked content in ")
            append(snapshot.packageName)
            if (result.reason != null) {
                append(": ")
                append(result.reason)
            }
        }
    }
}
