package com.mindguard.shared.acceptance

import kotlin.test.Test
import kotlin.test.assertEquals

class GherkinDslTest {

    @Test
    fun dslExecutesAllStepsAndForwardsAssertions() {
        var steps = mutableListOf<String>()

        feature("Gherkin DSL") {
            scenario("All step keywords execute their lambdas") {
                given("a fresh scenario") { steps += "given" }
                `when`("each step keyword is called") { steps += "when" }
                then("the lambda runs immediately") { steps += "then" }
                and("additional clauses also run") { steps += "and" }
                but("negation clauses also run") { steps += "but" }
            }
        }

        assertEquals(listOf("given", "when", "then", "and", "but"), steps)
    }

    @Test
    fun dslPropagatesAssertionFailures() {
        var reached = false
        var exceptionCaught = false

        try {
            feature("Error propagation") {
                scenario("Assertion failure is not swallowed") {
                    then("a failing assertion throws") { assertEquals(1, 2, "intentional failure") }
                    and("this line is never reached") { reached = true }
                }
            }
        } catch (e: AssertionError) {
            exceptionCaught = true
        }

        assertEquals(false, reached)
        assertEquals(true, exceptionCaught)
    }
}
