package com.mindguard.shared.rules

import com.mindguard.shared.models.BlockAction
import com.mindguard.shared.models.DetectionResult
import com.mindguard.shared.models.ScreenSnapshot

class SnapchatSpotlightRule : BlockingRule {

    private companion object {
        const val SNAPCHAT_PACKAGE  = "com.snapchat.android"
        const val MIN_SIGNAL_SCORE  = 1

        val SPOTLIGHT_RESOURCE_IDS = setOf(
            "spotlight_feed",
            "spotlight_video_container",
            "trending_content_view",
            "explore_feed_view"
        )

        val SPOTLIGHT_TEXT_INDICATORS = setOf("spotlight", "trending")

        val SPOTLIGHT_DESC_KEYWORDS = setOf("spotlight", "trending video")
    }

    override fun evaluate(snapshot: ScreenSnapshot): DetectionResult {
        if (snapshot.packageName != SNAPCHAT_PACKAGE) return noBlock()

        val score = countSignals(snapshot)
        return if (score >= MIN_SIGNAL_SCORE) {
            DetectionResult(
                shouldBlock = true,
                action      = BlockAction.GO_BACK,
                reason      = "Snapchat Spotlight detected ($score signals)"
            )
        } else noBlock()
    }

    private fun countSignals(snapshot: ScreenSnapshot): Int {
        var score = 0
        if (snapshot.resourceIds.any { id -> SPOTLIGHT_RESOURCE_IDS.any { id.contains(it, ignoreCase = true) } }) score++
        if (snapshot.screenText.any { t -> SPOTLIGHT_TEXT_INDICATORS.any { t.lowercase().contains(it) } }) score++
        if (snapshot.contentDescriptions.any { d -> SPOTLIGHT_DESC_KEYWORDS.any { d.lowercase().contains(it) } }) score++
        return score
    }

    private fun noBlock() = DetectionResult(shouldBlock = false, action = BlockAction.NONE, reason = null)
}
