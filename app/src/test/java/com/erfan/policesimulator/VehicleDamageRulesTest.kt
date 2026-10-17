package com.erfan.policesimulator

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class VehicleDamageRulesTest {
    @Test
    fun lowSpeedBuildingImpactCausesSmallDamage() {
        assertEquals(96, VehicleDamageRules.applyImpact(100, 2f, false))
    }

    @Test
    fun trafficImpactCausesMoreDamageThanAComparableBuildingImpact() {
        val building = VehicleDamageRules.applyImpact(100, 14f, false)
        val traffic = VehicleDamageRules.applyImpact(100, 14f, true)
        assertEquals(85, building)
        assertEquals(81, traffic)
    }

    @Test
    fun heavyImpactHasGreaterDamageThanLightImpact() {
        val light = VehicleDamageRules.applyImpact(100, 6f, false)
        val heavy = VehicleDamageRules.applyImpact(100, 25f, false)
        assertTrue(heavy < light)
    }

    @Test
    fun conditionNeverFallsBelowZero() {
        assertEquals(0, VehicleDamageRules.applyImpact(5, 30f, true))
    }

    @Test
    fun healthyVehicleKeepsFullSpeedAndAcceleration() {
        assertEquals(30f, VehicleDamageRules.maximumSpeed(100), 0.001f)
        assertEquals(1f, VehicleDamageRules.accelerationMultiplier(100), 0.001f)
    }

    @Test
    fun damagedVehicleHasReducedSpeedAndAcceleration() {
        assertEquals(17f, VehicleDamageRules.maximumSpeed(40), 0.001f)
        assertEquals(0.72f, VehicleDamageRules.accelerationMultiplier(40), 0.001f)
        assertTrue(VehicleDamageRules.maximumSpeed(10) < VehicleDamageRules.maximumSpeed(40))
    }

    @Test(expected = IllegalArgumentException::class)
    fun rejectsConditionAboveMaximum() {
        VehicleDamageRules.maximumSpeed(101)
    }

    @Test(expected = IllegalArgumentException::class)
    fun rejectsNegativeImpactSpeed() {
        VehicleDamageRules.applyImpact(100, -1f, false)
    }
}
