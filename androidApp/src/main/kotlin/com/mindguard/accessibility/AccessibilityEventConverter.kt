package com.mindguard.accessibility

import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityNodeInfo
import com.mindguard.shared.models.ScreenSnapshot

class AccessibilityEventConverter {

    fun toScreenSnapshot(event: AccessibilityEvent): ScreenSnapshot? {
        val packageName = event.packageName?.toString() ?: return null

        val text = mutableListOf<String>()
        val descriptions = mutableListOf<String>()
        val resourceIds = mutableListOf<String>()

        // Text directly in the event
        event.text?.forEach { if (it?.isNotBlank() == true) text.add(it.toString()) }
        if (event.contentDescription?.isNotBlank() == true) {
            descriptions.add(event.contentDescription.toString())
        }

        // Traverse the source node and its hierarchy (depth 15)
        val source = event.source
        if (source != null) {
            traverseNode(source, text, descriptions, resourceIds)
            source.recycle()
        }

        return ScreenSnapshot(
            packageName = packageName,
            screenText = text.distinct(),
            contentDescriptions = descriptions.distinct(),
            resourceIds = resourceIds.distinct(),
            timestampMillis = System.currentTimeMillis()
        )
    }

    /** Build a snapshot from a root AccessibilityNodeInfo (for watchdog scans). */
    fun toScreenSnapshotFromRoot(root: AccessibilityNodeInfo, packageName: String): ScreenSnapshot? {
        val text = mutableListOf<String>()
        val descriptions = mutableListOf<String>()
        val resourceIds = mutableListOf<String>()
        traverseNode(root, text, descriptions, resourceIds)
        if (resourceIds.isEmpty() && text.isEmpty()) return null
        return ScreenSnapshot(
            packageName = packageName,
            screenText = text.distinct(),
            contentDescriptions = descriptions.distinct(),
            resourceIds = resourceIds.distinct(),
            timestampMillis = System.currentTimeMillis()
        )
    }

    /** Merge a root-scan's findings into an existing snapshot. */
    fun augmentWithRoot(snapshot: ScreenSnapshot, root: AccessibilityNodeInfo): ScreenSnapshot {
        val text = snapshot.screenText.toMutableList()
        val descriptions = snapshot.contentDescriptions.toMutableList()
        val resourceIds = snapshot.resourceIds.toMutableList()
        traverseNode(root, text, descriptions, resourceIds)
        return snapshot.copy(
            screenText = text.distinct(),
            contentDescriptions = descriptions.distinct(),
            resourceIds = resourceIds.distinct()
        )
    }

    private fun traverseNode(
        node: AccessibilityNodeInfo,
        text: MutableList<String>,
        descriptions: MutableList<String>,
        resourceIds: MutableList<String>,
        depth: Int = 0
    ) {
        if (depth > MAX_DEPTH) return

        node.viewIdResourceName?.takeIf { it.isNotBlank() }?.let {
            if (!resourceIds.contains(it)) resourceIds.add(it)
        }
        node.text?.toString()?.takeIf { it.isNotBlank() }?.let {
            if (!text.contains(it)) text.add(it)
        }
        node.contentDescription?.toString()?.takeIf { it.isNotBlank() }?.let {
            if (!descriptions.contains(it)) descriptions.add(it)
        }

        repeat(node.childCount) { i ->
            val child = node.getChild(i) ?: return@repeat
            traverseNode(child, text, descriptions, resourceIds, depth + 1)
            child.recycle()
        }
    }

    companion object {
        // Deep enough to reach reel containers in Instagram/YouTube view hierarchies
        private const val MAX_DEPTH = 15
    }
}
