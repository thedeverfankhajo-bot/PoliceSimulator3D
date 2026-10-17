package com.erfan.policesimulator

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class VehicleGarageRulesTest {
    @Test
    fun quoteScalesWithMissingVehicleCondition() {
        assertEquals(0, VehicleGarageRules.quote(100))
        assertEquals(200, VehicleGarageRules.quote(60))
        assertEquals(500, VehicleGarageRules.quote(0))
    }

    @Test
    fun affordableRepairRestoresFullConditionAndDeductsCredits() {
        val result = VehicleGarageRules.repair(condition = 60, credits = 500)
        assertTrue(result.repaired)
        assertEquals(100, result.vehicleCondition)
        assertEquals(300, result.credits)
        assertEquals(200, result.cost)
    }

    @Test
    fun repairIsRejectedWhenCreditsAreInsufficient() {
        val result = VehicleGarageRules.repair(condition = 40, credits = 100)
        assertFalse(result.repaired)
        assertEquals(40, result.vehicleCondition)
        assertEquals(100, result.credits)
        assertEquals(300, result.cost)
    }

    @Test
    fun fullyRepairedVehicleDoesNotChargeCredits() {
        val result = VehicleGarageRules.repair(condition = 100, credits = 12)
        assertFalse(result.repaired)
        assertEquals(100, result.vehicleCondition)
        assertEquals(12, result.credits)
        assertEquals(0, result.cost)
    }

    @Test(expected = IllegalArgumentException::class)
    fun rejectsInvalidCondition() {
        VehicleGarageRules.quote(101)
    }

    @Test(expected = IllegalArgumentException::class)
    fun rejectsNegativeCredits() {
        VehicleGarageRules.repair(50, -1)
    }
}
