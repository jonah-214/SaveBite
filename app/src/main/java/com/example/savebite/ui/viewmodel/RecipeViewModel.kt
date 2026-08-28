package com.example.savebite.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.savebite.data.ai.GeminiRecipeService
import com.example.savebite.data.ai.Recipe
import com.example.savebite.model.Inventory
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class RecipeUiState(
    val expiringItems: List<Inventory> = emptyList(),
    val recipes: List<Recipe> = emptyList(),
    val isLoading: Boolean = false,
    val errorMessage: String? = null
)

class RecipeViewModel(
    private val aiService: GeminiRecipeService
) : ViewModel() {

    private val _uiState = MutableStateFlow(RecipeUiState())
    val uiState: StateFlow<RecipeUiState> = _uiState.asStateFlow()

    private var cachedInventory: List<Inventory> = emptyList()

    fun fetchAIRecipes(allInventoryItems: List<Inventory>) {
        cachedInventory = allInventoryItems
        val urgentItems = allInventoryItems.filter { !it.isConsumed && it.daysLeft <= 3 }

        if (urgentItems.isEmpty()) {
            _uiState.value = RecipeUiState(
                expiringItems = emptyList(),
                recipes = emptyList(),
                isLoading = false
            )
            return
        }

        if (_uiState.value.recipes.isNotEmpty() && _uiState.value.expiringItems == urgentItems) {
            return
        }

        executeFetch(urgentItems)
    }

    fun retryFetch() {
        val urgentItems = cachedInventory.filter { !it.isConsumed && it.daysLeft <= 3 }
        if (urgentItems.isNotEmpty()) {
            executeFetch(urgentItems)
        }
    }

    private fun executeFetch(urgentItems: List<Inventory>) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                expiringItems = urgentItems,
                isLoading = true,
                errorMessage = null
            )

            val generatedRecipes = aiService.generateRecipes(urgentItems)

            _uiState.value = _uiState.value.copy(
                recipes = generatedRecipes,
                isLoading = false,
                errorMessage = if (generatedRecipes.isEmpty()) "Failed to fetch recipes. Check API key or connection." else null
            )
        }
    }
}