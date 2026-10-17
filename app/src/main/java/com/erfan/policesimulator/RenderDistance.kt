package com.erfan.policesimulator

import kotlin.math.abs

/**
 * Lightweight X/Z distance visibility check for the low-poly scene.
 * The object's complete rectangular footprint is considered so large ground/road planes
 * are not incorrectly removed just because their center is far from the player.
 */
internal object RenderDistance {
    fun intersectsCircle(
        playerX: Float,
        playerZ: Float,
        centerX: Float,
        centerZ: Float,
        halfWidth: Float,
        halfDepth: Float,
        viewDistance: Float
    ): Boolean {
        require(playerX.isFinite() && playerZ.isFinite())
        require(centerX.isFinite() && centerZ.isFinite())
        require(halfWidth.isFinite() && halfWidth >= 0f)
        require(halfDepth.isFinite() && halfDepth >= 0f)
        require(viewDistance.isFinite() && viewDistance > 0f)

        val dx = (abs(playerX - centerX) - halfWidth).coerceAtLeast(0f)
        val dz = (abs(playerZ - centerZ) - halfDepth).coerceAtLeast(0f)
        return dx * dx + dz * dz <= viewDistance * viewDistance
    }
}
