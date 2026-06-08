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
            BlockAction.CLICK_SAFE_TAB -> clickSafeTab()
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

    // Navigates to a safe in-app tab rather than pressing Back, which can close the app.
    // Tries known Home-feed tab IDs for Instagram; falls back to GLOBAL_ACTION_BACK.
    private fun clickSafeTab(): Boolean {
        val root = try { service.rootInActiveWindow } catch (e: Exception) { null }
            ?: return goBack()
        return try {
            val instagramHomeTabs = listOf(
                "com.instagram.android:id/feed_tab",
                "com.instagram.android:id/tab_feed"
            )
            for (id in instagramHomeTabs) {
                val nodes = root.findAccessibilityNodeInfosByViewId(id)
                val tab = nodes.firstOrNull()
                if (tab != null) {
                    val clicked = tab.performAction(AccessibilityNodeInfo.ACTION_CLICK)
                    nodes.forEach { it.recycle() }
                    if (clicked) return true
                }
            }
            goBack()
        } finally {
            root.recycle()
        }
    }
}
