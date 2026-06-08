package com.mindguard.shared.acceptance

fun feature(description: String, block: FeatureContext.() -> Unit) =
    FeatureContext(description).block()

class FeatureContext(val name: String) {
    fun scenario(description: String, block: ScenarioContext.() -> Unit) =
        ScenarioContext(name, description).block()
}

class ScenarioContext(val feature: String, val name: String) {
    fun given(description: String, block: () -> Unit) = block()
    fun `when`(description: String, block: () -> Unit) = block()
    fun then(description: String, block: () -> Unit) = block()
    fun and(description: String, block: () -> Unit) = block()
    fun but(description: String, block: () -> Unit) = block()
}
