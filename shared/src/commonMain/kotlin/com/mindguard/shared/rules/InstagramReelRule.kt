package com.mindguard.shared.rules

import com.mindguard.shared.models.BlockAction
import com.mindguard.shared.models.DetectionResult
import com.mindguard.shared.models.ScreenSnapshot

class InstagramReelRule : BlockingRule {

    private companion object {
        const val INSTAGRAM_PACKAGE = "com.instagram.android"
        const val MIN_SIGNAL_SCORE = 2

        val REEL_RESOURCE_IDS = setOf(
            "reel_pager",
            "reels_tab",
            "reel_play_button",
            "reel_component"
        )

        val REEL_TEXT_INDICATORS = setOf(
            "reels"
        )

        val REEL_DESCRIPTION_KEYWORDS = setOf(
            "reel",
            "video reel"
        )

        val FEED_BLOCKLIST_RESOURCE_IDS = setOf(
            "feed_pager",
            "feed_container",
            "stories_container"
        )
    }

    override fun evaluate(snapshot: ScreenSnapshot): DetectionResult {
        if (snapshot.packageName != INSTAGRAM_PACKAGE) {
            return noBlock()
        }

        val signals = countSignals(snapshot)

        return if (signals >= MIN_SIGNAL_SCORE) {
            DetectionResult(
                shouldBlock = true,
                action = BlockAction.GO_BACK,
                reason = "Instagram Reel detected ($signals signals)"
            )
        } else {
            noBlock()
        }
    }

    private fun countSignals(snapshot: ScreenSnapshot): Int {
        var score = 0

        if (hasReelTextIndicator(snapshot.screenText)) {
            score++
        }

        if (hasReelResourceId(snapshot.resourceIds)) {
            score++
        }

        if (hasReelDescription(snapshot.contentDescriptions)) {
            score++
        }

        if (isNotFeedScreen(snapshot.resourceIds)) {
            score++
        }

        return score
    }

    private fun hasReelTextIndicator(screenText: List<String>): Boolean {
        return screenText.any { text ->
            REEL_TEXT_INDICATORS.any { indicator ->
                text.lowercase().contains(indicator)
            }
        }
    }

    private fun hasReelResourceId(resourceIds: List<String>): Boolean {
        return resourceIds.any { id ->
            REEL_RESOURCE_IDS.any { reelId ->
                id.contains(reelId, ignoreCase = true)
            }
        }
    }

    private fun hasReelDescription(descriptions: List<String>): Boolean {
        return descriptions.any { desc ->
            REEL_DESCRIPTION_KEYWORDS.any { keyword ->
                desc.lowercase().contains(keyword)
            }
        }
    }

    private fun isNotFeedScreen(resourceIds: List<String>): Boolean {
        return !resourceIds.any { id ->
            FEED_BLOCKLIST_RESOURCE_IDS.any { blocklist ->
                id.contains(blocklist, ignoreCase = true)
            }
        }
    }

    private fun noBlock(): DetectionResult {
        return DetectionResult(shouldBlock = false, action = BlockAction.NONE, reason = null)
    }
}
