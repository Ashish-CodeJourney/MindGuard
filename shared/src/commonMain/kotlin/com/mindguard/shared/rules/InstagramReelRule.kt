package com.mindguard.shared.rules

import com.mindguard.shared.models.BlockAction
import com.mindguard.shared.models.DetectionResult
import com.mindguard.shared.models.ScreenSnapshot

class InstagramReelRule : BlockingRule {

    private companion object {
        const val INSTAGRAM_PACKAGE = "com.instagram.android"

        // Resource IDs that only appear inside the actual Reels/Clips player.
        // Navigation-tab IDs (reels_tab, clips_tab) are intentionally excluded because
        // they appear on every Instagram screen and would cause false-positive blocks.
        val REEL_PLAYER_RESOURCE_IDS = setOf(
            "clips_viewer_view_pager",  // confirmed primary Reels ViewPager (multiple open-source apps)
            "reel_pager",
            "reel_play_button",
            "reel_component",
            "clips_swipe_container",
            "reels_viewer",
            "reel_feed_recycler_view",
            "ig_reels_player_container"
        )

        val FEED_SCREEN_RESOURCE_IDS = setOf(
            "feed_pager",
            "feed_container",
            "stories_container"
        )
    }

    override fun evaluate(snapshot: ScreenSnapshot): DetectionResult {
        if (snapshot.packageName != INSTAGRAM_PACKAGE) return noBlock()
        if (isFeedScreen(snapshot.resourceIds)) return noBlock()
        val hasPlayerResourceId = snapshot.resourceIds.any { id ->
            REEL_PLAYER_RESOURCE_IDS.any { id.contains(it, ignoreCase = true) }
        }
        return if (hasPlayerResourceId) {
            DetectionResult(shouldBlock = true, action = BlockAction.CLICK_SAFE_TAB, reason = "Instagram Reel player detected")
        } else noBlock()
    }

    private fun isFeedScreen(resourceIds: List<String>): Boolean =
        resourceIds.any { id -> FEED_SCREEN_RESOURCE_IDS.any { id.contains(it, ignoreCase = true) } }

    private fun noBlock() = DetectionResult(shouldBlock = false, action = BlockAction.NONE, reason = null)
}
