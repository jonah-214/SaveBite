package com.example.savebite.data.local

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.core.stringSetPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore by preferencesDataStore(name = "user_preferences")

class RecipeUserPreferences(private val context: Context) {

    companion object {
        val IS_FIRST_RUN = booleanPreferencesKey("is_first_run")
        val DIET_TYPE = stringPreferencesKey("diet_type") // 例如：None, Vegetarian, Vegan
        val ALLERGIES = stringSetPreferencesKey("allergies") // 例如：["Peanuts", "Seafood"]
        val HOUSEHOLD_TYPE = stringPreferencesKey("household_type") // Student, Adult, Family
    }

    // 判断是否是第一次进入 APP
    val isRecipeFirstRun: Flow<Boolean> = context.dataStore.data.map { preferences ->
        preferences[IS_FIRST_RUN] ?: true
    }

    // 获取用户偏好设置
    val dietType: Flow<String> = context.dataStore.data.map { preferences ->
        preferences[DIET_TYPE] ?: "None"
    }

    val allergies: Flow<Set<String>> = context.dataStore.data.map { preferences ->
        preferences[ALLERGIES] ?: emptySet()
    }

    // Defaults to "Student" since that's SaveBite's primary audience
    val householdType: Flow<String> = context.dataStore.data.map { preferences ->
        preferences[HOUSEHOLD_TYPE] ?: "Student"
    }

    // 保存设置并标记完成引导
    suspend fun saveUserPreferences(diet: String, allergySet: Set<String>, household: String) {
        context.dataStore.edit { preferences ->
            preferences[DIET_TYPE] = diet
            preferences[ALLERGIES] = allergySet
            preferences[HOUSEHOLD_TYPE] = household
            preferences[IS_FIRST_RUN] = false // 标记以后不再弹出引导页
        }
    }

    // 更新首次运行标记
    suspend fun setRecipeFirstRun(isFirstRun: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[IS_FIRST_RUN] = isFirstRun
        }
    }
}
