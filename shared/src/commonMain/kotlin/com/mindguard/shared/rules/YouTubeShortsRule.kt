package com.mindguard.shared.rules

import com.mindguard.shared.models.BlockAction
import com.mindguard.shared.models.DetectionResult
import com.mindguard.shared.models.ScreenSnapshot

class YouTubeShortsRule : BlockingRule {

    private companion object {
        const val YOUTUBE_PACKAGE = "com.google.android.youtube"

        // Only resource IDs that appear inside the actual Shorts player.
        // shorts_pivot_tab_label (nav tab), reel_shelf_item and shorts_shelf_header_endpoint
        // (home-feed shelf items) are intentionally excluded — they appear on the home
        // screen and would block all of YouTube.
        val SHORTS_PLAYER_RESOURCE_IDS = setOf(
            "reel_watch_fragment_root",      // confirmed primary Shorts player root (multiple sources)
            "reel_progress_bar",             // confirmed unique to Shorts player (Shorts-Blocker)
            "shorts_video_header",
            "shorts_container",
            "shorts_vertical_feed_container",
            "reel_player_page_container"
        )
    }

    override fun evaluate(snapshot: ScreenSnapshot): DetectionResult {
        if (snapshot.packageName != YOUTUBE_PACKAGE) return noBlock()
        val hasPlayerResourceId = snapshot.resourceIds.any { id ->
            SHORTS_PLAYER_RESOURCE_IDS.any { id.contains(it, ignoreCase = true) }
        }
        return if (hasPlayerResourceId) {
            DetectionResult(shouldBlock = true, action = BlockAction.GO_BACK, reason = "YouTube Shorts player detected")
        } else noBlock()
    }

    private fun noBlock() = DetectionResult(shouldBlock = false, action = BlockAction.NONE, reason = null)
}
