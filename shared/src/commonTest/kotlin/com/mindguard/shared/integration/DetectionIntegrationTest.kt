package com.mindguard.shared.integration

import com.mindguard.shared.models.BlockAction
import com.mindguard.shared.models.ScreenSnapshot
import com.mindguard.shared.rules.BlockingRule
import com.mindguard.shared.models.DetectionResult
import com.mindguard.shared.rules.InstagramReelRule
import com.mindguard.shared.rules.SnapchatSpotlightRule
import com.mindguard.shared.rules.TikTokRule
import com.mindguard.shared.rules.YouTubeShortsRule
import com.mindguard.shared.rules.RuleEngine
import com.mindguard.shared.usecases.BlockCooldown
import com.mindguard.shared.usecases.DetectBlockedContentUseCase
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class DetectionIntegrationTest {

    private object Snapshots {
        fun instagramReel(
            resourceIds: List<String> = listOf("com.instagram.android:id/reel_pager")
        ) = ScreenSnapshot(
            packageName = "com.instagram.android",
            screenText = emptyList(),
            contentDescriptions = emptyList(),
            resourceIds = resourceIds,
            timestampMillis = 1000L
        )

        fun instagramFeed() = ScreenSnapshot(
            packageName = "com.instagram.android",
            screenText = emptyList(),
            contentDescriptions = emptyList(),
            resourceIds = listOf("com.instagram.android:id/feed_pager"),
            timestampMillis = 1000L
        )

        fun youtubeShorts(
            resourceIds: List<String> = listOf("com.google.android.youtube:id/reel_watch_fragment_root")
        ) = ScreenSnapshot(
            packageName = "com.google.android.youtube",
            screenText = emptyList(),
            contentDescriptions = emptyList(),
            resourceIds = resourceIds,
            timestampMillis = 1000L
        )

        fun tiktokFeed(
            screenText: List<String> = listOf("For You"),
            resourceIds: List<String> = emptyList()
        ) = ScreenSnapshot(
            packageName = "com.zhiliaoapp.musically",
            screenText = screenText,
            contentDescriptions = emptyList(),
            resourceIds = resourceIds,
            timestampMillis = 1000L
        )

        fun snapchatSpotlight(
            screenText: List<String> = listOf("Spotlight"),
            resourceIds: List<String> = emptyList()
        ) = ScreenSnapshot(
            packageName = "com.snapchat.android",
            screenText = screenText,
            contentDescriptions = emptyList(),
            resourceIds = resourceIds,
            timestampMillis = 1000L
        )

        fun unknownApp() = ScreenSnapshot(
            packageName = "com.twitter.android",
            screenText = listOf("For You", "Spotlight", "Reels", "Shorts"),
            contentDescriptions = emptyList(),
            resourceIds = listOf("reel_watch_fragment_root", "clips_viewer_view_pager"),
            timestampMillis = 1000L
        )
    }

    private fun allRulesEngine() = RuleEngine(
        listOf(InstagramReelRule(), YouTubeShortsRule(), TikTokRule(), SnapchatSpotlightRule())
    )

    // ── Instagram ─────────────────────────────────────────────────────────

    @Test
    fun fullDetectionFlowForReel() {
        val engine = RuleEngine(listOf(InstagramReelRule()))
        val useCase = DetectBlockedContentUseCase(engine)
        val cooldown = BlockCooldown(cooldownMs = 2000)
        val snapshot = Snapshots.instagramReel()
        val now = 1000L

        val result = useCase.execute(snapshot)

        assertTrue(result.shouldBlock)
        assertEquals(BlockAction.CLICK_SAFE_TAB, result.action)
        assertTrue(cooldown.canBlock(now))

        cooldown.recordBlock(now)
        assertFalse(cooldown.canBlock(now + 1000))
        assertTrue(cooldown.canBlock(now + 2100))
    }

    @Test
    fun dontBlockFeedWhenNoReel() {
        val engine = RuleEngine(listOf(InstagramReelRule()))
        val useCase = DetectBlockedContentUseCase(engine)

        val result = useCase.execute(Snapshots.instagramFeed())

        assertFalse(result.shouldBlock)
        assertEquals(BlockAction.NONE, result.action)
    }

    @Test
    fun preventInfiniteBlockLoopsWithCooldown() {
        val engine = RuleEngine(listOf(InstagramReelRule()))
        val useCase = DetectBlockedContentUseCase(engine)
        val cooldown = BlockCooldown(cooldownMs = 2000)
        val snapshot = Snapshots.instagramReel()
        var successfulBlocks = 0

        for (i in 0..9) {
            val now = 1000L + (i * 200)
            if (cooldown.canBlock(now)) {
                val result = useCase.execute(snapshot)
                if (result.shouldBlock) {
                    successfulBlocks++
                    cooldown.recordBlock(now)
                }
            }
        }

        assertEquals(1, successfulBlocks)
    }

    // ── YouTube ───────────────────────────────────────────────────────────

    @Test
    fun fullDetectionFlowForYoutubeShorts() {
        val engine = RuleEngine(listOf(YouTubeShortsRule()))
        val useCase = DetectBlockedContentUseCase(engine)

        val result = useCase.execute(Snapshots.youtubeShorts())

        assertTrue(result.shouldBlock)
        assertEquals(BlockAction.GO_BACK, result.action)
    }

    @Test
    fun doesNotBlockYoutubeHomeScreen() {
        val engine = RuleEngine(listOf(YouTubeShortsRule()))
        val useCase = DetectBlockedContentUseCase(engine)
        val homeSnapshot = ScreenSnapshot(
            packageName = "com.google.android.youtube",
            screenText = emptyList(),
            contentDescriptions = emptyList(),
            resourceIds = listOf("com.google.android.youtube:id/shorts_pivot_tab_label"),
            timestampMillis = 1000L
        )

        val result = useCase.execute(homeSnapshot)

        assertFalse(result.shouldBlock)
    }

    // ── TikTok ────────────────────────────────────────────────────────────

    @Test
    fun fullDetectionFlowForTikTok() {
        val engine = RuleEngine(listOf(TikTokRule()))
        val useCase = DetectBlockedContentUseCase(engine)

        val result = useCase.execute(Snapshots.tiktokFeed())

        assertTrue(result.shouldBlock)
        assertEquals(BlockAction.GO_BACK, result.action)
    }

    @Test
    fun fullDetectionFlowForTikTokTrillPackage() {
        val engine = RuleEngine(listOf(TikTokRule()))
        val useCase = DetectBlockedContentUseCase(engine)
        val trillSnapshot = ScreenSnapshot(
            packageName = "com.ss.android.ugc.trill",
            screenText = listOf("For You"),
            contentDescriptions = emptyList(),
            resourceIds = emptyList(),
            timestampMillis = 1000L
        )

        val result = useCase.execute(trillSnapshot)

        assertTrue(result.shouldBlock)
        assertEquals(BlockAction.GO_BACK, result.action)
    }

    // ── Snapchat ──────────────────────────────────────────────────────────

    @Test
    fun fullDetectionFlowForSnapchatSpotlight() {
        val engine = RuleEngine(listOf(SnapchatSpotlightRule()))
        val useCase = DetectBlockedContentUseCase(engine)

        val result = useCase.execute(Snapshots.snapchatSpotlight())

        assertTrue(result.shouldBlock)
        assertEquals(BlockAction.GO_BACK, result.action)
    }

    @Test
    fun doesNotBlockSnapchatCameraScreen() {
        val engine = RuleEngine(listOf(SnapchatSpotlightRule()))
        val useCase = DetectBlockedContentUseCase(engine)
        val cameraSnapshot = ScreenSnapshot(
            packageName = "com.snapchat.android",
            screenText = listOf("Send To"),
            contentDescriptions = emptyList(),
            resourceIds = listOf("com.snapchat.android:id/capture_button"),
            timestampMillis = 1000L
        )

        val result = useCase.execute(cameraSnapshot)

        assertFalse(result.shouldBlock)
    }

    // ── Multi-rule engine ─────────────────────────────────────────────────

    @Test
    fun doesNotBlockUnknownApp() {
        val engine = allRulesEngine()
        val useCase = DetectBlockedContentUseCase(engine)

        val result = useCase.execute(Snapshots.unknownApp())

        assertFalse(result.shouldBlock)
        assertEquals(BlockAction.NONE, result.action)
    }

    @Test
    fun engineEvaluatesOnlyMatchingRule() {
        var instagramEvaluated = false
        var youtubeEvaluated = false

        val instagramRule = object : BlockingRule {
            override fun evaluate(snapshot: ScreenSnapshot): DetectionResult {
                instagramEvaluated = true
                return DetectionResult(shouldBlock = true, action = BlockAction.CLICK_SAFE_TAB, reason = "Instagram")
            }
        }
        val youtubeRule = object : BlockingRule {
            override fun evaluate(snapshot: ScreenSnapshot): DetectionResult {
                youtubeEvaluated = true
                return DetectionResult(shouldBlock = false, action = BlockAction.NONE, reason = null)
            }
        }

        val engine = RuleEngine(listOf(instagramRule, youtubeRule))
        engine.evaluate(Snapshots.instagramReel())

        assertTrue(instagramEvaluated)
        assertFalse(youtubeEvaluated)
    }

    @Test
    fun multipleRulesCanBeEvaluated() {
        val engine = allRulesEngine()
        val useCase = DetectBlockedContentUseCase(engine)

        val result = useCase.execute(Snapshots.instagramReel())

        assertTrue(result.shouldBlock)
        assertEquals(BlockAction.CLICK_SAFE_TAB, result.action)
    }
}
