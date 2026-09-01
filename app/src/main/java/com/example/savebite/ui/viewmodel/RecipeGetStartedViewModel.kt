package com.example.savebite.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.savebite.data.local.RecipeUserPreferences
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class RecipeGetStartedViewModel(application: Application) : AndroidViewModel(application) {

    private val userPreferences = RecipeUserPreferences(application)

    // 饮食偏好选项
    val selectedDiet = MutableStateFlow("None") // "None", "Vegetarian", "Vegan", "Halal"

    // 选中过敏原
    val selectedAllergies = MutableStateFlow<Set<String>>(emptySet())

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

    // 提交偏好并结束引导
    fun completeOnboarding(onFinished: () -> Unit) {
        viewModelScope.launch {
            userPreferences.saveUserPreferences(
                diet = selectedDiet.value,
                allergySet = selectedAllergies.value
            )
            onFinished()
        }
    }
}