package com.mindguard.shared.usecases

fun isPausedAt(pauseUntilMs: Long, currentTimeMs: Long): Boolean =
    pauseUntilMs > 0L && currentTimeMs < pauseUntilMs
