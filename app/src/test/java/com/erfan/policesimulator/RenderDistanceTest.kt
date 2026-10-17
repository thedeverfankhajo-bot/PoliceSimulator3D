package com.erfan.policesimulator

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class RenderDistanceTest {
    @Test
    fun nearbyObjectIsKept() {
        assertTrue(RenderDistance.intersectsCircle(0f, 0f, 20f, 10f, 2f, 2f, 90f))
    }

    @Test
    fun smallObjectOutsideRangeIsCulled() {
        assertFalse(RenderDistance.intersectsCircle(0f, 0f, 150f, 0f, 1f, 1f, 90f))
    }

    @Test
    fun objectTouchingViewDistanceBoundaryIsKept() {
        assertTrue(RenderDistance.intersectsCircle(0f, 0f, 95f, 0f, 5f, 2f, 90f))
    }

    @Test
    fun largeGroundPlaneIsKeptWhenItsCenterIsFarAway() {
        assertTrue(RenderDistance.intersectsCircle(150f, 150f, 0f, 0f, 180f, 180f, 90f))
    }

    @Test
    fun distantRoadSegmentCanRemainVisibleIfItsFootprintReachesThePlayer() {
        assertTrue(RenderDistance.intersectsCircle(0f, 0f, 120f, 0f, 40f, 6f, 90f))
    }

    @Test(expected = IllegalArgumentException::class)
    fun rejectsNegativeObjectExtent() {
        RenderDistance.intersectsCircle(0f, 0f, 0f, 0f, -1f, 2f, 90f)
    }
}
