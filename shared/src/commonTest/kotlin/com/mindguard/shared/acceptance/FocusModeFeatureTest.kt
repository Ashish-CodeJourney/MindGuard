package com.mindguard.shared.acceptance

import com.mindguard.shared.usecases.BlockCooldown
import com.mindguard.shared.usecases.isPausedAt
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class FocusModeFeatureTest {

    private val now = 100_000L

    @Test
    fun protectionIsNotPausedByDefault() = feature("Focus schedule and pause mode") {
        scenario("App freshly installed with no pause active") {
            given("pauseUntilMs is 0 (no pause scheduled)") {}
            `when`("the pause state is evaluated") {
                val paused = isPausedAt(pauseUntilMs = 0L, currentTimeMs = now)
                then("protection is active") { assertFalse(paused) }
            }
        }
    }

    @Test
    fun protectionIsPausedDuringPauseWindow() = feature("Focus schedule and pause mode") {
        scenario("User taps Pause for 30 minutes") {
            given("pauseUntilMs is set to now + 30 minutes") {}
            `when`("the pause state is evaluated before the deadline") {
                val paused = isPausedAt(pauseUntilMs = now + 30 * 60_000L, currentTimeMs = now)
                then("protection is paused") { assertTrue(paused) }
            }
        }
    }

    @Test
    fun protectionResumesAfterPauseExpires() = feature("Focus schedule and pause mode") {
        scenario("The 30-minute pause window has passed") {
            given("pauseUntilMs is in the past by 1ms") {}
            `when`("the pause state is evaluated after the deadline") {
                val paused = isPausedAt(pauseUntilMs = now - 1L, currentTimeMs = now)
                then("protection is active again") { assertFalse(paused) }
            }
        }
    }

    @Test
    fun pauseExpiresAtExactDeadline() = feature("Focus schedule and pause mode") {
        scenario("Current time is exactly equal to the pause deadline") {
            given("pauseUntilMs equals currentTimeMs exactly") {}
            `when`("the pause state is evaluated at the boundary") {
                val paused = isPausedAt(pauseUntilMs = now, currentTimeMs = now)
                then("protection is active — pause expires at the deadline, not after") {
                    assertFalse(paused)
                }
            }
        }
    }

    @Test
    fun cooldownPreventsRapidReblocking() = feature("Focus schedule and pause mode") {
        scenario("App detects a reel 10 times within the cooldown window") {
            given("a 500ms cooldown is configured") {
                val cooldown = BlockCooldown(cooldownMs = 500)
                var blockCount = 0
                `when`("10 detection events fire within 400ms") {
                    for (i in 0..9) {
                        val t = now + (i * 40L)
                        if (cooldown.canBlock(t)) {
                            blockCount++
                            cooldown.recordBlock(t)
                        }
                    }
                    then("only 1 block is recorded") {
                        assertEquals(1, blockCount)
                    }
                }
            }
        }
    }

    @Test
    fun cooldownResetsAfterWindow() = feature("Focus schedule and pause mode") {
        scenario("User enters a reel again after the cooldown window expires") {
            given("a block was recorded at t=0") {
                val cooldown = BlockCooldown(cooldownMs = 500)
                cooldown.recordBlock(now)
                `when`("501ms have elapsed") {
                    then("blocking is allowed again") {
                        assertTrue(cooldown.canBlock(now + 501))
                    }
                }
            }
        }
    }
}
