package com.erfan.policesimulator

/**
 * Lightweight damage model for the patrol vehicle.
 *
 * It deliberately stays independent from Android and OpenGL so impact severity and handling
 * limits can be verified with local JVM unit tests.
 */
internal object VehicleDamageRules {
    const val MAX_CONDITION = 100

    fun applyImpact(
        currentCondition: Int,
        speed: Float,
        trafficImpact: Boolean
    ): Int {
        require(currentCondition in 0..MAX_CONDITION) { "Vehicle condition must be 0..100" }
        require(speed.isFinite() && speed >= 0f) { "Impact speed must be finite and non-negative" }

        val baseDamage = when {
            speed >= 22f -> 22
            speed >= 12f -> 15
            speed >= 5f -> 9
            else -> 4
        }
        val damage = baseDamage + if (trafficImpact) 4 else 0
        return (currentCondition - damage).coerceAtLeast(0)
    }

    /** Maximum forward speed in world units per second at the given condition. */
    fun maximumSpeed(condition: Int): Float {
        require(condition in 0..MAX_CONDITION) { "Vehicle condition must be 0..100" }
        return when {
            condition >= 75 -> 30f
            condition >= 50 -> 24f
            condition >= 25 -> 17f
            else -> 10f
        }
    }

    /** Reduced acceleration represents a damaged drivetrain without making the car unusable. */
    fun accelerationMultiplier(condition: Int): Float {
        require(condition in 0..MAX_CONDITION) { "Vehicle condition must be 0..100" }
        return when {
            condition >= 75 -> 1f
            condition >= 50 -> 0.88f
            condition >= 25 -> 0.72f
            else -> 0.55f
        }
    }
}
