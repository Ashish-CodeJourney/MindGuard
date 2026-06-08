package com.mindguard.shared.rules

import com.mindguard.shared.models.BlockAction
import com.mindguard.shared.models.ScreenSnapshot
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class YouTubeShortsRuleTest {

    private val rule = YouTubeShortsRule()

    private fun snapshot(
        packageName: String = "com.google.android.youtube",
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
    fun blocksWhenShortsResourceIdPresent() {
        val result = rule.evaluate(
            snapshot(resourceIds = listOf("com.google.android.youtube:id/reel_watch_fragment_root"))
        )
        assertTrue(result.shouldBlock)
        assertEquals(BlockAction.GO_BACK, result.action)
    }

    @Test
    fun doesNotBlockWhenOnlyShortsTabTextPresent() {
        val result = rule.evaluate(
            snapshot(screenText = listOf("Shorts"))
        )
        assertFalse(result.shouldBlock)
        assertEquals(BlockAction.NONE, result.action)
    }

    @Test
    fun doesNotBlockWhenOnlyHashtagShortsInText() {
        val result = rule.evaluate(
            snapshot(screenText = listOf("#shorts"))
        )
        assertFalse(result.shouldBlock)
        assertEquals(BlockAction.NONE, result.action)
    }

    @Test
    fun doesNotBlockWhenOnlyShortsDescriptionPresent() {
        val result = rule.evaluate(
            snapshot(contentDescriptions = listOf("Shorts video"))
        )
        assertFalse(result.shouldBlock)
        assertEquals(BlockAction.NONE, result.action)
    }

    @Test
    fun blocksResourceIdCaseInsensitively() {
        val result = rule.evaluate(
            snapshot(resourceIds = listOf("com.google.android.youtube:id/REEL_WATCH_FRAGMENT_ROOT"))
        )
        assertTrue(result.shouldBlock)
        assertEquals(BlockAction.GO_BACK, result.action)
    }

    @Test
    fun doesNotBlockWhenOnlyShortsNavTabPresent() {
        val result = rule.evaluate(
            snapshot(resourceIds = listOf("com.google.android.youtube:id/shorts_pivot_tab_label"))
        )
        assertFalse(result.shouldBlock)
        assertEquals(BlockAction.NONE, result.action)
    }

    @Test
    fun blocksWhenShortsVerticalFeedContainerPresent() {
        val result = rule.evaluate(
            snapshot(resourceIds = listOf("com.google.android.youtube:id/shorts_vertical_feed_container"))
        )
        assertTrue(result.shouldBlock)
        assertEquals(BlockAction.GO_BACK, result.action)
    }

    @Test
    fun blocksWhenReelPlayerPageContainerPresent() {
        val result = rule.evaluate(
            snapshot(resourceIds = listOf("com.google.android.youtube:id/reel_player_page_container"))
        )
        assertTrue(result.shouldBlock)
        assertEquals(BlockAction.GO_BACK, result.action)
    }

    @Test
    fun blocksWhenShortsShelfResourceIdPresent() {
        val result = rule.evaluate(
            snapshot(resourceIds = listOf("com.google.android.youtube:id/shorts_video_header"))
        )
        assertTrue(result.shouldBlock)
        assertEquals(BlockAction.GO_BACK, result.action)
    }

    // ── Should not block ─────────────────────────────────────────────────────

    @Test
    fun doesNotBlockRegularYouTubeContent() {
        val result = rule.evaluate(
            snapshot(
                screenText = listOf("Subscribe", "Like", "Comments"),
                resourceIds = listOf("com.google.android.youtube:id/watch_player")
            )
        )
        assertFalse(result.shouldBlock)
        assertEquals(BlockAction.NONE, result.action)
    }

    @Test
    fun doesNotBlockOtherApps() {
        val result = rule.evaluate(
            snapshot(
                packageName = "com.instagram.android",
                screenText = listOf("Shorts")
            )
        )
        assertFalse(result.shouldBlock)
        assertEquals(BlockAction.NONE, result.action)
    }

    @Test
    fun doesNotBlockEmptyYouTubeEvent() {
        val result = rule.evaluate(snapshot())
        assertFalse(result.shouldBlock)
        assertEquals(BlockAction.NONE, result.action)
    }

    @Test
    fun includesReasonInDetectionResult() {
        val result = rule.evaluate(
            snapshot(resourceIds = listOf("com.google.android.youtube:id/reel_watch_fragment_root"))
        )
        assertTrue(result.shouldBlock)
        assertTrue(result.reason != null)
    }
}
