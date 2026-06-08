package com.mindguard.storage

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import java.time.LocalDate

private const val SETTINGS_NAME = "mindguard_settings"
private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = SETTINGS_NAME)

class SettingsDataStore(private val context: Context) {

    companion object {
        // Core
        private val PROTECTION_ENABLED    = booleanPreferencesKey("protection_enabled")
        private val ONBOARDING_COMPLETE   = booleanPreferencesKey("onboarding_complete")

        // Daily counters
        private val BLOCK_COUNT_TODAY     = longPreferencesKey("block_count_today")
        private val ATTEMPT_COUNT_TODAY   = longPreferencesKey("attempt_count_today")
        private val LAST_RESET_DAY        = longPreferencesKey("last_reset_day")

        // All-time counters
        private val TOTAL_BLOCKS          = longPreferencesKey("total_blocks")
        private val TOTAL_ATTEMPTS        = longPreferencesKey("total_attempts")

        // Streak
        private val CURRENT_STREAK        = longPreferencesKey("current_streak")
        private val BEST_STREAK           = longPreferencesKey("best_streak")

        // Per-app blocking toggles (all on by default)
        private val INSTAGRAM_ENABLED     = booleanPreferencesKey("instagram_enabled")
        private val YOUTUBE_ENABLED       = booleanPreferencesKey("youtube_enabled")
        private val TIKTOK_ENABLED        = booleanPreferencesKey("tiktok_enabled")
        private val SNAPCHAT_ENABLED      = booleanPreferencesKey("snapchat_enabled")

        // Focus schedule
        private val FOCUS_SCHEDULE_ON     = booleanPreferencesKey("focus_schedule_enabled")
        private val FOCUS_START_HOUR      = intPreferencesKey("focus_start_hour")
        private val FOCUS_END_HOUR        = intPreferencesKey("focus_end_hour")

        // Package names
        const val INSTAGRAM_PKG = "com.instagram.android"
        const val YOUTUBE_PKG   = "com.google.android.youtube"
        const val TIKTOK_PKG    = "com.zhiliaoapp.musically"
        const val TIKTOK_PKG2   = "com.ss.android.ugc.trill"
        const val TIKTOK_PKG3   = "com.ss.android.ugc.aweme"
        const val SNAPCHAT_PKG  = "com.snapchat.android"
    }

    // ── Core flows ───────────────────────────────────────────────────────────
    val protectionEnabledFlow: Flow<Boolean> = context.dataStore.data.map { it[PROTECTION_ENABLED] ?: true }
    val onboardingCompleteFlow: Flow<Boolean> = context.dataStore.data.map { it[ONBOARDING_COMPLETE] ?: false }

    // ── Stats flows ───────────────────────────────────────────────────────────
    val blockCountTodayFlow: Flow<Long>       = context.dataStore.data.map { it[BLOCK_COUNT_TODAY] ?: 0L }
    val attemptCountTodayFlow: Flow<Long>     = context.dataStore.data.map { it[ATTEMPT_COUNT_TODAY] ?: 0L }
    val totalBlocksFlow: Flow<Long>           = context.dataStore.data.map { it[TOTAL_BLOCKS] ?: 0L }
    val totalAttemptsFlow: Flow<Long>         = context.dataStore.data.map { it[TOTAL_ATTEMPTS] ?: 0L }

    // ── Streak flows ──────────────────────────────────────────────────────────
    val currentStreakFlow: Flow<Long>         = context.dataStore.data.map { it[CURRENT_STREAK] ?: 0L }
    val bestStreakFlow: Flow<Long>            = context.dataStore.data.map { it[BEST_STREAK] ?: 0L }

    // ── Per-app enable flows ──────────────────────────────────────────────────
    val instagramEnabledFlow: Flow<Boolean>  = context.dataStore.data.map { it[INSTAGRAM_ENABLED] ?: true }
    val youtubeEnabledFlow: Flow<Boolean>    = context.dataStore.data.map { it[YOUTUBE_ENABLED] ?: true }
    val tiktokEnabledFlow: Flow<Boolean>     = context.dataStore.data.map { it[TIKTOK_ENABLED] ?: true }
    val snapchatEnabledFlow: Flow<Boolean>   = context.dataStore.data.map { it[SNAPCHAT_ENABLED] ?: true }

    // ── Focus schedule flows ──────────────────────────────────────────────────
    val focusScheduleEnabledFlow: Flow<Boolean> = context.dataStore.data.map { it[FOCUS_SCHEDULE_ON] ?: false }
    val focusStartHourFlow: Flow<Int>            = context.dataStore.data.map { it[FOCUS_START_HOUR] ?: 9 }
    val focusEndHourFlow: Flow<Int>              = context.dataStore.data.map { it[FOCUS_END_HOUR] ?: 17 }

    // ── Enabled packages flow (combines all app toggles) ─────────────────────
    val enabledPackagesFlow: Flow<Set<String>> = combine(
        instagramEnabledFlow,
        youtubeEnabledFlow,
        tiktokEnabledFlow,
        snapchatEnabledFlow
    ) { instagram, youtube, tiktok, snapchat ->
        buildSet {
            if (instagram) add(INSTAGRAM_PKG)
            if (youtube)   add(YOUTUBE_PKG)
            if (tiktok)  { add(TIKTOK_PKG); add(TIKTOK_PKG2); add(TIKTOK_PKG3) }
            if (snapchat)  add(SNAPCHAT_PKG)
        }
    }

    // ── Write operations ──────────────────────────────────────────────────────

    suspend fun setProtectionEnabled(enabled: Boolean) {
        context.dataStore.edit { it[PROTECTION_ENABLED] = enabled }
    }

    suspend fun setOnboardingComplete() {
        context.dataStore.edit { it[ONBOARDING_COMPLETE] = true }
    }

    suspend fun setInstagramEnabled(enabled: Boolean) {
        context.dataStore.edit { it[INSTAGRAM_ENABLED] = enabled }
    }

    suspend fun setYoutubeEnabled(enabled: Boolean) {
        context.dataStore.edit { it[YOUTUBE_ENABLED] = enabled }
    }

    suspend fun setTiktokEnabled(enabled: Boolean) {
        context.dataStore.edit { it[TIKTOK_ENABLED] = enabled }
    }

    suspend fun setSnapchatEnabled(enabled: Boolean) {
        context.dataStore.edit { it[SNAPCHAT_ENABLED] = enabled }
    }

    suspend fun setFocusSchedule(enabled: Boolean, startHour: Int, endHour: Int) {
        context.dataStore.edit {
            it[FOCUS_SCHEDULE_ON] = enabled
            it[FOCUS_START_HOUR]  = startHour.coerceIn(0, 23)
            it[FOCUS_END_HOUR]    = endHour.coerceIn(0, 23)
        }
    }

    suspend fun recordAttempt() {
        context.dataStore.edit {
            it[ATTEMPT_COUNT_TODAY] = (it[ATTEMPT_COUNT_TODAY] ?: 0L) + 1
            it[TOTAL_ATTEMPTS]      = (it[TOTAL_ATTEMPTS] ?: 0L) + 1
        }
    }

    suspend fun recordBlock() {
        context.dataStore.edit {
            it[BLOCK_COUNT_TODAY] = (it[BLOCK_COUNT_TODAY] ?: 0L) + 1
            it[TOTAL_BLOCKS]      = (it[TOTAL_BLOCKS] ?: 0L) + 1
        }
    }

    suspend fun resetDailyCountsIfNeeded() {
        val todayEpochDay = LocalDate.now().toEpochDay()
        val prefs = context.dataStore.data.first()
        if ((prefs[LAST_RESET_DAY] ?: 0L) >= todayEpochDay) return

        // Streak: if yesterday everything attempted was blocked → clean day → increment
        val attempts = prefs[ATTEMPT_COUNT_TODAY] ?: 0L
        val blocks   = prefs[BLOCK_COUNT_TODAY] ?: 0L
        val wasClean = attempts <= blocks  // 0 slipped through (or no usage)
        val newStreak = if (wasClean) (prefs[CURRENT_STREAK] ?: 0L) + 1L else 0L
        val newBest   = maxOf(prefs[BEST_STREAK] ?: 0L, newStreak)

        context.dataStore.edit {
            it[BLOCK_COUNT_TODAY]   = 0L
            it[ATTEMPT_COUNT_TODAY] = 0L
            it[LAST_RESET_DAY]      = todayEpochDay
            it[CURRENT_STREAK]      = newStreak
            it[BEST_STREAK]         = newBest
        }
    }
}
