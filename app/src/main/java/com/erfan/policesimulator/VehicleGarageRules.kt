package com.erfan.policesimulator

/** Pure repair-shop quote and transaction rules; no Android or renderer dependencies. */
internal data class GarageRepairResult(
    val repaired: Boolean,
    val cost: Int,
    val vehicleCondition: Int,
    val credits: Int,
    val message: String
)

internal object VehicleGarageRules {
    const val MAX_CONDITION = 100
    const val COST_PER_CONDITION_POINT = 5

    fun quote(condition: Int): Int {
        require(condition in 0..MAX_CONDITION) { "Vehicle condition must be 0..100" }
        return (MAX_CONDITION - condition) * COST_PER_CONDITION_POINT
    }

    fun repair(condition: Int, credits: Int): GarageRepairResult {
        require(condition in 0..MAX_CONDITION) { "Vehicle condition must be 0..100" }
        require(credits >= 0) { "Credits must not be negative" }

        val cost = quote(condition)
        if (condition == MAX_CONDITION) {
            return GarageRepairResult(
                repaired = false,
                cost = 0,
                vehicleCondition = condition,
                credits = credits,
                message = "Vehicle is already fully repaired."
            )
        }
        if (credits < cost) {
            return GarageRepairResult(
                repaired = false,
                cost = cost,
                vehicleCondition = condition,
                credits = credits,
                message = "Not enough credits. Repair costs $cost credits."
            )
        }
        return GarageRepairResult(
            repaired = true,
            cost = cost,
            vehicleCondition = MAX_CONDITION,
            credits = credits - cost,
            message = "Repair complete. $cost credits paid."
        )
    }
}
