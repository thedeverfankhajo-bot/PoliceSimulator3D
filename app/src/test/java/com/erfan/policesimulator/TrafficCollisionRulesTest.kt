package com.erfan.policesimulator

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class TrafficCollisionRulesTest {
    @Test
    fun distantVehiclesDoNotCollide() {
        val result = TrafficCollisionRules.resolve(
            playerX = -6f,
            playerZ = 0f,
            trafficX = 0f,
            trafficZ = 0f,
            trafficMovesVertically = true
        )

        assertFalse(result.collided)
        assertEquals(-6f, result.x, 0.001f)
        assertEquals(0f, result.z, 0.001f)
    }

    @Test
    fun verticalTrafficContactSeparatesTheVehicles() {
        val result = TrafficCollisionRules.resolve(
            playerX = -2f,
            playerZ = 0f,
            trafficX = 0f,
            trafficZ = 0f,
            trafficMovesVertically = true
        )

        assertTrue(result.collided)
        assertTrue(result.x <= -2.45f)
        assertEquals(0f, result.z, 0.001f)
    }

    @Test
    fun horizontalTrafficUsesItsRotatedFootprint() {
        val result = TrafficCollisionRules.resolve(
            playerX = 0f,
            playerZ = -2f,
            trafficX = 0f,
            trafficZ = 0f,
            trafficMovesVertically = false
        )

        assertTrue(result.collided)
        assertTrue(result.z <= -1.78f)
        assertEquals(0f, result.x, 0.001f)
    }

    @Test
    fun overlappingCentersAreSeparatedToNearestEdge() {
        val result = TrafficCollisionRules.resolve(
            playerX = 0f,
            playerZ = 0f,
            trafficX = 0f,
            trafficZ = 0f,
            trafficMovesVertically = true
        )

        assertTrue(result.collided)
        assertTrue(result.x < -2.4f)
        assertEquals(0f, result.z, 0.001f)
    }

    @Test(expected = IllegalArgumentException::class)
    fun rejectsInvalidCollisionRadius() {
        TrafficCollisionRules.resolve(0f, 0f, 0f, 0f, true, radius = 0f)
    }
}
