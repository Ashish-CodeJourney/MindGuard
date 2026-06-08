package com.mindguard.shared.acceptance

import com.mindguard.shared.models.BlockAction
import com.mindguard.shared.models.ScreenSnapshot
import com.mindguard.shared.rules.SnapchatSpotlightRule
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class SnapchatSpotlightFeatureTest {

    private val rule = SnapchatSpotlightRule()

    private fun snapshot(
        packageName: String = "com.snapchat.android",
        resourceIds: List<String> = emptyList(),
        screenText: List<String> = emptyList(),
        contentDescriptions: List<String> = emptyList()
    ) = ScreenSnapshot(
        packageName = packageName,
        resourceIds = resourceIds,
        screenText = screenText,
        contentDescriptions = contentDescriptions,
        timestampMillis = 1000L
    )

    @Test
    fun spotlightFeedIsBlocked() = feature("Snapchat Spotlight") {
        scenario("User opens the Spotlight feed") {
            given("MindGuard is monitoring Snapchat") {}
            `when`("The spotlight_feed resource ID is on screen") {
                val result = rule.evaluate(
                    snapshot(resourceIds = listOf("com.snapchat.android:id/spotlight_feed"))
                )
                then("a GO_BACK block is triggered") {
                    assertTrue(result.shouldBlock)
                    assertEquals(BlockAction.GO_BACK, result.action)
                }
            }
        }
    }

    @Test
    fun spotlightTextTriggersBlock() = feature("Snapchat Spotlight") {
        scenario("The Spotlight label is visible but no player resource ID is present") {
            given("MindGuard is monitoring Snapchat") {}
            `when`("The screen text contains 'Spotlight'") {
                val result = rule.evaluate(snapshot(screenText = listOf("Spotlight")))
                then("a block is triggered via text signal") {
                    assertTrue(result.shouldBlock)
                    assertEquals(BlockAction.GO_BACK, result.action)
                }
            }
        }
    }

    @Test
    fun cameraScreenIsNotBlocked() = feature("Snapchat Spotlight") {
        scenario("User opens Snapchat on the camera screen") {
            given("MindGuard is monitoring Snapchat") {}
            `when`("The camera capture resource IDs are present") {
                val result = rule.evaluate(
                    snapshot(
                        resourceIds = listOf("com.snapchat.android:id/capture_button"),
                        screenText = listOf("Send To")
                    )
                )
                then("no block is triggered") {
                    assertFalse(result.shouldBlock)
                }
            }
        }
    }

    @Test
    fun otherAppsNotBlocked() = feature("Snapchat Spotlight") {
        scenario("Another app uses the word 'Spotlight' in its UI") {
            given("MindGuard is monitoring Snapchat") {}
            `when`("Spotlight text appears in a non-Snapchat package") {
                val result = rule.evaluate(
                    snapshot(
                        packageName = "com.instagram.android",
                        screenText = listOf("Spotlight")
                    )
                )
                then("no block is triggered because package does not match") {
                    assertFalse(result.shouldBlock)
                }
            }
        }
    }
}
