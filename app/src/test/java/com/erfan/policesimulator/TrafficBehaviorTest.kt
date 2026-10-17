package com.erfan.policesimulator

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class TrafficBehaviorTest {
    @Test
    fun trafficOnlyYieldsWhenEmergencyEquipmentIsActive() {
        assertTrue(TrafficBehavior.shouldYield(true, 15f))
        assertFalse(TrafficBehavior.shouldYield(false, 1f))
        assertFalse(TrafficBehavior.shouldYield(true, 35f))
    }

    @Test
    fun trafficYieldsAtTheConfiguredBoundary() {
        assertTrue(TrafficBehavior.shouldYield(true, TrafficBehavior.YIELD_RADIUS))
    }

    @Test
    fun rightSideLaneMovesTowardItsRightShoulder() {
        val result = TrafficBehavior.approachShoulderOffset(
            currentOffset = 0f,
            lanePosition = 2.5f,
            roadCenter = 0f,
            yielding = true,
            deltaSeconds = 1f
        )
        assertEquals(TrafficBehavior.SHOULDER_OFFSET, result, 0.001f)
    }

    @Test
    fun leftSideLaneMovesTowardItsLeftShoulder() {
        val result = TrafficBehavior.approachShoulderOffset(
            currentOffset = 0f,
            lanePosition = -2.5f,
            roadCenter = 0f,
            yielding = true,
            deltaSeconds = 1f
        )
        assertEquals(-TrafficBehavior.SHOULDER_OFFSET, result, 0.001f)
    }

    @Test
    fun shoulderMotionIsSmoothedAndReturnsToLane() {
        val moved = TrafficBehavior.approachShoulderOffset(
            currentOffset = 0f,
            lanePosition = 2.5f,
            roadCenter = 0f,
            yielding = true,
            deltaSeconds = 0.1f
        )
        assertEquals(0.42f, moved, 0.001f)

        val returning = TrafficBehavior.approachShoulderOffset(
            currentOffset = moved,
            lanePosition = 2.5f,
            roadCenter = 0f,
            yielding = false,
            deltaSeconds = 0.1f
        )
        assertEquals(0.294f, returning, 0.001f)
    }

    @Test
    fun trafficSlowsDownAndThenSmoothlyResumesCruisingSpeed() {
        val slowed = TrafficBehavior.approachSpeedFactor(1f, true, 0.1f)
        assertEquals(0.805f, slowed, 0.001f)

        val recovering = TrafficBehavior.approachSpeedFactor(slowed, false, 0.1f)
        assertEquals(0.8635f, recovering, 0.001f)
    }

    @Test
    fun zeroDeltaDoesNotMoveTrafficResponseState() {
        assertEquals(
            0.8f,
            TrafficBehavior.approachSpeedFactor(0.8f, true, 0f),
            0.001f
        )
        assertEquals(
            0.4f,
            TrafficBehavior.approachShoulderOffset(0.4f, 2f, 0f, true, 0f),
            0.001f
        )
    }

    @Test(expected = IllegalArgumentException::class)
    fun negativeDistanceIsRejected() {
        TrafficBehavior.shouldYield(true, -1f)
    }
}
