package com.mindguard.shared.acceptance

import com.mindguard.shared.models.BlockAction
import com.mindguard.shared.models.ScreenSnapshot
import com.mindguard.shared.rules.TikTokRule
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class TikTokFeatureTest {

    private val rule = TikTokRule()

    private fun snapshot(
        packageName: String = "com.zhiliaoapp.musically",
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
    fun forYouFeedIsBlocked() = feature("TikTok") {
        scenario("User opens the For You feed") {
            given("MindGuard is monitoring TikTok") {}
            `when`("The For You text indicator is on screen") {
                val result = rule.evaluate(snapshot(screenText = listOf("For You")))
                then("a GO_BACK block is triggered") {
                    assertTrue(result.shouldBlock)
                    assertEquals(BlockAction.GO_BACK, result.action)
                }
            }
        }
    }

    @Test
    fun allThreePackageVariantsAreMonitored() = feature("TikTok") {
        scenario("User runs a market-specific TikTok package") {
            given("MindGuard is monitoring TikTok") {}
            `when`("The same feed text appears in the trill and aweme packages") {
                val trillResult = rule.evaluate(
                    snapshot(packageName = "com.ss.android.ugc.trill", screenText = listOf("For You"))
                )
                val awemeResult = rule.evaluate(
                    snapshot(packageName = "com.ss.android.ugc.aweme", screenText = listOf("For You"))
                )
                then("both package variants are blocked") {
                    assertTrue(trillResult.shouldBlock)
                    assertTrue(awemeResult.shouldBlock)
                }
            }
        }
    }

    @Test
    fun settingsScreenIsNotBlocked() = feature("TikTok") {
        scenario("User opens TikTok settings") {
            given("MindGuard is monitoring TikTok") {}
            `when`("The screen contains only settings-related content") {
                val result = rule.evaluate(
                    snapshot(
                        resourceIds = listOf("com.zhiliaoapp.musically:id/settings_item"),
                        screenText = listOf("Privacy", "Account")
                    )
                )
                then("no block is triggered") {
                    assertFalse(result.shouldBlock)
                }
            }
        }
    }

    @Test
    fun feedResourceIdAloneTriggersBlock() = feature("TikTok") {
        scenario("The feed container resource ID is detected without text signals") {
            given("MindGuard is monitoring TikTok") {}
            `when`("The feed_video_container resource ID is present") {
                val result = rule.evaluate(
                    snapshot(resourceIds = listOf("com.zhiliaoapp.musically:id/feed_video_container"))
                )
                then("a block is triggered via resource ID") {
                    assertTrue(result.shouldBlock)
                    assertEquals(BlockAction.GO_BACK, result.action)
                }
            }
        }
    }
}
