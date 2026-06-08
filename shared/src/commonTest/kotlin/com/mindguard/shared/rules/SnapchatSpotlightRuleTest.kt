package com.mindguard.shared.rules

import com.mindguard.shared.models.BlockAction
import com.mindguard.shared.models.ScreenSnapshot
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class SnapchatSpotlightRuleTest {

    private val rule = SnapchatSpotlightRule()

    private fun snapshot(
        packageName: String = "com.snapchat.android",
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
    fun blocksWhenSpotlightTextPresent() {
        val result = rule.evaluate(
            snapshot(screenText = listOf("Spotlight"))
        )
        assertTrue(result.shouldBlock)
        assertEquals(BlockAction.GO_BACK, result.action)
    }

    @Test
    fun blocksWhenTrendingTextPresent() {
        val result = rule.evaluate(
            snapshot(screenText = listOf("Trending"))
        )
        assertTrue(result.shouldBlock)
        assertEquals(BlockAction.GO_BACK, result.action)
    }

    @Test
    fun blocksWhenSpotlightResourceIdPresent() {
        val result = rule.evaluate(
            snapshot(resourceIds = listOf("com.snapchat.android:id/spotlight_feed"))
        )
        assertTrue(result.shouldBlock)
        assertEquals(BlockAction.GO_BACK, result.action)
    }

    @Test
    fun blocksWhenSpotlightDescriptionPresent() {
        val result = rule.evaluate(
            snapshot(contentDescriptions = listOf("Spotlight video"))
        )
        assertTrue(result.shouldBlock)
        assertEquals(BlockAction.GO_BACK, result.action)
    }

    @Test
    fun blocksCaseInsensitively() {
        val result = rule.evaluate(
            snapshot(screenText = listOf("SPOTLIGHT"))
        )
        assertTrue(result.shouldBlock)
        assertEquals(BlockAction.GO_BACK, result.action)
    }

    @Test
    fun includesReasonInResult() {
        val result = rule.evaluate(
            snapshot(screenText = listOf("Spotlight"))
        )
        assertTrue(result.reason != null)
    }

    // ── Should not block ─────────────────────────────────────────────────────

    @Test
    fun doesNotBlockRegularSnapchatContent() {
        val result = rule.evaluate(
            snapshot(
                screenText = listOf("Camera", "Chat", "Stories"),
                resourceIds = listOf("com.snapchat.android:id/camera_view")
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
                screenText = listOf("Spotlight")
            )
        )
        assertFalse(result.shouldBlock)
        assertEquals(BlockAction.NONE, result.action)
    }

    @Test
    fun doesNotBlockEmptySnapchatEvent() {
        val result = rule.evaluate(snapshot())
        assertFalse(result.shouldBlock)
        assertEquals(BlockAction.NONE, result.action)
    }

    @Test
    fun doesNotBlockSnapchatWithGenericContent() {
        val result = rule.evaluate(
            snapshot(
                screenText = listOf("Add friend", "My story"),
                resourceIds = listOf("com.snapchat.android:id/chat_list_view")
            )
        )
        assertFalse(result.shouldBlock)
        assertEquals(BlockAction.NONE, result.action)
    }
}
