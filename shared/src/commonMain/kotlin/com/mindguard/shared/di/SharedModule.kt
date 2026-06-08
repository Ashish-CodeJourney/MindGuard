package com.mindguard.shared.di

import com.mindguard.shared.rules.InstagramReelRule
import com.mindguard.shared.rules.RuleEngine
import com.mindguard.shared.usecases.BlockCooldown
import com.mindguard.shared.usecases.DetectBlockedContentUseCase
import org.koin.dsl.module

val sharedModule = module {
    single { InstagramReelRule() }
    single { RuleEngine(listOf(get<InstagramReelRule>())) }
    single { DetectBlockedContentUseCase(get()) }
    single { BlockCooldown(cooldownMs = 2000) }
}
