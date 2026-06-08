package com.mindguard.shared.rules

import com.mindguard.shared.models.BlockAction
import com.mindguard.shared.models.DetectionResult
import com.mindguard.shared.models.ScreenSnapshot

class YouTubeShortsRule : BlockingRule {

    private companion object {
        const val YOUTUBE_PACKAGE   = "com.google.android.youtube"
        const val MIN_SIGNAL_SCORE  = 1

        val SHORTS_RESOURCE_IDS = setOf(
            "reel_watch_fragment_root",
            "shorts_video_header",
            "shorts_shelf_header_endpoint",
            "shorts_container",
            "reel_shelf_item"
        )

        val SHORTS_TEXT_INDICATORS = setOf("shorts", "#shorts")

        val SHORTS_DESC_KEYWORDS = setOf("shorts", "short video")
    }

    override fun evaluate(snapshot: ScreenSnapshot): DetectionResult {
        if (snapshot.packageName != YOUTUBE_PACKAGE) return noBlock()

        val score = countSignals(snapshot)
        return if (score >= MIN_SIGNAL_SCORE) {
            DetectionResult(
                shouldBlock = true,
                action      = BlockAction.GO_BACK,
                reason      = "YouTube Shorts detected ($score signals)"
            )
        } else noBlock()
    }

    private fun countSignals(snapshot: ScreenSnapshot): Int {
        var score = 0
        if (snapshot.resourceIds.any { id -> SHORTS_RESOURCE_IDS.any { id.contains(it, ignoreCase = true) } }) score++
        if (snapshot.screenText.any { t -> SHORTS_TEXT_INDICATORS.any { t.lowercase().contains(it) } }) score++
        if (snapshot.contentDescriptions.any { d -> SHORTS_DESC_KEYWORDS.any { d.lowercase().contains(it) } }) score++
        return score
    }

    private fun noBlock() = DetectionResult(shouldBlock = false, action = BlockAction.NONE, reason = null)
}
