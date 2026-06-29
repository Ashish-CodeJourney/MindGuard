package com.mindguard

import android.accessibilityservice.AccessibilityServiceInfo
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.view.accessibility.AccessibilityManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.compose.runtime.*
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.compose.ui.platform.LocalLifecycleOwner
import kotlinx.coroutines.delay
import com.mindguard.ui.screens.HomeScreen
import com.mindguard.ui.screens.OnboardingScreen
import com.mindguard.ui.screens.PermissionsScreen
import com.mindguard.ui.screens.SettingsScreen
import com.mindguard.ui.screens.StatsScreen
import com.mindguard.ui.theme.MindGuardTheme
import org.koin.androidx.compose.koinViewModel

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MindGuardTheme {
                MindGuardApp(
                    checkAccessibilityEnabled = { isAccessibilityServiceEnabled() },
                    needsRestrictedSettingsStep = needsRestrictedSettingsStep(),
                    onOpenAccessibilitySettings = {
                        startActivity(Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS))
                    },
                    onOpenAppInfo = {
                        startActivity(
                            Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                                data = Uri.parse("package:$packageName")
                            }
                        )
                    }
                )
            }
        }
    }

    /**
     * Android 13+ restricts sideloaded apps from enabling accessibility services until
     * the user explicitly allows "restricted settings" in App Info. This check detects
     * that scenario so the UI can show the extra step.
     */
    private fun needsRestrictedSettingsStep(): Boolean {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) return false
        return try {
            val source = packageManager.getInstallSourceInfo(packageName)
            source.installingPackageName != "com.android.vending"
        } catch (e: Exception) {
            true  // assume restricted if we can't determine the install source
        }
    }

    private fun isAccessibilityServiceEnabled(): Boolean {
        val am = getSystemService(ACCESSIBILITY_SERVICE) as AccessibilityManager
        return am.getEnabledAccessibilityServiceList(AccessibilityServiceInfo.FEEDBACK_ALL_MASK)
            .any { it.resolveInfo.serviceInfo.name ==
                "com.mindguard.accessibility.MindGuardAccessibilityService" }
    }
}

private enum class Screen { ONBOARDING, PERMISSIONS, HOME, STATS, SETTINGS }

@Composable
fun MindGuardApp(
    checkAccessibilityEnabled: () -> Boolean,
    needsRestrictedSettingsStep: Boolean,
    onOpenAccessibilitySettings: () -> Unit,
    onOpenAppInfo: () -> Unit,
    viewModel: MainViewModel = koinViewModel()
) {
    val onboardingComplete  by viewModel.onboardingComplete.collectAsState()
    val protectionEnabled   by viewModel.protectionEnabled.collectAsState()
    val blockCountToday     by viewModel.blockCountToday.collectAsState()
    val attemptCountToday   by viewModel.attemptCountToday.collectAsState()
    val totalBlocks         by viewModel.totalBlocks.collectAsState()
    val totalAttempts       by viewModel.totalAttempts.collectAsState()
    val currentStreak       by viewModel.currentStreak.collectAsState()
    val bestStreak          by viewModel.bestStreak.collectAsState()
    val instagramEnabled    by viewModel.instagramEnabled.collectAsState()
    val youtubeEnabled      by viewModel.youtubeEnabled.collectAsState()
    val tiktokEnabled       by viewModel.tiktokEnabled.collectAsState()
    val snapchatEnabled     by viewModel.snapchatEnabled.collectAsState()
    val focusScheduleEnabled by viewModel.focusScheduleEnabled.collectAsState()
    val focusStartHour      by viewModel.focusStartHour.collectAsState()
    val focusEndHour        by viewModel.focusEndHour.collectAsState()
    val pauseUntil          by viewModel.pauseUntil.collectAsState()

    val lifecycleOwner = LocalLifecycleOwner.current
    var accessibilityEnabled by remember { mutableStateOf(checkAccessibilityEnabled()) }
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) accessibilityEnabled = checkAccessibilityEnabled()
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }
    // Active poll every 500ms while waiting for the user to grant permission.
    // ON_RESUME alone is unreliable when the system commits the accessibility setting
    // slightly after the activity resumes.
    LaunchedEffect(Unit) {
        while (true) {
            delay(500)
            val current = checkAccessibilityEnabled()
            if (current != accessibilityEnabled) accessibilityEnabled = current
        }
    }

    val startScreen = when {
        !onboardingComplete   -> Screen.ONBOARDING
        !accessibilityEnabled -> Screen.PERMISSIONS
        else                  -> Screen.HOME
    }
    var currentScreen by remember(onboardingComplete, accessibilityEnabled) {
        mutableStateOf(startScreen)
    }

    when (currentScreen) {
        Screen.ONBOARDING -> OnboardingScreen(
            onContinue = {
                viewModel.completeOnboarding()
                currentScreen = if (accessibilityEnabled) Screen.HOME else Screen.PERMISSIONS
            }
        )
        Screen.PERMISSIONS -> PermissionsScreen(
            isGranted = accessibilityEnabled,
            needsRestrictedSettingsStep = needsRestrictedSettingsStep,
            onOpenAppInfo = onOpenAppInfo,
            onOpenSettings = onOpenAccessibilitySettings,
            onContinue = { currentScreen = Screen.HOME }
        )
        Screen.HOME -> HomeScreen(
            protectionEnabled  = protectionEnabled,
            blockCount         = blockCountToday,
            attemptCount       = attemptCountToday,
            currentStreak      = currentStreak,
            pauseUntilMs       = pauseUntil,
            onToggleProtection = viewModel::toggleProtection,
            onResumeFromPause  = viewModel::resumeFromPause,
            onViewStats        = { currentScreen = Screen.STATS },
            onOpenSettings     = { currentScreen = Screen.SETTINGS }
        )
        Screen.STATS -> {
            BackHandler { currentScreen = Screen.HOME }
            StatsScreen(
                blockCountToday   = blockCountToday,
                attemptCountToday = attemptCountToday,
                totalBlocks       = totalBlocks,
                totalAttempts     = totalAttempts,
                currentStreak     = currentStreak,
                bestStreak        = bestStreak,
                onBack            = { currentScreen = Screen.HOME }
            )
        }
        Screen.SETTINGS -> {
            BackHandler { currentScreen = Screen.HOME }
            SettingsScreen(
            instagramEnabled     = instagramEnabled,
            youtubeEnabled       = youtubeEnabled,
            tiktokEnabled        = tiktokEnabled,
            snapchatEnabled      = snapchatEnabled,
            focusScheduleEnabled = focusScheduleEnabled,
            focusStartHour       = focusStartHour,
            focusEndHour         = focusEndHour,
            currentStreak        = currentStreak,
            bestStreak           = bestStreak,
            onInstagramToggle    = viewModel::setInstagramEnabled,
            onYoutubeToggle      = viewModel::setYoutubeEnabled,
            onTiktokToggle       = viewModel::setTiktokEnabled,
            onSnapchatToggle     = viewModel::setSnapchatEnabled,
            onFocusScheduleChange = viewModel::setFocusSchedule,
            onBack               = { currentScreen = Screen.HOME }
            )
        }
    }
}
