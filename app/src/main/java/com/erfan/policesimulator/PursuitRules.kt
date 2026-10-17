package com.erfan.policesimulator

import kotlin.math.abs

internal enum class PursuitState { SUSPICIOUS, FLEEING, STOPPED }

internal data class PursuitUpdate(
    val state: PursuitState,
    val targetSpeedFactor: Float,
    val shouldTurnAtJunction: Boolean,
    val escaped: Boolean
)

/**
 * Deterministic suspect AI rules, kept independent of Android/OpenGL for JVM tests.
 */
internal object PursuitRules {
    const val START_FLEE_DISTANCE = 24f
    const val STOP_DISTANCE = 5f
    const val STOP_PATROL_SPEED = 1.5f
    const val ESCAPE_DISTANCE = 165f
    const val FLEE_SPEED_FACTOR = 1.65f
    const val SUSPICIOUS_SPEED_FACTOR = 0.65f

    fun update(
        state: PursuitState,
        patrolDistance: Float,
        emergencyActive: Boolean,
        patrolSpeed: Float,
        deltaSeconds: Float
    ): PursuitUpdate {
        require(patrolDistance.isFinite() && patrolDistance >= 0f)
        require(patrolSpeed.isFinite() && patrolSpeed >= 0f)
        require(deltaSeconds.isFinite() && deltaSeconds >= 0f)

        if (patrolDistance >= ESCAPE_DISTANCE) {
            return PursuitUpdate(PursuitState.FLEEING, FLEE_SPEED_FACTOR, true, true)
        }
        if (state == PursuitState.STOPPED) {
            return PursuitUpdate(PursuitState.STOPPED, 0f, false, false)
        }
        if (!emergencyActive && patrolDistance <= STOP_DISTANCE && patrolSpeed <= STOP_PATROL_SPEED) {
            return PursuitUpdate(PursuitState.STOPPED, 0f, false, false)
        }
        if (emergencyActive && patrolDistance <= START_FLEE_DISTANCE) {
            return PursuitUpdate(PursuitState.FLEEING, FLEE_SPEED_FACTOR, true, false)
        }
        if (state == PursuitState.FLEEING) {
            return PursuitUpdate(PursuitState.FLEEING, FLEE_SPEED_FACTOR, true, false)
        }
        return PursuitUpdate(PursuitState.SUSPICIOUS, SUSPICIOUS_SPEED_FACTOR, false, false)
    }

    fun approachSpeedFactor(
        current: Float,
        target: Float,
        deltaSeconds: Float,
        response: Float = 1.8f
    ): Float {
        require(current.isFinite() && current >= 0f)
        require(target.isFinite() && target >= 0f)
        require(deltaSeconds.isFinite() && deltaSeconds >= 0f)
        require(response.isFinite() && response > 0f)
        val alpha = (deltaSeconds * response).coerceIn(0f, 1f)
        return current + (target - current) * alpha
    }

    /** Choose the street axis that maximizes distance from the patrol car. */
    fun chooseVerticalRoad(suspectX: Float, suspectZ: Float, patrolX: Float, patrolZ: Float): Boolean {
        require(listOf(suspectX, suspectZ, patrolX, patrolZ).all { it.isFinite() })
        val verticalOption = abs(suspectX - patrolX) + abs((suspectZ + 40f) - patrolZ)
        val horizontalOption = abs((suspectX + 40f) - patrolX) + abs(suspectZ - patrolZ)
        return verticalOption >= horizontalOption
    }
}
