package com.mindguard.accessibility

import android.accessibilityservice.AccessibilityService
import android.view.accessibility.AccessibilityEvent
import com.mindguard.shared.rules.InstagramReelRule
import com.mindguard.shared.rules.RuleEngine
import com.mindguard.shared.usecases.BlockCooldown
import com.mindguard.shared.usecases.DetectBlockedContentUseCase
import com.mindguard.storage.SettingsDataStore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

class MindGuardAccessibilityService : AccessibilityService(), KoinComponent {

    private val settingsDataStore: SettingsDataStore by inject()
    private val serviceScope = CoroutineScope(Dispatchers.IO + SupervisorJob())

    private lateinit var converter: AccessibilityEventConverter
    private lateinit var debouncer: AccessibilityEventDebouncer
    private lateinit var detector: DetectBlockedContentUseCase
    private lateinit var cooldown: BlockCooldown
    private lateinit var executor: BlockActionExecutor

    override fun onCreate() {
        super.onCreate()
        converter = AccessibilityEventConverter()
        debouncer  = AccessibilityEventDebouncer(debounceMs = 100L)
        executor   = BlockActionExecutor(this)
        detector   = DetectBlockedContentUseCase(RuleEngine(listOf(InstagramReelRule())))
        cooldown   = BlockCooldown(cooldownMs = 2_000L)
        serviceScope.launch { settingsDataStore.resetDailyCountsIfNeeded() }
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        if (event == null) return
        if (debouncer.debounce { processEvent(event) }) processEvent(event)
    }

    private fun processEvent(event: AccessibilityEvent) {
        try {
            val snapshot = converter.toScreenSnapshot(event) ?: return
            val result   = detector.execute(snapshot)
            if (!result.shouldBlock) return

            val now = System.currentTimeMillis()
            serviceScope.launch {
                if (!settingsDataStore.protectionEnabledFlow.first()) return@launch
                settingsDataStore.recordAttempt()
                if (cooldown.canBlock(now)) {
                    cooldown.recordBlock(now)
                    settingsDataStore.recordBlock()
                    executeBlockAction(result.action)
                }
            }
        } catch (e: Exception) {
            android.util.Log.e("MindGuard", "processEvent error: ${e.message}")
        }
    }

    private fun executeBlockAction(action: com.mindguard.shared.models.BlockAction) {
        try { executor.execute(action) } catch (e: Exception) {
            android.util.Log.e("MindGuard", "executeBlockAction error: ${e.message}")
        }
    }

    override fun onInterrupt() = Unit
    override fun onDestroy() { serviceScope.cancel(); super.onDestroy() }
}
