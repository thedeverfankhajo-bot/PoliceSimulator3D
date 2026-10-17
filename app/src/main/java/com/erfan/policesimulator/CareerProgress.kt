package com.erfan.policesimulator

/**
 * Small, serializable career summary. Pure Kotlin keeps progression rules easy to unit-test.
 */
internal data class CareerProgress(
    val xp: Int = 0,
    val callsCompleted: Int = 0,
    val correctDecisions: Int = 0,
    val incorrectDecisions: Int = 0
) {
    fun recordDecision(outcome: IncidentOutcome): CareerProgress {
        val updatedXp = (xp.toLong() + outcome.scoreDelta.toLong())
            .coerceIn(0L, Int.MAX_VALUE.toLong())
            .toInt()

        return copy(
            xp = updatedXp,
            callsCompleted = callsCompleted.incrementSafely(),
            correctDecisions = if (outcome.correct) correctDecisions.incrementSafely() else correctDecisions,
            incorrectDecisions = if (outcome.correct) incorrectDecisions else incorrectDecisions.incrementSafely()
        )
    }

    private fun Int.incrementSafely(): Int =
        if (this == Int.MAX_VALUE) this else this + 1
}
