package com.example.savebite.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.savebite.data.ai.Recipe
import com.example.savebite.data.repo.RecipeRepository
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
    private val repository: RecipeRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(RecipeUiState())
    val uiState: StateFlow<RecipeUiState> = _uiState.asStateFlow()

    private var cachedInventory: List<Inventory> = emptyList()

    init {
        // ViewModel 初始化时立即监听 Room 缓存数据
        viewModelScope.launch {
            repository.cachedRecipes.collect { localRecipes ->
                if (localRecipes.isNotEmpty() && _uiState.value.recipes.isEmpty()) {
                    _uiState.value = _uiState.value.copy(recipes = localRecipes)
                }
            }
        }
    }

    fun fetchAIRecipes(allInventoryItems: List<Inventory>) {
        cachedInventory = allInventoryItems
        val urgentItems = allInventoryItems.filter { !it.isConsumed && it.daysLeft <= 3 }

        _uiState.value = _uiState.value.copy(expiringItems = urgentItems)

        // 如果已有数据且过期食材没变，不自动刷新
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

            try {
                // 调用 repository，成功后会自动存入 Room
                val fetchedRecipes = repository.fetchAndSaveRecipes(urgentItems)

                _uiState.value = _uiState.value.copy(
                    recipes = fetchedRecipes,
                    isLoading = false,
                    errorMessage = if (fetchedRecipes.isEmpty()) "No recipes found. Try again later." else null
                )
            } catch (e: Exception) {
                val msg = e.message ?: ""
                val userMessage = when {
                    msg.contains("MISSING_API_KEY") -> "Gemini API Key is missing in local.properties."
                    msg.contains("INVALID_API_KEY") -> "The API Key is invalid or not authorized."
                    msg.contains("NETWORK_ERROR") -> "Cannot reach Gemini. Showing cached data if available."
                    msg.contains("RATE_LIMIT_EXCEEDED") -> "Too many requests. Please wait a moment."
                    msg.contains("SERVER_ERROR") -> "Google server error. Please try again later."
                    else -> "Error: ${e.localizedMessage ?: "Unknown error"}"
                }

                // 即使请求失败，依然保留已有的缓存 Recipe 页面显示
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = if (_uiState.value.recipes.isEmpty()) userMessage else null
                )
            }
        }
    }
}