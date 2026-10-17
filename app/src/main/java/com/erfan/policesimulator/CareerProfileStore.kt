package com.erfan.policesimulator

import android.content.Context
import android.os.Handler
import android.os.Looper
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.io.IOException

private val Context.careerDataStore by preferencesDataStore(name = "police_career")

/**
 * Persists the small career profile with DataStore. All disk work runs off the UI and GL threads.
 */
internal data class SavedPoliceProfile(val career: CareerProgress, val vehicleCondition: Int, val credits: Int)

internal class CareerProfileStore(context: Context) {
    private val dataStore: DataStore<Preferences> = context.careerDataStore
    private val ioScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private val mainHandler = Handler(Looper.getMainLooper())

    fun load(
        onLoaded: (CareerProgress) -> Unit,
        onError: () -> Unit
    ) = loadProfile(
        onLoaded = { onLoaded(it.career) },
        onError = onError
    )

    fun loadProfile(
        onLoaded: (SavedPoliceProfile) -> Unit,
        onError: () -> Unit
    ) {
        ioScope.launch {
            try {
                val saved = dataStore.data.first().toSavedPoliceProfile()
                mainHandler.post { onLoaded(saved) }
            } catch (_: IOException) {
                mainHandler.post(onError)
            }
        }
    }

    fun saveVehicle(
        condition: Int,
        credits: Int,
        onSaved: (SavedPoliceProfile) -> Unit,
        onError: () -> Unit
    ) {
        require(condition in 0..VehicleDamageRules.MAX_CONDITION)
        require(credits >= 0)
        ioScope.launch {
            try {
                val saved = dataStore.edit { preferences ->
                    preferences[VEHICLE_CONDITION_KEY] = condition
                    preferences[CREDITS_KEY] = credits
                }.toSavedPoliceProfile()
                mainHandler.post { onSaved(saved) }
            } catch (_: IOException) {
                mainHandler.post(onError)
            }
        }
    }

    fun repairVehicle(
        onSaved: (SavedPoliceProfile, GarageRepairResult) -> Unit,
        onError: () -> Unit
    ) {
        ioScope.launch {
            try {
                var result: GarageRepairResult? = null
                val saved = dataStore.edit { preferences ->
                    val condition = preferences[VEHICLE_CONDITION_KEY] ?: VehicleDamageRules.MAX_CONDITION
                    val credits = preferences[CREDITS_KEY] ?: DEFAULT_CREDITS
                    result = VehicleGarageRules.repair(condition, credits)
                    if (result!!.repaired) {
                        preferences[VEHICLE_CONDITION_KEY] = result!!.vehicleCondition
                        preferences[CREDITS_KEY] = result!!.credits
                    }
                }.toSavedPoliceProfile()
                mainHandler.post { onSaved(saved, result!!) }
            } catch (_: IOException) {
                mainHandler.post(onError)
            }
        }
    }

    fun recordDecision(
        outcome: IncidentOutcome,
        onSaved: (CareerProgress) -> Unit,
        onError: () -> Unit
    ) {
        ioScope.launch {
            try {
                val updatedPreferences = dataStore.edit { preferences ->
                    val updated = preferences.toCareerProgress().recordDecision(outcome)
                    preferences[XP_KEY] = updated.xp
                    preferences[CALLS_KEY] = updated.callsCompleted
                    preferences[CORRECT_KEY] = updated.correctDecisions
                    preferences[INCORRECT_KEY] = updated.incorrectDecisions
                }
                val saved = updatedPreferences.toCareerProgress()
                mainHandler.post { onSaved(saved) }
            } catch (_: IOException) {
                mainHandler.post(onError)
            }
        }
    }

    private fun Preferences.toCareerProgress() = CareerProgress(
        xp = this[XP_KEY] ?: 0,
        callsCompleted = this[CALLS_KEY] ?: 0,
        correctDecisions = this[CORRECT_KEY] ?: 0,
        incorrectDecisions = this[INCORRECT_KEY] ?: 0
    )

    private fun Preferences.toSavedPoliceProfile() = SavedPoliceProfile(
        career = toCareerProgress(),
        vehicleCondition = this[VEHICLE_CONDITION_KEY] ?: VehicleDamageRules.MAX_CONDITION,
        credits = this[CREDITS_KEY] ?: DEFAULT_CREDITS
    )

    private companion object {
        const val DEFAULT_CREDITS = 1_000
        val VEHICLE_CONDITION_KEY = intPreferencesKey("vehicle_condition")
        val CREDITS_KEY = intPreferencesKey("garage_credits")
        val XP_KEY = intPreferencesKey("career_xp")
        val CALLS_KEY = intPreferencesKey("calls_completed")
        val CORRECT_KEY = intPreferencesKey("correct_decisions")
        val INCORRECT_KEY = intPreferencesKey("incorrect_decisions")
    }
}
