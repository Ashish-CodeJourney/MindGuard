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

    // Navigates to the Instagram Home-feed tab rather than pressing Back.
    // Back press inside the Reels player stays within the reel stack causing an infinite loop.
    // Strategy: try 6 known resource IDs across builds → try content-description "Home" →
    // fall back to Android HOME key. GO_BACK is never used.
    private fun clickSafeTab(): Boolean {
        val root = try { service.rootInActiveWindow } catch (e: Exception) { null }
            ?: return service.performGlobalAction(AccessibilityService.GLOBAL_ACTION_HOME)
        return try {
            // 1. Try known resource IDs in priority order
            for (id in InstagramHomeTabStrategy.ORDERED_IDS) {
                val nodes = root.findAccessibilityNodeInfosByViewId(id)
                val tab = nodes.firstOrNull()
                nodes.forEach { it.recycle() }
                if (tab != null && tab.performAction(AccessibilityNodeInfo.ACTION_CLICK)) return true
            }
            // 2. Try content description "Home" (version-agnostic)
            val homeNodes = root.findAccessibilityNodeInfosByText("Home")
            val homeTab = homeNodes.firstOrNull { it.isClickable }
            homeNodes.forEach { it.recycle() }
            if (homeTab != null && homeTab.performAction(AccessibilityNodeInfo.ACTION_CLICK)) return true
            // 3. Last resort: Android HOME key — never GO_BACK
            service.performGlobalAction(AccessibilityService.GLOBAL_ACTION_HOME)
        } finally {
            root.recycle()
        }
    }
}
