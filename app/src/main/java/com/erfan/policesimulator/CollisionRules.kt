package com.erfan.policesimulator

import kotlin.math.abs
import kotlin.math.ceil

internal data class CollisionRect(
    val centerX: Float,
    val centerZ: Float,
    val halfWidth: Float,
    val halfDepth: Float
) {
    init {
        require(halfWidth >= 0f && halfDepth >= 0f)
    }
}

internal data class MovementResult(
    val x: Float,
    val z: Float,
    val collidedX: Boolean,
    val collidedZ: Boolean
) {
    val collided: Boolean get() = collidedX || collidedZ
}

/** Deterministic X/Z collision solver; independent of Android and OpenGL. */
internal object WorldCollision {
    fun move(
        x: Float,
        z: Float,
        deltaX: Float,
        deltaZ: Float,
        radius: Float,
        obstacles: List<CollisionRect>,
        worldLimit: Float = 155f,
        maxStep: Float = 0.5f
    ): MovementResult {
        require(radius > 0f && radius.isFinite())
        require(worldLimit > radius && worldLimit.isFinite())
        require(maxStep > 0f && maxStep.isFinite())
        require(x.isFinite() && z.isFinite() && deltaX.isFinite() && deltaZ.isFinite())

        val steps = ceil(maxOf(abs(deltaX), abs(deltaZ)) / maxStep).toInt().coerceAtLeast(1)
        val stepX = deltaX / steps
        val stepZ = deltaZ / steps
        var px = x.coerceIn(-worldLimit + radius, worldLimit - radius)
        var pz = z.coerceIn(-worldLimit + radius, worldLimit - radius)
        var hitX = px != x
        var hitZ = pz != z

        repeat(steps) {
            // Check the horizontal move at both the current and upcoming Z positions.
            // This prevents diagonal movement from slipping through a building corner.
            val nextZ = pz + stepZ
            if (stepX != 0f) {
                val desiredX = px + stepX
                val candidate = desiredX.coerceIn(-worldLimit + radius, worldLimit - radius)
                if (
                    candidate != desiredX ||
                    blocked(candidate, pz, radius, obstacles) ||
                    blocked(candidate, nextZ, radius, obstacles)
                ) {
                    hitX = true
                } else {
                    px = candidate
                }
            }
            if (stepZ != 0f) {
                val desiredZ = pz + stepZ
                val candidate = desiredZ.coerceIn(-worldLimit + radius, worldLimit - radius)
                if (candidate != desiredZ || blocked(px, candidate, radius, obstacles)) {
                    hitZ = true
                } else {
                    pz = candidate
                }
            }
        }
        return MovementResult(px, pz, hitX, hitZ)
    }

    private fun blocked(x: Float, z: Float, radius: Float, obstacles: List<CollisionRect>) =
        obstacles.any { rect ->
            val nearestX = x.coerceIn(rect.centerX - rect.halfWidth, rect.centerX + rect.halfWidth)
            val nearestZ = z.coerceIn(rect.centerZ - rect.halfDepth, rect.centerZ + rect.halfDepth)
            val dx = x - nearestX
            val dz = z - nearestZ
            dx * dx + dz * dz < radius * radius
        }
}
