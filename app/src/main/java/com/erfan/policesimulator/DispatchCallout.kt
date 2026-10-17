package com.erfan.policesimulator

import java.util.Random

/** The kind of dispatch request sent to the patrol unit. */
internal enum class DispatchType(val title: String) {
    SUSPICIOUS_VEHICLE("Suspicious vehicle"),
    RECKLESS_DRIVER("Reckless driving"),
    SPEEDING_DRIVER("Speeding complaint"),
    STOLEN_VEHICLE("Stolen vehicle")
}

/** How a driver reacts when the patrol activates its emergency equipment nearby. */
internal enum class SuspectResponse {
    COMPLIES,
    FLEES
}

/**
 * A data-only mission profile. Keeping dispatch content and response rules outside the renderer
 * makes the scenario selection deterministic in tests and simple to expand without a game engine.
 */
internal data class DispatchCallout(
    val type: DispatchType,
    val dispatchMessage: String,
    val approachObjective: String,
    val pursuitObjective: String,
    val complianceObjective: String,
    val complianceRadioMessage: String,
    val targetVehicleColor: Int,
    val suspectResponse: SuspectResponse,
    val hasActiveWarrant: Boolean
) {
    val title: String get() = type.title
}

internal object DispatchCalloutFactory {
    private const val RANDOM_WARRANT_SALT = 0x51A7L

    /**
     * Callouts rotate through four distinct profiles. The seed only influences profiles whose
     * records check is variable; the same index and seed always produce the same assignment.
     */
    fun create(callIndex: Int, seed: Long): DispatchCallout {
        require(callIndex >= 0) { "callIndex must not be negative" }

        val type = when (callIndex % 4) {
            0 -> DispatchType.SUSPICIOUS_VEHICLE
            1 -> DispatchType.RECKLESS_DRIVER
            2 -> DispatchType.SPEEDING_DRIVER
            else -> DispatchType.STOLEN_VEHICLE
        }
        val activeWarrant = when (type) {
            DispatchType.SPEEDING_DRIVER -> false
            DispatchType.STOLEN_VEHICLE -> true
            DispatchType.SUSPICIOUS_VEHICLE ->
                Random(seed xor RANDOM_WARRANT_SALT xor callIndex.toLong()).nextBoolean()
            DispatchType.RECKLESS_DRIVER ->
                Random(seed xor (RANDOM_WARRANT_SALT shl 1) xor callIndex.toLong()).nextBoolean()
        }

        return when (type) {
            DispatchType.SUSPICIOUS_VEHICLE -> DispatchCallout(
                type = type,
                dispatchMessage = "Dispatch: suspicious vehicle reported in your patrol sector.",
                approachObjective = "Locate the suspicious vehicle. Use emergency equipment and approach cautiously.",
                pursuitObjective = "Suspicious vehicle fleeing. Follow the route and keep a safe distance.",
                complianceObjective = "Driver has pulled over. Stop nearby and begin the field contact.",
                complianceRadioMessage = "Dispatch: vehicle has acknowledged emergency equipment. Proceed with the stop.",
                targetVehicleColor = 0x9AA5AB,
                suspectResponse = SuspectResponse.FLEES,
                hasActiveWarrant = activeWarrant
            )
            DispatchType.RECKLESS_DRIVER -> DispatchCallout(
                type = type,
                dispatchMessage = "Dispatch: reckless driving reported. Locate the vehicle and conduct a stop.",
                approachObjective = "Locate the reckless driver. Activate emergency equipment and approach safely.",
                pursuitObjective = "Reckless driver fleeing. Follow the route and keep a safe distance.",
                complianceObjective = "Driver has pulled over. Stop nearby and begin the field contact.",
                complianceRadioMessage = "Dispatch: reckless driver has pulled over. Complete the traffic stop.",
                targetVehicleColor = 0xA78A58,
                suspectResponse = SuspectResponse.FLEES,
                hasActiveWarrant = activeWarrant
            )
            DispatchType.SPEEDING_DRIVER -> DispatchCallout(
                type = type,
                dispatchMessage = "Dispatch: speeding complaint reported. Locate the vehicle and conduct a traffic stop.",
                approachObjective = "Locate the reported speeding vehicle. Activate emergency equipment and move into a safe position.",
                pursuitObjective = "The driver is moving away. Keep the vehicle in view and request assistance if needed.",
                complianceObjective = "Driver has complied. Stop nearby to complete the traffic stop.",
                complianceRadioMessage = "Dispatch: driver is complying. Move into position and conduct a safe stop.",
                targetVehicleColor = 0x6C9AA8,
                suspectResponse = SuspectResponse.COMPLIES,
                hasActiveWarrant = false
            )
            DispatchType.STOLEN_VEHICLE -> DispatchCallout(
                type = type,
                dispatchMessage = "Dispatch: plate reader flagged a stolen vehicle. Use caution and request backup.",
                approachObjective = "Locate the flagged stolen vehicle. Activate emergency equipment and prepare for a pursuit.",
                pursuitObjective = "Stolen vehicle fleeing. Follow the route and keep a safe distance.",
                complianceObjective = "Vehicle has stopped. Approach cautiously and begin the field contact.",
                complianceRadioMessage = "Dispatch: stolen vehicle stopped. Requesting backup for the contact.",
                targetVehicleColor = 0x6B4145,
                suspectResponse = SuspectResponse.FLEES,
                hasActiveWarrant = true
            )
        }
    }
}
