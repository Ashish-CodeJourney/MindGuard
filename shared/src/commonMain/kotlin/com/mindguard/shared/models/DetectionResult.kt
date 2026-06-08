package com.mindguard.shared.models

data class DetectionResult(
    val shouldBlock: Boolean,
    val action: BlockAction,
    val reason: String?
)
