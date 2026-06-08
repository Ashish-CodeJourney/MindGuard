package com.mindguard.shared.rules

import com.mindguard.shared.models.BlockAction
import com.mindguard.shared.models.ScreenSnapshot
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class TikTokRuleTest {

    private val rule = TikTokRule()

    private fun snapshot(
        packageName: String = "com.zhiliaoapp.musically",
        screenText: List<String> = emptyList(),
        contentDescriptions: List<String> = emptyList(),
        resourceIds: List<String> = emptyList()
    ) = ScreenSnapshot(
        packageName = packageName,
        screenText = screenText,
        contentDescriptions = contentDescriptions,
        resourceIds = resourceIds,
        timestampMillis = 1000L
    )

    // ── Should block ─────────────────────────────────────────────────────────

    @Test
    fun blocksForYouFeedOnGlobalPackage() {
        val result = rule.evaluate(
            snapshot(screenText = listOf("For You"))
        )
        assertTrue(result.shouldBlock)
        assertEquals(BlockAction.GO_BACK, result.action)
    }

    @Test
    fun blocksFollowingFeedOnGlobalPackage() {
        val result = rule.evaluate(
            snapshot(screenText = listOf("Following"))
        )
        assertTrue(result.shouldBlock)
        assertEquals(BlockAction.GO_BACK, result.action)
    }

    @Test
    fun blocksFriendsFeedTab() {
        val result = rule.evaluate(
            snapshot(screenText = listOf("Friends"))
        )
        assertTrue(result.shouldBlock)
        assertEquals(BlockAction.GO_BACK, result.action)
    }

    @Test
    fun blocksWhenFeedResourceIdPresent() {
        val result = rule.evaluate(
            snapshot(resourceIds = listOf("com.zhiliaoapp.musically:id/feed_video_container"))
        )
        assertTrue(result.shouldBlock)
        assertEquals(BlockAction.GO_BACK, result.action)
    }

    @Test
    fun blocksOnRegionalPackageTrill() {
        val result = rule.evaluate(
            snapshot(
                packageName = "com.ss.android.ugc.trill",
                screenText = listOf("For You")
            )
        )
        assertTrue(result.shouldBlock)
        assertEquals(BlockAction.GO_BACK, result.action)
    }

    @Test
    fun blocksOnDomesticPackageAweme() {
        val result = rule.evaluate(
            snapshot(
                packageName = "com.ss.android.ugc.aweme",
                screenText = listOf("Following")
            )
        )
        assertTrue(result.shouldBlock)
        assertEquals(BlockAction.GO_BACK, result.action)
    }

    @Test
    fun blocksCaseInsensitively() {
        val result = rule.evaluate(
            snapshot(screenText = listOf("FOR YOU"))
        )
        assertTrue(result.shouldBlock)
        assertEquals(BlockAction.GO_BACK, result.action)
    }

    @Test
    fun includesReasonInResult() {
        val result = rule.evaluate(
            snapshot(screenText = listOf("For You"))
        )
        assertTrue(result.reason != null)
    }

    // ── Should not block ─────────────────────────────────────────────────────

    @Test
    fun doesNotBlockOtherApps() {
        val result = rule.evaluate(
            snapshot(
                packageName = "com.instagram.android",
                screenText = listOf("For You")
            )
        )
        assertFalse(result.shouldBlock)
        assertEquals(BlockAction.NONE, result.action)
    }

    @Test
    fun doesNotBlockTikTokWithoutFeedSignals() {
        val result = rule.evaluate(
            snapshot(
                screenText = listOf("Settings", "Privacy"),
                resourceIds = listOf("com.zhiliaoapp.musically:id/settings_container")
            )
        )
        assertFalse(result.shouldBlock)
        assertEquals(BlockAction.NONE, result.action)
    }

    @Test
    fun doesNotBlockEmptyTikTokEvent() {
        val result = rule.evaluate(snapshot())
        assertFalse(result.shouldBlock)
        assertEquals(BlockAction.NONE, result.action)
    }
}
