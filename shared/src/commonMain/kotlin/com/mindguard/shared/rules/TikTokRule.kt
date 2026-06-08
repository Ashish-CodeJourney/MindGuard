package com.mindguard.shared.rules

import com.mindguard.shared.models.BlockAction
import com.mindguard.shared.models.DetectionResult
import com.mindguard.shared.models.ScreenSnapshot

class TikTokRule : BlockingRule {

    private companion object {
        val TIKTOK_PACKAGES = setOf(
            "com.zhiliaoapp.musically",   // global
            "com.ss.android.ugc.trill",   // some markets
            "com.ss.android.ugc.aweme"    // China domestic
        )

        val FEED_RESOURCE_IDS = setOf(
            "feed_video_container",
            "main_item_container",
            "video_player_controller",
            "feed_tab_following",
            "feed_tab_for_you"
        )

        val FEED_TEXT_INDICATORS = setOf("for you", "following", "friends")
    }

    override fun evaluate(snapshot: ScreenSnapshot): DetectionResult {
        if (snapshot.packageName !in TIKTOK_PACKAGES) return noBlock()

        val hasFeedSignal =
            snapshot.resourceIds.any { id -> FEED_RESOURCE_IDS.any { id.contains(it, ignoreCase = true) } } ||
            snapshot.screenText.any { t -> FEED_TEXT_INDICATORS.any { t.lowercase().contains(it) } }

        return if (hasFeedSignal) {
            DetectionResult(
                shouldBlock = true,
                action      = BlockAction.GO_BACK,
                reason      = "TikTok short video feed detected"
            )
        } else noBlock()
    }

    private fun noBlock() = DetectionResult(shouldBlock = false, action = BlockAction.NONE, reason = null)
}
