package com.erfan.policesimulator

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class DispatchCalloutFactoryTest {
    @Test
    fun calloutsRotateAcrossFourDistinctIncidentTypes() {
        val types = (0..3).map { DispatchCalloutFactory.create(it, 123L).type }.toSet()
        assertEquals(4, types.size)
    }

    @Test
    fun speedingCalloutIsCompliantAndDoesNotInventAWarrant() {
        val call = DispatchCalloutFactory.create(2, 456L)
        assertEquals(DispatchType.SPEEDING_DRIVER, call.type)
        assertEquals(SuspectResponse.COMPLIES, call.suspectResponse)
        assertEquals(false, call.hasActiveWarrant)
    }

    @Test
    fun stolenVehicleCalloutAlwaysHasPriorityWarrantAndFleeingResponse() {
        val call = DispatchCalloutFactory.create(3, 456L)
        assertEquals(DispatchType.STOLEN_VEHICLE, call.type)
        assertEquals(SuspectResponse.FLEES, call.suspectResponse)
        assertTrue(call.hasActiveWarrant)
    }

    @Test
    fun calloutGenerationIsDeterministicForSameSeedAndIndex() {
        assertEquals(
            DispatchCalloutFactory.create(0, 9876L),
            DispatchCalloutFactory.create(0, 9876L)
        )
    }

    @Test
    fun calloutProfilesHaveDifferentDispatchAndObjectiveText() {
        val suspicious = DispatchCalloutFactory.create(0, 1L)
        val stolen = DispatchCalloutFactory.create(3, 1L)
        assertNotEquals(suspicious.dispatchMessage, stolen.dispatchMessage)
        assertNotEquals(suspicious.approachObjective, stolen.approachObjective)
        assertNotEquals(suspicious.targetVehicleColor, stolen.targetVehicleColor)
    }
}
