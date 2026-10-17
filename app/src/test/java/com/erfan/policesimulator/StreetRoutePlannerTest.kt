package com.erfan.policesimulator

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class StreetRoutePlannerTest {
    @Test
    fun escapeRouteStartsAtNearestStreetNodeAndMovesAwayFromPatrol() {
        val route = StreetRoutePlanner.planEscapeRoute(
            startX = 2f,
            startZ = -38f,
            patrolX = 0f,
            patrolZ = 12f
        )

        assertTrue(route.size >= 2)
        assertEquals(RoadNode(0, -1), route.first())
        val startDistance = kotlin.math.abs(route.first().worldX - 0f) +
            kotlin.math.abs(route.first().worldZ - 12f)
        val endDistance = kotlin.math.abs(route.last().worldX - 0f) +
            kotlin.math.abs(route.last().worldZ - 12f)
        assertTrue(endDistance > startDistance)
    }

    @Test
    fun plannedRouteUsesOnlyAdjacentStreetIntersections() {
        val route = StreetRoutePlanner.planEscapeRoute(0f, 0f, -25f, 10f)

        assertTrue(route.size >= 2)
        route.zipWithNext().forEach { (a, b) ->
            val gridSteps = kotlin.math.abs(a.gridX - b.gridX) + kotlin.math.abs(a.gridZ - b.gridZ)
            assertEquals(1, gridSteps)
        }
    }

    @Test
    fun plannerRoutesAroundABlockedStreetSegment() {
        val obstruction = CollisionRect(centerX = 20f, centerZ = 0f, halfWidth = 5f, halfDepth = 5f)
        val route = StreetRoutePlanner.planEscapeRoute(
            startX = 0f,
            startZ = 0f,
            patrolX = -100f,
            patrolZ = 0f,
            obstacles = listOf(obstruction)
        )

        assertTrue(route.size >= 3)
        assertFalse(route[1] == RoadNode(1, 0))
        route.zipWithNext().forEach { (a, b) ->
            assertFalse(
                "Route edge should not intersect the blocked street segment",
                if (a.gridZ == b.gridZ) {
                    val low = minOf(a.worldX, b.worldX)
                    val high = maxOf(a.worldX, b.worldX)
                    0f >= obstruction.centerZ - obstruction.halfDepth - 2f &&
                        0f <= obstruction.centerZ + obstruction.halfDepth + 2f &&
                        high >= obstruction.centerX - obstruction.halfWidth - 2f &&
                        low <= obstruction.centerX + obstruction.halfWidth + 2f
                } else {
                    val x = a.worldX
                    x >= obstruction.centerX - obstruction.halfWidth - 2f &&
                        x <= obstruction.centerX + obstruction.halfWidth + 2f &&
                        maxOf(a.worldZ, b.worldZ) >= obstruction.centerZ - obstruction.halfDepth - 2f &&
                        minOf(a.worldZ, b.worldZ) <= obstruction.centerZ + obstruction.halfDepth + 2f
                }
            )
        }
    }

    @Test
    fun advanceMovesAlongOneStreetSegmentAndPreservesRouteIndexUntilNode() {
        val route = listOf(RoadNode(0, 0), RoadNode(1, 0), RoadNode(1, 1))
        val partial = StreetRoutePlanner.advanceAlongRoute(route, 1, 0f, 0f, 10f, 1f)
        assertEquals(10f, partial.x, 0.001f)
        assertEquals(0f, partial.z, 0.001f)
        assertEquals(1, partial.routeIndex)
        assertFalse(partial.finished)

        val reached = StreetRoutePlanner.advanceAlongRoute(route, 1, 30f, 0f, 15f, 1f)
        assertEquals(40f, reached.x, 0.001f)
        assertEquals(0f, reached.z, 0.001f)
        assertEquals(2, reached.routeIndex)
        assertFalse(reached.finished)
    }

    @Test
    fun advancingPastFinalNodeReportsCompletion() {
        val route = listOf(RoadNode(0, 0), RoadNode(1, 0))
        val end = StreetRoutePlanner.advanceAlongRoute(route, 1, 35f, 0f, 10f, 1f)
        assertEquals(40f, end.x, 0.001f)
        assertTrue(end.finished)
    }

    @Test
    fun zeroSpeedDoesNotMoveSuspect() {
        val route = listOf(RoadNode(0, 0), RoadNode(1, 0))
        val stopped = StreetRoutePlanner.advanceAlongRoute(route, 1, 4f, 0f, 0f, 0.5f)
        assertEquals(4f, stopped.x, 0.001f)
        assertEquals(0f, stopped.z, 0.001f)
        assertFalse(stopped.finished)
    }
}
