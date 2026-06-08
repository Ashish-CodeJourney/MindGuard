package com.mindguard.shared.models

import kotlin.test.Test
import kotlin.test.assertEquals

class SupportedAppTest {

    @Test
    fun definesExactlyFourSupportedApps() {
        assertEquals(4, SupportedApp.values().size, "SupportedApp must define exactly 4 values")
    }

    @Test
    fun instagramAppExists() {
        assertEquals("INSTAGRAM", SupportedApp.INSTAGRAM.name)
    }

    @Test
    fun youtubeAppExists() {
        assertEquals("YOUTUBE", SupportedApp.YOUTUBE.name)
    }

    @Test
    fun snapchatAppExists() {
        assertEquals("SNAPCHAT", SupportedApp.SNAPCHAT.name)
    }

    @Test
    fun tiktokAppExists() {
        assertEquals("TIKTOK", SupportedApp.TIKTOK.name)
    }
}
