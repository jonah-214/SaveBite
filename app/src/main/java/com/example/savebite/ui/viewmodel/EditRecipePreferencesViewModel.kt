package com.example.savebite.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.savebite.data.local.RecipeUserPreferences
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

// Like RecipeGetStartedViewModel, but loads the user's already-saved preferences as the
// starting state instead of defaulting to "None"/empty — this is for editing them later
// from Profile & Settings, not the first-run onboarding flow.
class EditRecipePreferencesViewModel(application: Application) : AndroidViewModel(application) {

    private val userPreferences = RecipeUserPreferences(application)

    val selectedDiet = MutableStateFlow("None")
    val selectedAllergies = MutableStateFlow<Set<String>>(emptySet())
    val selectedHousehold = MutableStateFlow("Student")

    // The screen shows a loading spinner until this flips true, so the FilterChips don't
    // flash from their default values to the saved ones a frame later.
    val isLoaded = MutableStateFlow(false)

    init {
        viewModelScope.launch {
            selectedDiet.value = userPreferences.dietType.first()
            selectedAllergies.value = userPreferences.allergies.first()
            selectedHousehold.value = userPreferences.householdType.first()
            isLoaded.value = true
        }
    }

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

    fun savePreferences(onSaved: () -> Unit) {
        viewModelScope.launch {
            userPreferences.saveUserPreferences(
                diet = selectedDiet.value,
                allergySet = selectedAllergies.value,
                household = selectedHousehold.value
            )
            onSaved()
        }
    }
}
