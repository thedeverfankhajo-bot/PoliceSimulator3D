package com.erfan.policesimulator

/**
 * Small, deterministic traffic-response rules.
 *
 * Nearby traffic eases toward the shoulder and slows down only while the patrol car is
 * using emergency lights and siren. Once the patrol has passed or deactivates them, cars
 * gradually return to their original lane and cruising speed.
 */
internal object TrafficBehavior {
    const val YIELD_RADIUS = 28f
    const val SHOULDER_OFFSET = 1.4f
    const val YIELD_SPEED_FACTOR = 0.35f

    fun shouldYield(
        emergencyActive: Boolean,
        distance: Float,
        yieldRadius: Float = YIELD_RADIUS
    ): Boolean {
        require(distance.isFinite() && distance >= 0f)
        require(yieldRadius.isFinite() && yieldRadius > 0f)
        return emergencyActive && distance <= yieldRadius
    }

    fun approachShoulderOffset(
        currentOffset: Float,
        lanePosition: Float,
        roadCenter: Float,
        yielding: Boolean,
        deltaSeconds: Float,
        shoulderDistance: Float = SHOULDER_OFFSET,
        response: Float = 3f
    ): Float {
        require(currentOffset.isFinite())
        require(lanePosition.isFinite() && roadCenter.isFinite())
        require(deltaSeconds.isFinite() && deltaSeconds >= 0f)
        require(shoulderDistance.isFinite() && shoulderDistance > 0f)
        require(response.isFinite() && response > 0f)

        val laneSide = if (lanePosition >= roadCenter) 1f else -1f
        val target = if (yielding) laneSide * shoulderDistance else 0f
        val alpha = (deltaSeconds * response).coerceIn(0f, 1f)
        return currentOffset + (target - currentOffset) * alpha
    }

    fun approachSpeedFactor(
        currentFactor: Float,
        yielding: Boolean,
        deltaSeconds: Float,
        normalFactor: Float = 1f,
        yieldingFactor: Float = YIELD_SPEED_FACTOR,
        response: Float = 3f
    ): Float {
        require(currentFactor.isFinite() && currentFactor >= 0f)
        require(deltaSeconds.isFinite() && deltaSeconds >= 0f)
        require(normalFactor.isFinite() && normalFactor > 0f)
        require(yieldingFactor.isFinite() && yieldingFactor >= 0f && yieldingFactor <= normalFactor)
        require(response.isFinite() && response > 0f)

        val target = if (yielding) yieldingFactor else normalFactor
        val alpha = (deltaSeconds * response).coerceIn(0f, 1f)
        return currentFactor + (target - currentFactor) * alpha
    }
}
