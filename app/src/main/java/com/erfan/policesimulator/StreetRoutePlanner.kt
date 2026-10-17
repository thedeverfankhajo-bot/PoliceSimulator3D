package com.erfan.policesimulator

import kotlin.math.abs
import kotlin.math.min
import kotlin.math.roundToInt
import kotlin.math.sqrt

/** Integer-coordinate street intersection on the generated seven-by-seven road grid. */
internal data class RoadNode(val gridX: Int, val gridZ: Int) {
    val worldX: Float get() = gridX * StreetRoutePlanner.ROAD_SPACING
    val worldZ: Float get() = gridZ * StreetRoutePlanner.ROAD_SPACING
}

internal data class RouteProgress(
    val x: Float,
    val z: Float,
    val routeIndex: Int,
    val finished: Boolean,
    val headingRadians: Float
)

/**
 * Grid-based street routing for suspect driving. Edges overlapping building footprints are
 * removed before the route is chosen; this keeps the suspect on the road network instead of
 * steering through generated building blocks. BFS is sufficient because each street segment
 * has the same length and avoids the need for an external pathfinding/game-engine dependency.
 */
internal object StreetRoutePlanner {
    const val ROAD_SPACING = 40f
    private const val GRID_MIN = -3
    private const val GRID_MAX = 3
    private const val ROAD_CLEARANCE = 2.0f

    fun nearestNode(x: Float, z: Float): RoadNode {
        require(x.isFinite() && z.isFinite())
        return RoadNode(
            (x / ROAD_SPACING).roundToInt().coerceIn(GRID_MIN, GRID_MAX),
            (z / ROAD_SPACING).roundToInt().coerceIn(GRID_MIN, GRID_MAX)
        )
    }

    /**
     * Find a route to a reachable intersection far from the patrol car. The returned path
     * includes both the snapped start node and the escape target. Ties are deterministic.
     */
    fun planEscapeRoute(
        startX: Float,
        startZ: Float,
        patrolX: Float,
        patrolZ: Float,
        obstacles: List<CollisionRect> = emptyList()
    ): List<RoadNode> {
        require(listOf(startX, startZ, patrolX, patrolZ).all { it.isFinite() })
        val start = nearestNode(startX, startZ)
        val parents = linkedMapOf<RoadNode, RoadNode?>()
        val queue = ArrayDeque<RoadNode>()
        parents[start] = null
        queue.addLast(start)

        while (queue.isNotEmpty()) {
            val current = queue.removeFirst()
            for (next in neighbors(current)) {
                if (parents.containsKey(next)) continue
                if (!edgeIsClear(current, next, obstacles)) continue
                parents[next] = current
                queue.addLast(next)
            }
        }

        val goal = parents.keys.maxWithOrNull(
            compareBy<RoadNode> { manhattan(it.worldX, it.worldZ, patrolX, patrolZ) }
                .thenBy { manhattan(it.worldX, it.worldZ, start.worldX, start.worldZ) }
                .thenBy { it.gridX }
                .thenBy { it.gridZ }
        ) ?: return emptyList()

        val reversed = ArrayList<RoadNode>()
        var cursor: RoadNode? = goal
        while (cursor != null) {
            reversed.add(cursor)
            cursor = parents[cursor]
        }
        reversed.reverse()
        return reversed
    }

    /** Advance toward one route node without cutting across the corner of a street turn. */
    fun advanceAlongRoute(
        route: List<RoadNode>,
        routeIndex: Int,
        currentX: Float,
        currentZ: Float,
        speed: Float,
        deltaSeconds: Float
    ): RouteProgress {
        require(currentX.isFinite() && currentZ.isFinite())
        require(speed.isFinite() && speed >= 0f)
        require(deltaSeconds.isFinite() && deltaSeconds >= 0f)
        require(routeIndex >= 0)

        if (route.isEmpty() || routeIndex >= route.size) {
            return RouteProgress(currentX, currentZ, route.size, true, 0f)
        }

        val target = route[routeIndex]
        val dx = target.worldX - currentX
        val dz = target.worldZ - currentZ
        val distance = sqrt(dx * dx + dz * dz)
        val heading = headingFor(dx, dz)
        val travel = speed * deltaSeconds

        if (distance <= 0.001f || travel >= distance) {
            val nextIndex = routeIndex + 1
            return RouteProgress(
                target.worldX,
                target.worldZ,
                nextIndex,
                nextIndex >= route.size,
                heading
            )
        }
        if (travel <= 0f) return RouteProgress(currentX, currentZ, routeIndex, false, heading)

        val fraction = min(1f, travel / distance)
        return RouteProgress(
            currentX + dx * fraction,
            currentZ + dz * fraction,
            routeIndex,
            false,
            heading
        )
    }

    private fun neighbors(node: RoadNode): List<RoadNode> = listOf(
        RoadNode(node.gridX - 1, node.gridZ),
        RoadNode(node.gridX + 1, node.gridZ),
        RoadNode(node.gridX, node.gridZ - 1),
        RoadNode(node.gridX, node.gridZ + 1)
    ).filter { it.gridX in GRID_MIN..GRID_MAX && it.gridZ in GRID_MIN..GRID_MAX }

    private fun edgeIsClear(a: RoadNode, b: RoadNode, obstacles: List<CollisionRect>): Boolean {
        val clearance = ROAD_CLEARANCE
        if (a.gridZ == b.gridZ) {
            val lineZ = a.worldZ
            val minX = min(a.worldX, b.worldX)
            val maxX = maxOf(a.worldX, b.worldX)
            return obstacles.none { rect ->
                lineZ >= rect.centerZ - rect.halfDepth - clearance &&
                    lineZ <= rect.centerZ + rect.halfDepth + clearance &&
                    maxX >= rect.centerX - rect.halfWidth - clearance &&
                    minX <= rect.centerX + rect.halfWidth + clearance
            }
        }
        val lineX = a.worldX
        val minZ = min(a.worldZ, b.worldZ)
        val maxZ = maxOf(a.worldZ, b.worldZ)
        return obstacles.none { rect ->
            lineX >= rect.centerX - rect.halfWidth - clearance &&
                lineX <= rect.centerX + rect.halfWidth + clearance &&
                maxZ >= rect.centerZ - rect.halfDepth - clearance &&
                minZ <= rect.centerZ + rect.halfDepth + clearance
        }
    }

    private fun headingFor(dx: Float, dz: Float): Float = when {
        abs(dx) > abs(dz) && dx > 0f -> (-Math.PI / 2.0).toFloat()
        abs(dx) > abs(dz) -> (Math.PI / 2.0).toFloat()
        dz > 0f -> Math.PI.toFloat()
        else -> 0f
    }

    private fun manhattan(ax: Float, az: Float, bx: Float, bz: Float) =
        abs(ax - bx) + abs(az - bz)
}
