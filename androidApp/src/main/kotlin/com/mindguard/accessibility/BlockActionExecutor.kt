package com.mindguard.accessibility

import android.accessibilityservice.AccessibilityService
import android.content.Intent
import android.view.accessibility.AccessibilityNodeInfo
import com.mindguard.shared.models.BlockAction

class BlockActionExecutor(private val service: AccessibilityService) {

    fun execute(action: BlockAction): Boolean {
        return when (action) {
            BlockAction.GO_BACK -> goBack()
            BlockAction.GO_HOME_AND_REOPEN_APP -> goHomeAndReopenApp()
            BlockAction.NONE -> false
        }
    }

    private fun goBack(): Boolean {
        return try {
            service.performGlobalAction(AccessibilityService.GLOBAL_ACTION_BACK)
        } catch (e: Exception) {
            false
        }
    }

    private fun goHomeAndReopenApp(): Boolean {
        return try {
            service.performGlobalAction(AccessibilityService.GLOBAL_ACTION_HOME)
        } catch (e: Exception) {
            false
        }
    }
}
