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
internal class CareerProfileStore(context: Context) {
    private val dataStore: DataStore<Preferences> = context.careerDataStore
    private val ioScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private val mainHandler = Handler(Looper.getMainLooper())

    fun load(
        onLoaded: (CareerProgress) -> Unit,
        onError: () -> Unit
    ) {
        ioScope.launch {
            try {
                val saved = dataStore.data.first().toCareerProgress()
                mainHandler.post { onLoaded(saved) }
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

    private companion object {
        val XP_KEY = intPreferencesKey("career_xp")
        val CALLS_KEY = intPreferencesKey("calls_completed")
        val CORRECT_KEY = intPreferencesKey("correct_decisions")
        val INCORRECT_KEY = intPreferencesKey("incorrect_decisions")
    }
}
