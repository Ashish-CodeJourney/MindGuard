package com.mindguard.shared.acceptance

import com.mindguard.shared.models.BlockAction
import com.mindguard.shared.models.ScreenSnapshot
import com.mindguard.shared.rules.InstagramReelRule
import com.mindguard.shared.rules.SnapchatSpotlightRule
import com.mindguard.shared.rules.TikTokRule
import com.mindguard.shared.rules.YouTubeShortsRule
import com.mindguard.shared.rules.RuleEngine
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class MultiAppFeatureTest {

    private fun allRulesEngine() = RuleEngine(
        listOf(InstagramReelRule(), YouTubeShortsRule(), TikTokRule(), SnapchatSpotlightRule())
    )

    private fun snapshot(
        packageName: String,
        resourceIds: List<String> = emptyList(),
        screenText: List<String> = emptyList()
    ) = ScreenSnapshot(
        packageName = packageName,
        resourceIds = resourceIds,
        screenText = screenText,
        contentDescriptions = emptyList(),
        timestampMillis = 1000L
    )

    @Test
    fun allAppsAreMonitoredByDefault() = feature("Multi-app and per-app toggle behaviour") {
        scenario("All four supported apps are active with content on screen") {
            given("MindGuard monitors all four apps with the default rule set") {}
            `when`("each app shows its short-video player") {
                val instagramResult = allRulesEngine().evaluate(
                    snapshot("com.instagram.android", resourceIds = listOf("com.instagram.android:id/reel_pager"))
                )
                val youtubeResult = allRulesEngine().evaluate(
                    snapshot("com.google.android.youtube", resourceIds = listOf("com.google.android.youtube:id/reel_watch_fragment_root"))
                )
                val tiktokResult = allRulesEngine().evaluate(
                    snapshot("com.zhiliaoapp.musically", screenText = listOf("For You"))
                )
                val snapchatResult = allRulesEngine().evaluate(
                    snapshot("com.snapchat.android", screenText = listOf("Spotlight"))
                )
                then("all four are blocked") {
                    assertTrue(instagramResult.shouldBlock, "Instagram should block")
                    assertTrue(youtubeResult.shouldBlock, "YouTube should block")
                    assertTrue(tiktokResult.shouldBlock, "TikTok should block")
                    assertTrue(snapchatResult.shouldBlock, "Snapchat should block")
                }
            }
        }
    }

    @Test
    fun unknownAppIsNeverBlocked() = feature("Multi-app and per-app toggle behaviour") {
        scenario("An app outside the supported list has matching text and IDs") {
            given("MindGuard monitors only the four supported apps") {}
            `when`("an unknown app shows IDs and text that would match supported app rules") {
                val result = allRulesEngine().evaluate(
                    snapshot(
                        packageName = "com.twitter.android",
                        resourceIds = listOf("reel_watch_fragment_root", "clips_viewer_view_pager"),
                        screenText = listOf("Spotlight", "For You", "Shorts")
                    )
                )
                then("no block is triggered for the unknown package") {
                    assertFalse(result.shouldBlock)
                    assertEquals(BlockAction.NONE, result.action)
                }
            }
        }
    }

    @Test
    fun rulesAreIndependent() = feature("Multi-app and per-app toggle behaviour") {
        scenario("A YouTube Shorts snapshot is evaluated against all rules") {
            given("the rule engine has all four rules") {}
            `when`("a YouTube Shorts snapshot is evaluated") {
                val result = allRulesEngine().evaluate(
                    snapshot(
                        packageName = "com.google.android.youtube",
                        resourceIds = listOf("com.google.android.youtube:id/reel_watch_fragment_root")
                    )
                )
                then("only the YouTube rule fires, and the action is GO_BACK") {
                    assertTrue(result.shouldBlock)
                    assertEquals(BlockAction.GO_BACK, result.action)
                }
            }
        }
    }

    @Test
    fun emptySnapshotBlocksNoApp() = feature("Multi-app and per-app toggle behaviour") {
        scenario("A snapshot arrives with no content at all") {
            given("the rule engine has all four rules") {}
            `when`("an empty snapshot is evaluated for each app package") {
                val packages = listOf(
                    "com.instagram.android",
                    "com.google.android.youtube",
                    "com.zhiliaoapp.musically",
                    "com.snapchat.android"
                )
                val results = packages.map { pkg ->
                    allRulesEngine().evaluate(snapshot(packageName = pkg))
                }
                then("no app is blocked") {
                    results.forEach { result ->
                        assertFalse(result.shouldBlock, "Empty snapshot should not block ${result.reason}")
                    }
                }
            }
        }
    }

    @Test
    fun ruleEngineShortCircuitsOnFirstMatch() = feature("Multi-app and per-app toggle behaviour") {
        scenario("The first matching rule prevents subsequent rules from running") {
            given("the engine has Instagram rule first followed by a spy rule") {
                var spyExecuted = false
                val spyRule = object : com.mindguard.shared.rules.BlockingRule {
                    override fun evaluate(snapshot: ScreenSnapshot) = com.mindguard.shared.models.DetectionResult(
                        shouldBlock = false, action = BlockAction.NONE, reason = null
                    ).also { spyExecuted = true }
                }
                val engine = RuleEngine(listOf(InstagramReelRule(), spyRule))
                `when`("an Instagram Reels snapshot is evaluated") {
                    engine.evaluate(
                        snapshot(
                            packageName = "com.instagram.android",
                            resourceIds = listOf("com.instagram.android:id/reel_pager")
                        )
                    )
                    then("the spy rule is never evaluated") {
                        assertFalse(spyExecuted)
                    }
                }
            }
        }
    }
}
