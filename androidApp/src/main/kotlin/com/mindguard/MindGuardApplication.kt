package com.mindguard

import android.app.Application
import com.mindguard.di.accessibilityModule
import com.mindguard.di.storageModule
import com.mindguard.di.uiModule
import com.mindguard.shared.di.sharedModule
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin

class MindGuardApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        startKoin {
            androidContext(this@MindGuardApplication)
            modules(sharedModule, storageModule, accessibilityModule, uiModule)
        }
    }
}
