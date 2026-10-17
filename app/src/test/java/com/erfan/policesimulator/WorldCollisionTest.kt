package com.erfan.policesimulator

import org.junit.Assert.*
import org.junit.Test

class WorldCollisionTest {
    private val building = CollisionRect(0f, 0f, 2f, 2f)

    @Test fun clearMovementKeepsRequestedPosition() {
        val result = WorldCollision.move(-8f, -8f, 2f, 1f, 1f, listOf(building))
        assertEquals(-6f, result.x, 0.001f)
        assertEquals(-7f, result.z, 0.001f)
        assertFalse(result.collided)
    }

    @Test fun vehicleStopsBeforeIntersectingBuilding() {
        val result = WorldCollision.move(-8f, 0f, 10f, 0f, 1.5f, listOf(building))
        assertTrue(result.collidedX)
        assertTrue(result.x <= -3.49f)
        assertTrue(result.x >= -4.1f)
    }

    @Test fun substepsPreventFastMovementTunnelling() {
        val result = WorldCollision.move(-8f, 0f, 16f, 0f, 1.5f, listOf(building))
        assertTrue(result.collidedX)
        assertTrue(result.x < -2f)
    }

    @Test fun movementSlidesAlongWall() {
        val result = WorldCollision.move(-3.5f, -7f, 4f, 8f, 1.5f, listOf(building))
        assertTrue(result.collidedX)
        assertTrue(result.z > 0f)
        assertTrue(result.x <= -3.49f)
    }

    @Test fun movementStaysInsideWorldBounds() {
        val result = WorldCollision.move(154f, -154f, 30f, -30f, 1.5f, emptyList())
        assertTrue(result.collidedX)
        assertTrue(result.collidedZ)
        assertTrue(result.x <= 153.5f)
        assertTrue(result.z >= -153.5f)
    }
}
