package com.mindguard.accessibility

import android.accessibilityservice.AccessibilityService
import android.view.accessibility.AccessibilityEvent
import com.mindguard.shared.rules.InstagramReelRule
import com.mindguard.shared.rules.RuleEngine
import com.mindguard.shared.usecases.BlockCooldown
import com.mindguard.shared.usecases.DetectBlockedContentUseCase

class MindGuardAccessibilityService : AccessibilityService() {

    private lateinit var converter: AccessibilityEventConverter
    private lateinit var debouncer: AccessibilityEventDebouncer
    private lateinit var detector: DetectBlockedContentUseCase
    private lateinit var cooldown: BlockCooldown

    override fun onCreate() {
        super.onCreate()

        converter = AccessibilityEventConverter()
        debouncer = AccessibilityEventDebouncer(debounceMs = 100L)

        val instagramRule = InstagramReelRule()
        val ruleEngine = RuleEngine(listOf(instagramRule))
        detector = DetectBlockedContentUseCase(ruleEngine) { message ->
            logDebug(message)
        }

        cooldown = BlockCooldown(cooldownMs = 2000L)

        logDebug("MindGuard Accessibility Service created")
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        if (event == null) return

        val shouldProcess = debouncer.debounce {
            processEvent(event)
        }

        if (shouldProcess) {
            processEvent(event)
        }
    }

    private fun processEvent(event: AccessibilityEvent) {
        try {
            val snapshot = converter.toScreenSnapshot(event) ?: return
            val now = System.currentTimeMillis()

            if (!cooldown.canBlock(now)) {
                return
            }

            val result = detector.execute(snapshot)

            if (result.shouldBlock) {
                cooldown.recordBlock(now)
                executeBlockAction(result.action)
            }
        } catch (e: Exception) {
            logDebug("Error processing accessibility event: ${e.message}")
        }
    }

    private fun executeBlockAction(action: com.mindguard.shared.models.BlockAction) {
        // Action execution will be implemented in next steps
        logDebug("Block action: $action")
    }

    override fun onInterrupt() {
        logDebug("MindGuard Accessibility Service interrupted")
    }

    override fun onDestroy() {
        logDebug("MindGuard Accessibility Service destroyed")
        super.onDestroy()
    }

    private fun logDebug(message: String) {
        android.util.Log.d("MindGuard", message)
    }
}
