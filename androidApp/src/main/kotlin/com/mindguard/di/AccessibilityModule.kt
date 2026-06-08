package com.mindguard.di

import com.mindguard.accessibility.AccessibilityEventConverter
import com.mindguard.accessibility.AccessibilityEventDebouncer
import com.mindguard.accessibility.BlockActionExecutor
import org.koin.dsl.module

val accessibilityModule = module {
    single { AccessibilityEventConverter() }
    single { AccessibilityEventDebouncer(debounceMs = 100) }
}
