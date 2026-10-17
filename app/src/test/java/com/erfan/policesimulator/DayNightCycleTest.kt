package com.erfan.policesimulator

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class DayNightCycleTest {
    @Test
    fun startsWithExpectedClockAndDayPhase() {
        val state = DayNightCycle.at(8f * 3_600f)
        assertEquals("08:00", state.clockText)
        assertEquals(DayPhase.DAY, state.phase)
        assertEquals(1f, state.daylight, 0.001f)
        assertFalse(state.streetLightsOn)
    }

    @Test
    fun streetLightsTurnOnAfterSunsetAndOffAfterSunrise() {
        assertTrue(DayNightCycle.at(19f * 3_600f).streetLightsOn)
        assertTrue(DayNightCycle.at(2f * 3_600f).streetLightsOn)
        assertFalse(DayNightCycle.at(8f * 3_600f).streetLightsOn)
    }

    @Test
    fun nightSkyIsDarkerThanNoonSky() {
        val night = DayNightCycle.at(0f)
        val noon = DayNightCycle.at(12f * 3_600f)
        assertTrue(night.sceneBrightness < noon.sceneBrightness)
        assertNotEquals(night.skyBlue, noon.skyBlue)
    }

    @Test
    fun sunriseAndSunsetHaveTransitionPhases() {
        assertEquals(DayPhase.DAWN, DayNightCycle.at(6f * 3_600f).phase)
        assertEquals(DayPhase.DUSK, DayNightCycle.at(18f * 3_600f).phase)
        assertTrue(DayNightCycle.at(6f * 3_600f).daylight in 0f..1f)
    }

    @Test
    fun clockWrapsAcrossMidnight() {
        val advanced = DayNightCycle.advance(86_390f, 1f)
        assertTrue(advanced >= 0f && advanced < DayNightCycle.GAME_DAY_SECONDS)
        assertEquals("00:01", DayNightCycle.at(advanced).clockText)
    }

    @Test
    fun nightStreetLightingAndWorldBrightnessStayWithinValidRange() {
        val midnight = DayNightCycle.at(0f)
        val evening = DayNightCycle.at(20f * 3_600f)
        val afternoon = DayNightCycle.at(14f * 3_600f)
        assertTrue(midnight.streetLightsOn)
        assertTrue(evening.streetLightsOn)
        assertFalse(afternoon.streetLightsOn)
        assertTrue(midnight.sceneBrightness in 0f..1f)
        assertTrue(afternoon.sceneBrightness in 0f..1f)
        assertTrue(midnight.sceneBrightness < afternoon.sceneBrightness)
    }

    @Test
    fun timeAdvancesAtExpectedGameRate() {
        val afterOneSecond = DayNightCycle.advance(8f * 3_600f, 1f)
        assertEquals(8f * 3_600f + 90f, afterOneSecond, 0.01f)
    }

    @Test(expected = IllegalArgumentException::class)
    fun rejectsNegativeDelta() {
        DayNightCycle.advance(0f, -1f)
    }
}
