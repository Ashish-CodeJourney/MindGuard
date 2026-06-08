package com.mindguard.shared.models

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class BlockActionTest {

    @Test
    fun definesExactlyFourBlockActions() {
        assertEquals(4, BlockAction.values().size, "BlockAction must define exactly 4 values")
    }

    @Test
    fun noneActionExists() {
        assertEquals("NONE", BlockAction.NONE.name)
    }

    @Test
    fun goBackActionExists() {
        assertEquals("GO_BACK", BlockAction.GO_BACK.name)
    }

    @Test
    fun goHomeAndReopenActionExists() {
        assertEquals("GO_HOME_AND_REOPEN_APP", BlockAction.GO_HOME_AND_REOPEN_APP.name)
    }

    @Test
    fun clickSafeTabActionExists() {
        assertEquals("CLICK_SAFE_TAB", BlockAction.CLICK_SAFE_TAB.name)
    }
}
