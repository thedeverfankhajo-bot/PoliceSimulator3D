package com.erfan.policesimulator

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class IncidentRulesTest {
    @Test
    fun activeWarrantAndArrestAwardsFullScore() {
        val outcome = IncidentRules.resolve(true, IncidentAction.ARREST)

        assertEquals(250, outcome.scoreDelta)
        assertTrue(outcome.correct)
    }

    @Test
    fun noWarrantAndCitationOrWarningAwardsScore() {
        val outcome = IncidentRules.resolve(false, IncidentAction.CITE_OR_WARN)

        assertEquals(120, outcome.scoreDelta)
        assertTrue(outcome.correct)
    }

    @Test
    fun citationDoesNotIgnoreAnActiveWarrant() {
        val outcome = IncidentRules.resolve(true, IncidentAction.CITE_OR_WARN)

        assertEquals(-60, outcome.scoreDelta)
        assertFalse(outcome.correct)
    }

    @Test
    fun arrestWithoutWarrantIsFlagged() {
        val outcome = IncidentRules.resolve(false, IncidentAction.ARREST)

        assertEquals(-100, outcome.scoreDelta)
        assertFalse(outcome.correct)
    }
}
