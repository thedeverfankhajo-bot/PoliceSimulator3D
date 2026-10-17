package com.erfan.policesimulator

/** Shared timing and intensity rules keep the lightbar and its ground spill in sync. */
internal data class EmergencyLightState(
    val redActive: Boolean,
    val blueActive: Boolean,
    val reflectionIntensity: Float
)

internal object EmergencyLightRules {
    private const val FLASH_INTERVAL_MS = 230L

    fun state(elapsedMillis: Long, enabled: Boolean, daylight: Float): EmergencyLightState {
        require(elapsedMillis >= 0L) { "Elapsed time must not be negative" }
        require(daylight.isFinite() && daylight in 0f..1f) { "Daylight must be between 0 and 1" }
        if (!enabled) return EmergencyLightState(false, false, 0f)
        val redPhase = (elapsedMillis / FLASH_INTERVAL_MS) % 2L == 0L
        val intensity = (0.16f + (1f - daylight) * 0.55f).coerceIn(0f, 0.72f)
        return EmergencyLightState(
            redActive = redPhase,
            blueActive = !redPhase,
            reflectionIntensity = intensity
        )
    }
}
