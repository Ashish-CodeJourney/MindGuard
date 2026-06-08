package com.mindguard.di

import android.content.Context
import com.mindguard.storage.SettingsDataStore
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module

val storageModule = module {
    single { SettingsDataStore(androidContext()) }
}
