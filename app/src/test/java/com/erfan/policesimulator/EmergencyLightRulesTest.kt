package com.erfan.policesimulator

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class EmergencyLightRulesTest {
    @Test
    fun redAndBlueFlashAlternatelyInSyncWithElapsedTime() {
        val red = EmergencyLightRules.state(0L, true, 0f)
        val blue = EmergencyLightRules.state(230L, true, 0f)
        assertTrue(red.redActive)
        assertFalse(red.blueActive)
        assertFalse(blue.redActive)
        assertTrue(blue.blueActive)
    }

    @Test
    fun disabledLightbarHasNoReflection() {
        val state = EmergencyLightRules.state(120L, false, 0f)
        assertFalse(state.redActive)
        assertFalse(state.blueActive)
        assertEquals(0f, state.reflectionIntensity, 0.001f)
    }

    @Test
    fun reflectionsAreStrongerAtNightThanInDaylight() {
        val night = EmergencyLightRules.state(0L, true, 0f)
        val day = EmergencyLightRules.state(0L, true, 1f)
        assertTrue(night.reflectionIntensity > day.reflectionIntensity)
        assertTrue(night.reflectionIntensity <= 0.72f)
        assertNotEquals(night.reflectionIntensity, day.reflectionIntensity)
    }

    @Test(expected = IllegalArgumentException::class)
    fun rejectsInvalidDaylightValue() {
        EmergencyLightRules.state(0L, true, 1.1f)
    }
}
