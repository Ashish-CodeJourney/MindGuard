package com.mindguard.accessibility

class AccessibilityEventDebouncer(private val debounceMs: Long = 100L) {

    private var lastEventTimeMs: Long = -1
    private var pendingRunnable: Runnable? = null

    fun debounce(runnable: Runnable): Boolean {
        val now = System.currentTimeMillis()

        if (lastEventTimeMs == -1L || (now - lastEventTimeMs) >= debounceMs) {
            lastEventTimeMs = now
            pendingRunnable = null
            return true
        }

        pendingRunnable = runnable
        return false
    }

    fun getPendingRunnable(): Runnable? {
        return pendingRunnable
    }

    fun clearPending() {
        pendingRunnable = null
    }
}
