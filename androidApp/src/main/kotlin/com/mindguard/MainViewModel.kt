package com.mindguard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mindguard.storage.SettingsDataStore
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class MainViewModel(private val settingsDataStore: SettingsDataStore) : ViewModel() {

    val protectionEnabled: StateFlow<Boolean> = settingsDataStore.protectionEnabledFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), true)

    val onboardingComplete: StateFlow<Boolean> = settingsDataStore.onboardingCompleteFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), false)

    val blockCountToday: StateFlow<Long> = settingsDataStore.blockCountTodayFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), 0L)

    val attemptCountToday: StateFlow<Long> = settingsDataStore.attemptCountTodayFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), 0L)

    val totalBlocks: StateFlow<Long> = settingsDataStore.totalBlocksFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), 0L)

    val totalAttempts: StateFlow<Long> = settingsDataStore.totalAttemptsFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), 0L)

    init {
        viewModelScope.launch {
            settingsDataStore.resetDailyCountsIfNeeded()
        }
    }

    fun toggleProtection(enabled: Boolean) {
        viewModelScope.launch { settingsDataStore.setProtectionEnabled(enabled) }
    }

    fun completeOnboarding() {
        viewModelScope.launch { settingsDataStore.setOnboardingComplete() }
    }
}
