package com.erfan.policesimulator

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class HeadlightRulesTest {
    @Test
    fun autoFollowsStreetLightSchedule() {
        assertTrue(HeadlightRules.isOn(HeadlightMode.AUTO, DayNightCycle.at(20f * 3_600f)))
        assertFalse(HeadlightRules.isOn(HeadlightMode.AUTO, DayNightCycle.at(12f * 3_600f)))
    }

    @Test
    fun forcedOnAndOffOverrideDayNightCycle() {
        val noon = DayNightCycle.at(12f * 3_600f)
        val midnight = DayNightCycle.at(0f)
        assertTrue(HeadlightRules.isOn(HeadlightMode.ON, noon))
        assertTrue(HeadlightRules.isOn(HeadlightMode.ON, midnight))
        assertFalse(HeadlightRules.isOn(HeadlightMode.OFF, noon))
        assertFalse(HeadlightRules.isOn(HeadlightMode.OFF, midnight))
    }

    @Test
    fun modeCyclesAutoOnOffAuto() {
        assertEquals(HeadlightMode.ON, HeadlightRules.next(HeadlightMode.AUTO))
        assertEquals(HeadlightMode.OFF, HeadlightRules.next(HeadlightMode.ON))
        assertEquals(HeadlightMode.AUTO, HeadlightRules.next(HeadlightMode.OFF))
    }
}
