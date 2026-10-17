package com.erfan.policesimulator

import org.junit.Assert.assertEquals
import org.junit.Test

class CareerProgressTest {
    @Test
    fun recordingCorrectDecisionUpdatesLifetimeStats() {
        val profile = CareerProgress(xp = 40, callsCompleted = 2, correctDecisions = 2)
            .recordDecision(IncidentRules.resolve(hasActiveWarrant = true, action = IncidentAction.ARREST))

        assertEquals(290, profile.xp)
        assertEquals(3, profile.callsCompleted)
        assertEquals(3, profile.correctDecisions)
        assertEquals(0, profile.incorrectDecisions)
    }

    @Test
    fun incorrectDecisionAppliesPenaltyAndIncrementsReviewCounter() {
        val profile = CareerProgress(xp = 70, callsCompleted = 1, correctDecisions = 1)
            .recordDecision(IncidentRules.resolve(hasActiveWarrant = false, action = IncidentAction.ARREST))

        assertEquals(0, profile.xp)
        assertEquals(2, profile.callsCompleted)
        assertEquals(1, profile.correctDecisions)
        assertEquals(1, profile.incorrectDecisions)
    }

    @Test
    fun xpNeverBecomesNegative() {
        val profile = CareerProgress(xp = 10)
            .recordDecision(IncidentRules.resolve(hasActiveWarrant = true, action = IncidentAction.CITE_OR_WARN))

        assertEquals(0, profile.xp)
        assertEquals(1, profile.callsCompleted)
    }

    @Test
    fun countersAndXpSaturateInsteadOfOverflowing() {
        val profile = CareerProgress(
            xp = Int.MAX_VALUE,
            callsCompleted = Int.MAX_VALUE,
            correctDecisions = Int.MAX_VALUE
        ).recordDecision(IncidentRules.resolve(hasActiveWarrant = true, action = IncidentAction.ARREST))

        assertEquals(Int.MAX_VALUE, profile.xp)
        assertEquals(Int.MAX_VALUE, profile.callsCompleted)
        assertEquals(Int.MAX_VALUE, profile.correctDecisions)
    }
}
