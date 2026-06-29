package com.mindguard.accessibility

import android.accessibilityservice.AccessibilityService
import android.view.accessibility.AccessibilityEvent
import com.mindguard.shared.models.ScreenSnapshot
import com.mindguard.shared.usecases.isPausedAt
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
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import java.util.Calendar

class MindGuardAccessibilityService : AccessibilityService(), KoinComponent {

    private val settingsDataStore: SettingsDataStore by inject()
    private val serviceScope = CoroutineScope(Dispatchers.Main + SupervisorJob())

    private lateinit var converter: AccessibilityEventConverter
    private lateinit var cooldown: BlockCooldown
    private lateinit var executor: BlockActionExecutor

    private val detector = DetectBlockedContentUseCase(
        RuleEngine(listOf(InstagramReelRule(), YouTubeShortsRule(), TikTokRule(), SnapchatSpotlightRule()))
    )

    // All cached synchronously — no coroutine hop on the hot path
    @Volatile private var enabledPackages: Set<String> = SettingsDataStore.run {
        setOf(INSTAGRAM_PKG, YOUTUBE_PKG, TIKTOK_PKG, TIKTOK_PKG2, TIKTOK_PKG3, SNAPCHAT_PKG)
    }
    @Volatile private var protectionEnabled    = true
    @Volatile private var focusScheduleEnabled = false
    @Volatile private var focusStartHour       = 9
    @Volatile private var focusEndHour         = 17
    @Volatile private var pauseUntilMs         = 0L

    // Watchdog: re-checks the current foreground window every 800ms for blocked packages.
    // Suspended for POST_BLOCK_PAUSE_MS after each block to let navigation animations settle.
    private var watchdogJob: Job? = null
    private var watchdogPackage: String? = null
    private var lastBlockTimeMs: Long = 0L
    private val POST_BLOCK_PAUSE_MS = 3_000L

    override fun onCreate() {
        super.onCreate()
        converter = AccessibilityEventConverter()
        executor  = BlockActionExecutor(this)
        // 500ms cooldown: tight enough to re-block quickly if user navigates back,
        // long enough to prevent back-press feedback loops.
        cooldown  = BlockCooldown(cooldownMs = 500L)

        serviceScope.launch { settingsDataStore.resetDailyCountsIfNeeded() }

        serviceScope.launch { settingsDataStore.protectionEnabledFlow.collect { protectionEnabled = it } }
        serviceScope.launch { settingsDataStore.enabledPackagesFlow.collect { enabledPackages = it } }
        serviceScope.launch { settingsDataStore.pauseUntilFlow.collect { pauseUntilMs = it } }
        serviceScope.launch {
            combine(
                settingsDataStore.focusScheduleEnabledFlow,
                settingsDataStore.focusStartHourFlow,
                settingsDataStore.focusEndHourFlow
            ) { e, s, n -> Triple(e, s, n) }.collect { (e, s, n) ->
                focusScheduleEnabled = e; focusStartHour = s; focusEndHour = n
            }
        }
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        if (event == null) return
        // No debounce — every event is evaluated. The cooldown (500ms) prevents rapid
        // back-press loops. Processing every event is essential because the event that
        // contains reel resource IDs is often the 2nd or 3rd in a burst.
        val pkg = event.packageName?.toString() ?: return
        manageWatchdog(pkg)
        processEventInternal(event)
    }

    private fun processEventInternal(event: AccessibilityEvent) {
        try {
            val pkg = event.packageName?.toString() ?: return
            if (pkg !in enabledPackages) return

            val snapshot = buildSnapshot(event) ?: return
            val result = detector.execute(snapshot)
            if (!result.shouldBlock) return

            maybeBlock(result.action)
        } catch (e: Exception) {
            android.util.Log.e("MindGuard", "processEvent error: ${e.message}")
        }
    }

    private fun maybeBlock(action: com.mindguard.shared.models.BlockAction) {
        val now = System.currentTimeMillis()
        val isProtected = (protectionEnabled || isCurrentlyInFocusHours()) &&
            !isPausedAt(pauseUntilMs, now)
        if (!isProtected) return

        // Suppress watchdog re-triggers for POST_BLOCK_PAUSE_MS after each block.
        // This prevents the watchdog from firing again before the navigation animation
        // (e.g. swipe-to-home-tab) has finished, which caused the Instagram refresh loop.
        if (now - lastBlockTimeMs < POST_BLOCK_PAUSE_MS) return

        if (cooldown.canBlock(now)) {
            lastBlockTimeMs = now
            cooldown.recordBlock(now)
            executeBlockAction(action)
            serviceScope.launch(Dispatchers.IO) {
                settingsDataStore.recordAttempt()
                settingsDataStore.recordBlock()
            }
        }
    }

    // Watchdog: scan the full active window every 800ms while a blocked app is foreground.
    // This catches the case where the app stops emitting events during reel playback.
    private fun manageWatchdog(pkg: String) {
        if (pkg !in enabledPackages) {
            if (watchdogPackage != null) stopWatchdog()
            return
        }
        if (pkg == watchdogPackage) return  // already watching
        stopWatchdog()
        watchdogPackage = pkg
        watchdogJob = serviceScope.launch {
            while (isActive) {
                delay(800)
                scanActiveWindowFor(pkg)
            }
        }
    }

    private fun stopWatchdog() {
        watchdogJob?.cancel()
        watchdogJob = null
        watchdogPackage = null
    }

    private fun scanActiveWindowFor(pkg: String) {
        try {
            val root = rootInActiveWindow ?: return
            val snapshot = converter.toScreenSnapshotFromRoot(root, pkg) ?: run {
                root.recycle(); return
            }
            root.recycle()
            if (snapshot.packageName !in enabledPackages) return
            val result = detector.execute(snapshot)
            if (result.shouldBlock) maybeBlock(result.action)
        } catch (e: Exception) {
            android.util.Log.e("MindGuard", "watchdog error: ${e.message}")
        }
    }

    private fun buildSnapshot(event: AccessibilityEvent): ScreenSnapshot? {
        val basic = converter.toScreenSnapshot(event) ?: return null
        // Augment with a full window scan to find resource IDs deep in the hierarchy
        val root = rootInActiveWindow ?: return basic
        return try {
            converter.augmentWithRoot(basic, root)
        } finally {
            root.recycle()
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
