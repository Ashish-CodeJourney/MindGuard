package com.mindguard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mindguard.storage.SettingsDataStore
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class MainViewModel(private val store: SettingsDataStore) : ViewModel() {

    val protectionEnabled: StateFlow<Boolean> = store.protectionEnabledFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), true)

    val onboardingComplete: StateFlow<Boolean> = store.onboardingCompleteFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), false)

    val blockCountToday: StateFlow<Long> = store.blockCountTodayFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), 0L)

    val attemptCountToday: StateFlow<Long> = store.attemptCountTodayFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), 0L)

    val totalBlocks: StateFlow<Long> = store.totalBlocksFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), 0L)

    val totalAttempts: StateFlow<Long> = store.totalAttemptsFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), 0L)

    val currentStreak: StateFlow<Long> = store.currentStreakFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), 0L)

    val bestStreak: StateFlow<Long> = store.bestStreakFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), 0L)

    val instagramEnabled: StateFlow<Boolean> = store.instagramEnabledFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), true)

    val youtubeEnabled: StateFlow<Boolean> = store.youtubeEnabledFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), true)

    val tiktokEnabled: StateFlow<Boolean> = store.tiktokEnabledFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), true)

    val snapchatEnabled: StateFlow<Boolean> = store.snapchatEnabledFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), true)

    val focusScheduleEnabled: StateFlow<Boolean> = store.focusScheduleEnabledFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), false)

    val focusStartHour: StateFlow<Int> = store.focusStartHourFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), 9)

    val focusEndHour: StateFlow<Int> = store.focusEndHourFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), 17)

    init {
        viewModelScope.launch { store.resetDailyCountsIfNeeded() }
    }

    fun toggleProtection(enabled: Boolean) {
        viewModelScope.launch { store.setProtectionEnabled(enabled) }
    }

    fun completeOnboarding() {
        viewModelScope.launch { store.setOnboardingComplete() }
    }

    fun setInstagramEnabled(v: Boolean) { viewModelScope.launch { store.setInstagramEnabled(v) } }
    fun setYoutubeEnabled(v: Boolean)   { viewModelScope.launch { store.setYoutubeEnabled(v) } }
    fun setTiktokEnabled(v: Boolean)    { viewModelScope.launch { store.setTiktokEnabled(v) } }
    fun setSnapchatEnabled(v: Boolean)  { viewModelScope.launch { store.setSnapchatEnabled(v) } }

    fun setFocusSchedule(enabled: Boolean, startHour: Int, endHour: Int) {
        viewModelScope.launch { store.setFocusSchedule(enabled, startHour, endHour) }
    }
}
