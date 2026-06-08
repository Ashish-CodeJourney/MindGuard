package com.mindguard.shared.acceptance

import com.mindguard.shared.models.BlockAction
import com.mindguard.shared.models.ScreenSnapshot
import com.mindguard.shared.rules.YouTubeShortsRule
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class YouTubeShortsFeatureTest {

    private val rule = YouTubeShortsRule()

    private fun snapshot(
        packageName: String = "com.google.android.youtube",
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
    fun shortsPlayerIsBlockedImmediately() = feature("YouTube Shorts") {
        scenario("User swipes into the Shorts player") {
            given("MindGuard is monitoring YouTube") {}
            `when`("The Shorts player root view appears on screen") {
                val result = rule.evaluate(
                    snapshot(resourceIds = listOf("com.google.android.youtube:id/reel_watch_fragment_root"))
                )
                then("a GO_BACK block is triggered") {
                    assertTrue(result.shouldBlock)
                    assertEquals(BlockAction.GO_BACK, result.action)
                }
            }
        }
    }

    @Test
    fun progressBarAloneTriggersBlock() = feature("YouTube Shorts") {
        scenario("The Shorts progress bar is the only visible player signal") {
            given("MindGuard is monitoring YouTube") {}
            `when`("Only the reel_progress_bar resource ID is detected") {
                val result = rule.evaluate(
                    snapshot(resourceIds = listOf("com.google.android.youtube:id/reel_progress_bar"))
                )
                then("a block is triggered because reel_progress_bar is unique to the Shorts player") {
                    assertTrue(result.shouldBlock)
                    assertEquals(BlockAction.GO_BACK, result.action)
                }
            }
        }
    }

    @Test
    fun homeScreenWithShortsTabIsNotBlocked() = feature("YouTube Shorts") {
        scenario("User is on the YouTube home screen with the Shorts nav tab visible") {
            given("MindGuard is monitoring YouTube") {}
            `when`("Only the Shorts pivot tab label resource ID is present") {
                val result = rule.evaluate(
                    snapshot(resourceIds = listOf("com.google.android.youtube:id/shorts_pivot_tab_label"))
                )
                then("no block is triggered because the player is not open") {
                    assertFalse(result.shouldBlock)
                }
            }
        }
    }

    @Test
    fun regularVideoWatchScreenIsNotBlocked() = feature("YouTube Shorts") {
        scenario("User watches a regular long-form video") {
            given("MindGuard is monitoring YouTube") {}
            `when`("The standard watch player resource ID is on screen") {
                val result = rule.evaluate(
                    snapshot(resourceIds = listOf("com.google.android.youtube:id/watch_player"))
                )
                then("no block is triggered because this is a regular video") {
                    assertFalse(result.shouldBlock)
                }
            }
        }
    }

    @Test
    fun resourceIdMatchIsCaseInsensitive() = feature("YouTube Shorts") {
        scenario("Resource ID arrives in uppercase due to OEM variation") {
            given("MindGuard is monitoring YouTube") {}
            `when`("The player root ID is uppercase") {
                val result = rule.evaluate(
                    snapshot(resourceIds = listOf("com.google.android.youtube:id/REEL_WATCH_FRAGMENT_ROOT"))
                )
                then("the block still fires because matching is case-insensitive") {
                    assertTrue(result.shouldBlock)
                    assertEquals(BlockAction.GO_BACK, result.action)
                }
            }
        }
    }
}
