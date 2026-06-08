package com.mindguard.shared.rules

import com.mindguard.shared.models.BlockAction
import com.mindguard.shared.models.ScreenSnapshot
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class InstagramReelRuleTest {

    private val rule = InstagramReelRule()

    private fun createSnapshot(
        packageName: String = "com.instagram.android",
        screenText: List<String> = emptyList(),
        contentDescriptions: List<String> = emptyList(),
        resourceIds: List<String> = emptyList()
    ): ScreenSnapshot {
        return ScreenSnapshot(
            packageName = packageName,
            screenText = screenText,
            contentDescriptions = contentDescriptions,
            resourceIds = resourceIds,
            timestampMillis = 1000L
        )
    }

    @Test
    fun detectsReelWhenAllSignalsPresent() {
        val snapshot = createSnapshot(
            screenText = listOf("Reels", "Like", "Comment"),
            contentDescriptions = listOf("Video reel", "Like button"),
            resourceIds = listOf(
                "com.instagram.android:id/reel_pager",
                "com.instagram.android:id/reels_tab"
            )
        )

        val result = rule.evaluate(snapshot)

        assertTrue(result.shouldBlock)
        assertEquals(BlockAction.GO_BACK, result.action)
    }

    @Test
    fun doesNotBlockFeedScreen() {
        val snapshot = createSnapshot(
            screenText = listOf("Instagram", "Explore", "Messages"),
            contentDescriptions = listOf("Home feed"),
            resourceIds = listOf("com.instagram.android:id/feed_pager")
        )

        val result = rule.evaluate(snapshot)

        assertFalse(result.shouldBlock)
        assertEquals(BlockAction.NONE, result.action)
    }

    @Test
    fun doesNotBlockDifferentApp() {
        val snapshot = createSnapshot(
            packageName = "com.tiktok.android",
            screenText = listOf("Reels"),
            contentDescriptions = listOf("Video"),
            resourceIds = listOf("reel_id")
        )

        val result = rule.evaluate(snapshot)

        assertFalse(result.shouldBlock)
        assertEquals(BlockAction.NONE, result.action)
    }

    @Test
    fun doesNotBlockWhenOnlyReelsNavTabPresent() {
        val snapshot = createSnapshot(
            screenText = listOf("Reels"),
            resourceIds = listOf("com.instagram.android:id/reels_tab")
        )

        val result = rule.evaluate(snapshot)

        assertFalse(result.shouldBlock)
        assertEquals(BlockAction.NONE, result.action)
    }

    @Test
    fun detectsReelWithReelPagerSignal() {
        val snapshot = createSnapshot(
            screenText = listOf("Reels"),
            resourceIds = listOf("com.instagram.android:id/reel_pager")
        )

        val result = rule.evaluate(snapshot)

        assertTrue(result.shouldBlock)
        assertEquals(BlockAction.GO_BACK, result.action)
    }

    @Test
    fun detectsReelWithReelsTextLabel() {
        val snapshot = createSnapshot(
            screenText = listOf("Reels"),
            resourceIds = listOf("com.instagram.android:id/some_reel_component")
        )

        val result = rule.evaluate(snapshot)

        assertTrue(result.shouldBlock)
        assertEquals(BlockAction.GO_BACK, result.action)
    }

    @Test
    fun detectsReelWithVideoReelDescription() {
        val snapshot = createSnapshot(
            contentDescriptions = listOf("Video reel feed"),
            resourceIds = listOf("com.instagram.android:id/reel_component")
        )

        val result = rule.evaluate(snapshot)

        assertTrue(result.shouldBlock)
        assertEquals(BlockAction.GO_BACK, result.action)
    }

    // ── New: stricter detection (clips aliases + single-signal threshold) ────

    @Test
    fun doesNotBlockWhenOnlyClipsNavTabPresent() {
        val result = rule.evaluate(
            createSnapshot(resourceIds = listOf("com.instagram.android:id/clips_tab"))
        )
        assertFalse(result.shouldBlock)
        assertEquals(BlockAction.NONE, result.action)
    }

    @Test
    fun blocksWhenClipsSwipeContainerPresent() {
        val result = rule.evaluate(
            createSnapshot(resourceIds = listOf("com.instagram.android:id/clips_swipe_container"))
        )
        assertTrue(result.shouldBlock)
        assertEquals(BlockAction.GO_BACK, result.action)
    }

    @Test
    fun blocksWhenClipsViewerViewPagerPresent() {
        val result = rule.evaluate(
            createSnapshot(resourceIds = listOf("com.instagram.android:id/clips_viewer_view_pager"))
        )
        assertTrue(result.shouldBlock)
        assertEquals(BlockAction.GO_BACK, result.action)
    }

    @Test
    fun doesNotBlockWhenOnlyReelsTextPresentNoPlayerIds() {
        val result = rule.evaluate(
            createSnapshot(screenText = listOf("Reels", "Following"))
        )
        assertFalse(result.shouldBlock)
        assertEquals(BlockAction.NONE, result.action)
    }

    @Test
    fun blocksOnSingleReelResourceIdWithoutTextOrDescription() {
        val result = rule.evaluate(
            createSnapshot(
                screenText = listOf("Like", "Comment", "Share"),
                resourceIds = listOf("com.instagram.android:id/reel_pager")
            )
        )
        assertTrue(result.shouldBlock)
        assertEquals(BlockAction.GO_BACK, result.action)
    }

    @Test
    fun doesNotBlockWhenOnFeedEvenIfReelTextPresent() {
        val result = rule.evaluate(
            createSnapshot(
                screenText = listOf("Reels"),
                resourceIds = listOf(
                    "com.instagram.android:id/feed_pager",
                    "com.instagram.android:id/reel_pager"
                )
            )
        )
        assertFalse(result.shouldBlock)
        assertEquals(BlockAction.NONE, result.action)
    }

    @Test
    fun doesNotBlockWithOnlyOneWeakSignal() {
        val snapshot = createSnapshot(
            screenText = listOf("Explore"),
            resourceIds = listOf("com.instagram.android:id/feed_container")
        )

        val result = rule.evaluate(snapshot)

        assertFalse(result.shouldBlock)
        assertEquals(BlockAction.NONE, result.action)
    }

    @Test
    fun handlesEmptyScreenData() {
        val snapshot = createSnapshot(
            screenText = emptyList(),
            contentDescriptions = emptyList(),
            resourceIds = emptyList()
        )

        val result = rule.evaluate(snapshot)

        assertFalse(result.shouldBlock)
        assertEquals(BlockAction.NONE, result.action)
    }

    @Test
    fun detectsReelCaseInsensitive() {
        val snapshot = createSnapshot(
            screenText = listOf("REELS", "Like"),
            resourceIds = listOf("com.instagram.android:id/reel_pager")
        )

        val result = rule.evaluate(snapshot)

        assertTrue(result.shouldBlock)
        assertEquals(BlockAction.GO_BACK, result.action)
    }

    @Test
    fun doesNotBlockStoriesScreen() {
        val snapshot = createSnapshot(
            screenText = listOf("Your story", "Story"),
            resourceIds = listOf("com.instagram.android:id/stories_container")
        )

        val result = rule.evaluate(snapshot)

        assertFalse(result.shouldBlock)
        assertEquals(BlockAction.NONE, result.action)
    }

    @Test
    fun blocksReelWithMultipleReelIndicators() {
        val snapshot = createSnapshot(
            screenText = listOf("Reels", "Share", "Save"),
            resourceIds = listOf(
                "com.instagram.android:id/reel_pager",
                "com.instagram.android:id/reels_tab",
                "com.instagram.android:id/reel_play_button"
            )
        )

        val result = rule.evaluate(snapshot)

        assertTrue(result.shouldBlock)
        assertEquals(BlockAction.GO_BACK, result.action)
    }
}
