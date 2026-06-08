package com.mindguard.accessibility

import android.accessibilityservice.AccessibilityService
import android.view.accessibility.AccessibilityEvent
import com.mindguard.shared.rules.InstagramReelRule
import com.mindguard.shared.rules.RuleEngine
import com.mindguard.shared.rules.SnapchatSpotlightRule
import com.mindguard.shared.rules.TikTokRule
import com.mindguard.shared.rules.YouTubeShortsRule
import com.mindguard.shared.usecases.BlockCooldown
import com.mindguard.shared.usecases.DetectBlockedContentUseCase
import com.mindguard.storage.SettingsDataStore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import java.util.Calendar

class MindGuardAccessibilityService : AccessibilityService(), KoinComponent {

    private val settingsDataStore: SettingsDataStore by inject()
    private val serviceScope = CoroutineScope(Dispatchers.IO + SupervisorJob())

    private lateinit var converter: AccessibilityEventConverter
    private lateinit var debouncer: AccessibilityEventDebouncer
    private lateinit var cooldown: BlockCooldown
    private lateinit var executor: BlockActionExecutor

    // Full rule engine covering all supported apps — package filter applied before calling it
    private val detector = DetectBlockedContentUseCase(
        RuleEngine(listOf(InstagramReelRule(), YouTubeShortsRule(), TikTokRule(), SnapchatSpotlightRule()))
    )

    // Reactively updated from DataStore subscriptions
    @Volatile private var enabledPackages: Set<String> = SettingsDataStore.run {
        setOf(INSTAGRAM_PKG, YOUTUBE_PKG, TIKTOK_PKG, TIKTOK_PKG2, TIKTOK_PKG3, SNAPCHAT_PKG)
    }
    @Volatile private var focusScheduleEnabled = false
    @Volatile private var focusStartHour       = 9
    @Volatile private var focusEndHour         = 17

    override fun onCreate() {
        super.onCreate()
        converter = AccessibilityEventConverter()
        debouncer = AccessibilityEventDebouncer(debounceMs = 100L)
        executor  = BlockActionExecutor(this)
        cooldown  = BlockCooldown(cooldownMs = 2_000L)

        serviceScope.launch { settingsDataStore.resetDailyCountsIfNeeded() }

        // Subscribe to enabled packages so the filter stays live
        serviceScope.launch {
            settingsDataStore.enabledPackagesFlow.collect { pkgs ->
                enabledPackages = pkgs
            }
        }

        // Subscribe to focus schedule settings
        serviceScope.launch {
            combine(
                settingsDataStore.focusScheduleEnabledFlow,
                settingsDataStore.focusStartHourFlow,
                settingsDataStore.focusEndHourFlow
            ) { enabled, start, end -> Triple(enabled, start, end) }
                .collect { (enabled, start, end) ->
                    focusScheduleEnabled = enabled
                    focusStartHour       = start
                    focusEndHour         = end
                }
        }
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        if (event == null) return
        if (debouncer.debounce { processEvent(event) }) processEvent(event)
    }

    private fun processEvent(event: AccessibilityEvent) {
        try {
            val snapshot = converter.toScreenSnapshot(event) ?: return
            if (snapshot.packageName !in enabledPackages) return

            val result = detector.execute(snapshot)
            if (!result.shouldBlock) return

            val now = System.currentTimeMillis()
            serviceScope.launch {
                val manualOn = settingsDataStore.protectionEnabledFlow.first()
                val isProtected = manualOn || isCurrentlyInFocusHours()
                if (!isProtected) return@launch

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

    private fun isCurrentlyInFocusHours(): Boolean {
        if (!focusScheduleEnabled) return false
        val hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
        return if (focusStartHour <= focusEndHour) {
            hour in focusStartHour..focusEndHour
        } else {
            // overnight range e.g. 22:00 → 06:00
            hour >= focusStartHour || hour <= focusEndHour
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
