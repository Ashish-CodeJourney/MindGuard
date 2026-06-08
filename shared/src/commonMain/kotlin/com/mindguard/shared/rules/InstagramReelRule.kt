package com.mindguard.shared.rules

import com.mindguard.shared.models.BlockAction
import com.mindguard.shared.models.DetectionResult
import com.mindguard.shared.models.ScreenSnapshot

class InstagramReelRule : BlockingRule {

    private companion object {
        const val INSTAGRAM_PACKAGE = "com.instagram.android"

        // Any one of these resource IDs on screen → block immediately
        val REEL_RESOURCE_IDS = setOf(
            "reel_pager",
            "reels_tab",
            "reel_play_button",
            "reel_component",
            "clips_tab",
            "clips_swipe_container",
            "reels_viewer",
            "reel_feed_recycler_view",
            "ig_reels_player_container"
        )

        // "clips" is Instagram's alternate name for Reels in some regions/builds
        val REEL_TEXT_INDICATORS = setOf("reels", "clips")

        val REEL_DESCRIPTION_KEYWORDS = setOf("reel", "video reel")

        // If ANY of these are the primary container, user is on the regular feed — never block
        val FEED_SCREEN_RESOURCE_IDS = setOf(
            "feed_pager",
            "feed_container",
            "stories_container"
        )
    }

    override fun evaluate(snapshot: ScreenSnapshot): DetectionResult {
        if (snapshot.packageName != INSTAGRAM_PACKAGE) return noBlock()

        // Explicit feed-screen guard: if the main feed container is present, never block —
        // even if reel thumbnails appear as shelf items in the feed.
        if (isFeedScreen(snapshot.resourceIds)) return noBlock()

        val signals = countPositiveSignals(snapshot)
        return if (signals >= 1) {
            DetectionResult(
                shouldBlock = true,
                action = BlockAction.GO_BACK,
                reason = "Instagram Reel detected ($signals signals)"
            )
        } else noBlock()
    }

    private fun isFeedScreen(resourceIds: List<String>): Boolean =
        resourceIds.any { id -> FEED_SCREEN_RESOURCE_IDS.any { id.contains(it, ignoreCase = true) } }

    private fun countPositiveSignals(snapshot: ScreenSnapshot): Int {
        var score = 0
        if (snapshot.screenText.any { t -> REEL_TEXT_INDICATORS.any { t.lowercase().contains(it) } }) score++
        if (snapshot.resourceIds.any { id -> REEL_RESOURCE_IDS.any { id.contains(it, ignoreCase = true) } }) score++
        if (snapshot.contentDescriptions.any { d -> REEL_DESCRIPTION_KEYWORDS.any { d.lowercase().contains(it) } }) score++
        return score
    }

    private fun noBlock() = DetectionResult(shouldBlock = false, action = BlockAction.NONE, reason = null)
}
