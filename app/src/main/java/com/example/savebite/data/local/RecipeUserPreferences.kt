package com.example.savebite.data.local

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.core.stringSetPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import java.io.IOException

private val Context.dataStore by preferencesDataStore(name = "user_preferences")

class RecipeUserPreferences(private val context: Context) {

    companion object {
        val IS_FIRST_RUN = booleanPreferencesKey("is_first_run")
        val DIET_TYPE = stringPreferencesKey("diet_type")
        val ALLERGIES = stringSetPreferencesKey("allergies")
        val HOUSEHOLD_TYPE = stringPreferencesKey("household_type")
    }

    private val safeData = context.dataStore.data
        .catch { exception ->
            if (exception is IOException) emit(emptyPreferences()) else throw exception
        }

    val isRecipeFirstRun: Flow<Boolean> = safeData.map { preferences ->
        preferences[IS_FIRST_RUN] ?: true
    }.distinctUntilChanged()

    val dietType: Flow<String> = safeData.map { preferences ->
        preferences[DIET_TYPE] ?: "None"
    }.distinctUntilChanged()

    val allergies: Flow<Set<String>> = safeData.map { preferences ->
        preferences[ALLERGIES] ?: emptySet()
    }.distinctUntilChanged()

    val householdType: Flow<String> = safeData.map { preferences ->
        preferences[HOUSEHOLD_TYPE] ?: "Student"
    }.distinctUntilChanged()

    suspend fun saveUserPreferences(diet: String, allergySet: Set<String>, household: String) {
        context.dataStore.edit { preferences ->
            preferences[DIET_TYPE] = diet
            preferences[ALLERGIES] = allergySet
            preferences[HOUSEHOLD_TYPE] = household
            preferences[IS_FIRST_RUN] = false
        }
    }

    suspend fun setRecipeFirstRun(isFirstRun: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[IS_FIRST_RUN] = isFirstRun
        }
    }
}