package com.mindguard.accessibility

import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityNodeInfo
import com.mindguard.shared.models.ScreenSnapshot

class AccessibilityEventConverter {

    fun toScreenSnapshot(event: AccessibilityEvent): ScreenSnapshot? {
        val packageName = event.packageName?.toString() ?: return null
        val currentTimeMillis = System.currentTimeMillis()

        val screenText = extractText(event)
        val contentDescriptions = extractContentDescriptions(event)
        val resourceIds = extractResourceIds(event)

        return ScreenSnapshot(
            packageName = packageName,
            screenText = screenText,
            contentDescriptions = contentDescriptions,
            resourceIds = resourceIds,
            timestampMillis = currentTimeMillis
        )
    }

    private fun extractText(event: AccessibilityEvent): List<String> {
        val texts = mutableListOf<String>()

        event.text?.forEach { text ->
            if (text?.isNotBlank() == true) {
                texts.add(text.toString())
            }
        }

        return texts
    }

    private fun extractContentDescriptions(event: AccessibilityEvent): List<String> {
        val descriptions = mutableListOf<String>()

        if (event.contentDescription?.isNotBlank() == true) {
            descriptions.add(event.contentDescription.toString())
        }

        return descriptions
    }

    private fun extractResourceIds(event: AccessibilityEvent): List<String> {
        val resourceIds = mutableListOf<String>()

        // Try to extract from event source
        val source = event.source
        if (source != null) {
            val viewId = source.viewIdResourceName
            if (viewId?.isNotBlank() == true) {
                resourceIds.add(viewId)
            }

            // Traverse hierarchy for additional resource IDs
            traverseNodeHierarchy(source, resourceIds)
            source.recycle()
        }

        return resourceIds
    }

    private fun traverseNodeHierarchy(node: AccessibilityNodeInfo, resourceIds: MutableList<String>, depth: Int = 0) {
        if (depth > 5) return // Limit traversal depth

        repeat(node.childCount) { i ->
            val child = node.getChild(i)
            if (child != null) {
                val viewId = child.viewIdResourceName
                if (viewId?.isNotBlank() == true && !resourceIds.contains(viewId)) {
                    resourceIds.add(viewId)
                }
                traverseNodeHierarchy(child, resourceIds, depth + 1)
                child.recycle()
            }
        }
    }
}
