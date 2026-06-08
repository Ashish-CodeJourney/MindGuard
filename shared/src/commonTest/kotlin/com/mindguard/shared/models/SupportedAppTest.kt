package com.mindguard.shared.models

import kotlin.test.Test
import kotlin.test.assertTrue

class SupportedAppTest {

    @Test
    fun definesSupportedApps() {
        val apps = SupportedApp.values()
        assertTrue(apps.isNotEmpty(), "SupportedApp must have at least one value")

        val appNames = apps.map { it.name }
        assertTrue(appNames.contains("INSTAGRAM"), "Must have INSTAGRAM app")
    }

    @Test
    fun instagramAppExists() {
        val instagram = SupportedApp.INSTAGRAM
        assertTrue(instagram.name == "INSTAGRAM")
    }

    @Test
    fun futureAppsCanBeAdded() {
        val apps = SupportedApp.values()
        // MVP only requires INSTAGRAM, but enum should be extensible
        assertTrue(apps.size >= 1, "Must support at least INSTAGRAM")
    }
}
