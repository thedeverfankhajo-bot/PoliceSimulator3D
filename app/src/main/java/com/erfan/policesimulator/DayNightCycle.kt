package com.erfan.policesimulator

import kotlin.math.floor

internal enum class DayPhase(val displayName: String) {
    DAWN("DAWN"),
    DAY("DAY"),
    DUSK("DUSK"),
    NIGHT("NIGHT")
}

internal data class WorldLighting(
    val timeSeconds: Float,
    val clockText: String,
    val phase: DayPhase,
    /** 0 is night; 1 is full daylight. */
    val daylight: Float,
    val skyRed: Float,
    val skyGreen: Float,
    val skyBlue: Float,
    val streetLightsOn: Boolean
) {
    val sceneBrightness: Float get() = 0.34f + daylight * 0.66f
}

/**
 * Deterministic day/night cycle. One 16-minute real-time session represents one 24-hour game day.
 * Kept pure Kotlin so clock wrap, sunrise, sunset, and lamp state can be tested on the JVM.
 */
internal object DayNightCycle {
    const val GAME_DAY_SECONDS = 86_400f
    const val REAL_SECONDS_PER_GAME_DAY = 960f
    const val GAME_SECONDS_PER_REAL_SECOND = GAME_DAY_SECONDS / REAL_SECONDS_PER_GAME_DAY

    private val nightSky = floatArrayOf(0.025f, 0.045f, 0.105f)
    private val dawnSky = floatArrayOf(0.82f, 0.33f, 0.20f)
    private val daySky = floatArrayOf(0.43f, 0.66f, 0.79f)
    private val duskSky = floatArrayOf(0.78f, 0.28f, 0.20f)

    fun advance(currentSeconds: Float, realDeltaSeconds: Float): Float {
        require(currentSeconds.isFinite()) { "World time must be finite" }
        require(realDeltaSeconds.isFinite() && realDeltaSeconds >= 0f) {
            "Delta time must be finite and non-negative"
        }
        val advanced = currentSeconds + realDeltaSeconds * GAME_SECONDS_PER_REAL_SECOND
        return ((advanced % GAME_DAY_SECONDS) + GAME_DAY_SECONDS) % GAME_DAY_SECONDS
    }

    fun at(timeSeconds: Float): WorldLighting {
        require(timeSeconds.isFinite()) { "World time must be finite" }
        val normalized = ((timeSeconds % GAME_DAY_SECONDS) + GAME_DAY_SECONDS) % GAME_DAY_SECONDS
        val hour = normalized / 3_600f
        val minuteOfDay = floor(normalized / 60f).toInt().coerceIn(0, 1439)
        val clock = "%02d:%02d".format(minuteOfDay / 60, minuteOfDay % 60)

        val phase = when {
            hour >= 5f && hour < 7f -> DayPhase.DAWN
            hour >= 7f && hour < 17f -> DayPhase.DAY
            hour >= 17f && hour < 19f -> DayPhase.DUSK
            else -> DayPhase.NIGHT
        }
        val daylight = when {
            hour < 5f || hour >= 19f -> 0f
            hour < 7f -> smoothstep((hour - 5f) / 2f)
            hour < 17f -> 1f
            else -> 1f - smoothstep((hour - 17f) / 2f)
        }.coerceIn(0f, 1f)

        val sky = when {
            hour < 5f || hour >= 19f -> nightSky
            hour < 6f -> lerp(nightSky, dawnSky, hour - 5f)
            hour < 7f -> lerp(dawnSky, daySky, hour - 6f)
            hour < 17f -> daySky
            hour < 18f -> lerp(daySky, duskSky, hour - 17f)
            hour < 19f -> lerp(duskSky, nightSky, hour - 18f)
            else -> nightSky
        }
        return WorldLighting(
            timeSeconds = normalized,
            clockText = clock,
            phase = phase,
            daylight = daylight,
            skyRed = sky[0],
            skyGreen = sky[1],
            skyBlue = sky[2],
            streetLightsOn = hour < 6f || hour >= 18f
        )
    }

    private fun smoothstep(value: Float): Float {
        val t = value.coerceIn(0f, 1f)
        return t * t * (3f - 2f * t)
    }

    private fun lerp(from: FloatArray, to: FloatArray, amount: Float): FloatArray {
        val t = amount.coerceIn(0f, 1f)
        return floatArrayOf(
            from[0] + (to[0] - from[0]) * t,
            from[1] + (to[1] - from[1]) * t,
            from[2] + (to[2] - from[2]) * t
        )
    }
}
