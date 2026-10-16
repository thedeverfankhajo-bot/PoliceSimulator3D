package com.erfan.policesimulator

/**
 * Pure mission rules so the outcomes can be verified without an Android device or OpenGL context.
 */
internal enum class IncidentAction {
    ARREST,
    CITE_OR_WARN
}

internal data class IncidentOutcome(
    val scoreDelta: Int,
    val correct: Boolean,
    val message: String,
    val dispatchMessage: String
)

internal object IncidentRules {
    fun resolve(hasActiveWarrant: Boolean, action: IncidentAction): IncidentOutcome = when {
        hasActiveWarrant && action == IncidentAction.ARREST -> IncidentOutcome(
            scoreDelta = 250,
            correct = true,
            message = "Lawful arrest completed. +250 XP.",
            dispatchMessage = "Dispatch: arrest completed. Unit 12 available."
        )
        hasActiveWarrant -> IncidentOutcome(
            scoreDelta = -60,
            correct = false,
            message = "Active warrant missed. Supervisor review requested.",
            dispatchMessage = "Dispatch: possible missed warrant reported for review."
        )
        action == IncidentAction.CITE_OR_WARN -> IncidentOutcome(
            scoreDelta = 120,
            correct = true,
            message = "Citation / warning completed. +120 XP.",
            dispatchMessage = "Dispatch: traffic incident cleared. Unit 12 available."
        )
        else -> IncidentOutcome(
            scoreDelta = -100,
            correct = false,
            message = "No active warrant. Unjustified arrest flagged for review.",
            dispatchMessage = "Dispatch: use-of-authority review requested."
        )
    }
}
