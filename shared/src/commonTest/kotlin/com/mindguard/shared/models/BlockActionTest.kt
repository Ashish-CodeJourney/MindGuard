package com.mindguard.shared.models

import kotlin.test.Test
import kotlin.test.assertTrue

class BlockActionTest {

    @Test
    fun definesAllBlockActions() {
        val actions = BlockAction.values()
        assertTrue(actions.isNotEmpty(), "BlockAction must have at least one value")

        val actionNames = actions.map { it.name }
        assertTrue(actionNames.contains("NONE"), "Must have NONE action")
        assertTrue(actionNames.contains("GO_BACK"), "Must have GO_BACK action")
        assertTrue(actionNames.contains("GO_HOME_AND_REOPEN_APP"), "Must have GO_HOME_AND_REOPEN_APP action")
    }

    @Test
    fun noneActionExists() {
        val none = BlockAction.NONE
        assertTrue(none.name == "NONE")
    }

    @Test
    fun goBackActionExists() {
        val goBack = BlockAction.GO_BACK
        assertTrue(goBack.name == "GO_BACK")
    }

    @Test
    fun goHomeAndReopenActionExists() {
        val goHome = BlockAction.GO_HOME_AND_REOPEN_APP
        assertTrue(goHome.name == "GO_HOME_AND_REOPEN_APP")
    }
}
