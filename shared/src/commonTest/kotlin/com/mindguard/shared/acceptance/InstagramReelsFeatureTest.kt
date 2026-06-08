package com.mindguard.shared.acceptance

import com.mindguard.shared.models.BlockAction
import com.mindguard.shared.models.ScreenSnapshot
import com.mindguard.shared.rules.InstagramReelRule
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class InstagramReelsFeatureTest {

    private val rule = InstagramReelRule()

    private fun snapshot(
        packageName: String = "com.instagram.android",
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
    fun reelPlayerIsBlockedImmediately() = feature("Instagram Reels") {
        scenario("User navigates into the Reels player") {
            given("MindGuard is monitoring Instagram") {}
            `when`("The Reels player view appears on screen") {
                val result = rule.evaluate(
                    snapshot(resourceIds = listOf("com.instagram.android:id/clips_viewer_view_pager"))
                )
                then("the user is redirected to the Home feed tab") {
                    assertTrue(result.shouldBlock)
                    assertEquals(BlockAction.CLICK_SAFE_TAB, result.action)
                }
            }
        }
    }

    @Test
    fun homeFeedIsNeverBlocked() = feature("Instagram Reels") {
        scenario("User scrolls through their Home feed") {
            given("MindGuard is monitoring Instagram") {}
            `when`("The Home feed is on screen") {
                val result = rule.evaluate(
                    snapshot(resourceIds = listOf("com.instagram.android:id/feed_pager"))
                )
                then("no block is triggered") {
                    assertFalse(result.shouldBlock)
                    assertEquals(BlockAction.NONE, result.action)
                }
            }
        }
    }

    @Test
    fun reelsNavTabAloneDoesNotBlock() = feature("Instagram Reels") {
        scenario("User sees the Reels nav tab but has not entered the player") {
            given("MindGuard is monitoring Instagram") {}
            `when`("Only the Reels nav tab resource ID is present") {
                val result = rule.evaluate(
                    snapshot(resourceIds = listOf("com.instagram.android:id/reels_tab"))
                )
                then("no block is triggered because the player is not open") {
                    assertFalse(result.shouldBlock)
                }
            }
        }
    }

    @Test
    fun differentAppIsNeverBlocked() = feature("Instagram Reels") {
        scenario("A different app happens to have matching resource IDs") {
            given("MindGuard is monitoring Instagram") {}
            `when`("The same player resource IDs appear in a different package") {
                val result = rule.evaluate(
                    snapshot(
                        packageName = "com.twitter.android",
                        resourceIds = listOf("com.twitter.android:id/clips_viewer_view_pager")
                    )
                )
                then("no block is triggered because the package does not match") {
                    assertFalse(result.shouldBlock)
                }
            }
        }
    }

    @Test
    fun multiplePlayerResourceIdsBlockImmediately() = feature("Instagram Reels") {
        scenario("Multiple Reels player views are present simultaneously") {
            given("MindGuard is monitoring Instagram") {}
            `when`("Both reel_pager and clips_viewer_view_pager are detected") {
                val result = rule.evaluate(
                    snapshot(
                        resourceIds = listOf(
                            "com.instagram.android:id/reel_pager",
                            "com.instagram.android:id/clips_viewer_view_pager"
                        )
                    )
                )
                then("the block fires on the first match") {
                    assertTrue(result.shouldBlock)
                    assertEquals(BlockAction.CLICK_SAFE_TAB, result.action)
                }
            }
        }
    }
}
