package com.erfan.policesimulator

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class PursuitRulesTest {
    @Test fun emergencyApproachTriggersFlight() {
        val result = PursuitRules.update(PursuitState.SUSPICIOUS, 20f, true, 4f, 0.1f)
        assertEquals(PursuitState.FLEEING, result.state)
        assertTrue(result.targetSpeedFactor > 1f)
        assertTrue(result.shouldTurnAtJunction)
        assertFalse(result.escaped)
    }

    @Test fun distantSuspectCanEscape() {
        val result = PursuitRules.update(PursuitState.SUSPICIOUS, 170f, false, 5f, 0.1f)
        assertTrue(result.escaped)
        assertEquals(PursuitState.FLEEING, result.state)
    }

    @Test fun stoppedSuspectRemainsStopped() {
        val result = PursuitRules.update(PursuitState.STOPPED, 2f, false, 0f, 0.1f)
        assertEquals(PursuitState.STOPPED, result.state)
        assertEquals(0f, result.targetSpeedFactor, 0.001f)
        assertFalse(result.shouldTurnAtJunction)
    }

    @Test fun suspectSlowsWhenNotYetPursued() {
        val result = PursuitRules.update(PursuitState.SUSPICIOUS, 40f, false, 5f, 0.1f)
        assertEquals(PursuitState.SUSPICIOUS, result.state)
        assertEquals(PursuitRules.SUSPICIOUS_SPEED_FACTOR, result.targetSpeedFactor, 0.001f)
    }

    @Test fun speedFactorChangesSmoothly() {
        assertEquals(1.324f, PursuitRules.approachSpeedFactor(1f, 1.6f, 0.3f), 0.001f)
    }

    @Test fun junctionChoicePrefersGreaterManhattanDistance() {
        assertFalse(PursuitRules.chooseVerticalRoad(0f, 0f, 0f, 10f))
    }

    @Test(expected = IllegalArgumentException::class)
    fun rejectsNegativeDistance() {
        PursuitRules.update(PursuitState.SUSPICIOUS, -1f, false, 0f, 0.1f)
    }
}
