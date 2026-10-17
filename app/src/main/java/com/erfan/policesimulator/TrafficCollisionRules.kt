package com.erfan.policesimulator

import kotlin.math.sqrt

internal data class TrafficCollisionResult(
    val x: Float,
    val z: Float,
    val collided: Boolean
)

/**
 * Resolves contact between the player's circular footprint and a moving traffic car.
 * Traffic cars follow axis-aligned roads, so their footprint is a rotated 1.8 x 3.6 metre box.
 * This deterministic rule is separate from Android/OpenGL so contacts can be unit-tested.
 */
internal object TrafficCollisionRules {
    fun resolve(
        playerX: Float,
        playerZ: Float,
        trafficX: Float,
        trafficZ: Float,
        trafficMovesVertically: Boolean,
        radius: Float = 1.55f
    ): TrafficCollisionResult {
        require(playerX.isFinite() && playerZ.isFinite())
        require(trafficX.isFinite() && trafficZ.isFinite())
        require(radius > 0f && radius.isFinite())

        val halfWidth = if (trafficMovesVertically) 0.9f else 1.8f
        val halfDepth = if (trafficMovesVertically) 1.8f else 0.9f
        val left = trafficX - halfWidth
        val right = trafficX + halfWidth
        val near = trafficZ - halfDepth
        val far = trafficZ + halfDepth

        val nearestX = playerX.coerceIn(left, right)
        val nearestZ = playerZ.coerceIn(near, far)
        val dx = playerX - nearestX
        val dz = playerZ - nearestZ
        val distanceSquared = dx * dx + dz * dz
        val radiusSquared = radius * radius

        if (distanceSquared >= radiusSquared) {
            return TrafficCollisionResult(playerX, playerZ, collided = false)
        }

        // If the player center is inside the car rectangle, choose the nearest exit edge.
        if (distanceSquared < 0.000001f) {
            val toLeft = playerX - left
            val toRight = right - playerX
            val toNear = playerZ - near
            val toFar = far - playerZ
            val edgeDistance = minOf(toLeft, toRight, toNear, toFar)
            val padding = radius + 0.03f
            return when (edgeDistance) {
                toLeft -> TrafficCollisionResult(left - padding, playerZ, collided = true)
                toRight -> TrafficCollisionResult(right + padding, playerZ, collided = true)
                toNear -> TrafficCollisionResult(playerX, near - padding, collided = true)
                else -> TrafficCollisionResult(playerX, far + padding, collided = true)
            }
        }

        val distance = sqrt(distanceSquared)
        val penetration = radius - distance + 0.03f
        return TrafficCollisionResult(
            x = playerX + dx / distance * penetration,
            z = playerZ + dz / distance * penetration,
            collided = true
        )
    }
}
