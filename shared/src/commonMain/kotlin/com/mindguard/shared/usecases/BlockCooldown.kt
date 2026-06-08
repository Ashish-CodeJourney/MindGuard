package com.mindguard.shared.usecases

class BlockCooldown(private val cooldownMs: Long) {

    var lastBlockTimeMs: Long = -1
        private set

    fun canBlock(currentTimeMs: Long): Boolean {
        if (lastBlockTimeMs == -1L) {
            return true
        }

        val timeSinceLastBlock = currentTimeMs - lastBlockTimeMs
        return timeSinceLastBlock >= cooldownMs
    }

    fun recordBlock(currentTimeMs: Long) {
        lastBlockTimeMs = currentTimeMs
    }
}
