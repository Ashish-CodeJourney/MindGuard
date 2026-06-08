package com.mindguard.shared.rules

import com.mindguard.shared.models.DetectionResult
import com.mindguard.shared.models.ScreenSnapshot

interface BlockingRule {
    fun evaluate(snapshot: ScreenSnapshot): DetectionResult
}
