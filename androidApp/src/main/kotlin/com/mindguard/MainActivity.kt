package com.mindguard

import android.content.ComponentName
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.text.TextUtils
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.*
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.compose.ui.platform.LocalLifecycleOwner
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
        val enabled = Settings.Secure.getString(
            contentResolver, Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES
        ) ?: return false
        // Android stores the full component name (e.g. "com.mindguard/com.mindguard.accessibility.MindGuardAccessibilityService")
        // not the dot-shorthand used in AndroidManifest android:name attributes.
        val target = ComponentName(
            packageName,
            "com.mindguard.accessibility.MindGuardAccessibilityService"
        ).flattenToString()
        val splitter = TextUtils.SimpleStringSplitter(':')
        splitter.setString(enabled)
        while (splitter.hasNext()) {
            if (splitter.next().equals(target, ignoreCase = true)) return true
        }
        return false
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
        Screen.STATS -> StatsScreen(
            blockCountToday   = blockCountToday,
            attemptCountToday = attemptCountToday,
            totalBlocks       = totalBlocks,
            totalAttempts     = totalAttempts,
            currentStreak     = currentStreak,
            bestStreak        = bestStreak,
            onBack            = { currentScreen = Screen.HOME }
        )
        Screen.SETTINGS -> SettingsScreen(
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
