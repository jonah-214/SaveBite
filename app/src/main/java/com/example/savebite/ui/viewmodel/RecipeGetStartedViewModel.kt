package com.example.savebite.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.savebite.data.local.RecipeUserPreferences
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class RecipeGetStartedViewModel(application: Application) : AndroidViewModel(application) {

    // Persistent storage manager for user recipe preferences
    private val userPreferences = RecipeUserPreferences(application)

    // Dietary preference selection
    val selectedDiet = MutableStateFlow("None") // "None", "Vegetarian", "Vegan", "Halal"

    // Set of selected allergen exclusions
    val selectedAllergies = MutableStateFlow<Set<String>>(emptySet())

    // Household type selection
    val selectedHousehold = MutableStateFlow("Student")

    fun selectDiet(diet: String) {
        selectedDiet.value = diet
    }

    fun toggleAllergy(allergy: String) {
        val current = selectedAllergies.value.toMutableSet()
        if (current.contains(allergy)) {
            current.remove(allergy)
        } else {
            current.add(allergy)
        }
        selectedAllergies.value = current
    }

    fun selectHousehold(household: String) {
        selectedHousehold.value = household
    }

    // Persists the selected preferences to DataStore and marks onboarding as complete.
    fun completeOnboarding(onFinished: () -> Unit) {
        viewModelScope.launch {
            userPreferences.saveUserPreferences(
                diet = selectedDiet.value,
                allergySet = selectedAllergies.value,
                household = selectedHousehold.value
            )
            onFinished()
        }
    }
}
