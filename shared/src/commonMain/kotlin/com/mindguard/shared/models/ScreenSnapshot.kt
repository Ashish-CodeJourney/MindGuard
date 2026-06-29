package com.mindguard.shared.models

data class ScreenSnapshot(
    val packageName: String,
    val screenText: List<String>,
    val contentDescriptions: List<String>,
    val resourceIds: List<String>,
    val timestampMillis: Long,
    val windowClassName: String? = null
) {
    init {
        require(packageName.isNotBlank()) { "packageName must not be blank" }
        require(timestampMillis > 0) { "timestampMillis must be positive" }
    }
}
