package com.example.savebite.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.savebite.data.ai.Recipe
import com.example.savebite.data.repo.RecipeRepository
import com.example.savebite.model.Inventory
import com.example.savebite.utils.SessionManager
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

data class RecipeUiState(
    val expiringItems: List<Inventory> = emptyList(),
    val recipes: List<Recipe> = emptyList(),
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val searchQuery: String = ""
)

@OptIn(ExperimentalCoroutinesApi::class)
class RecipeViewModel(
    private val repository: RecipeRepository,
    private val sessionManager: SessionManager
) : ViewModel() {

    companion object {
        private const val NO_USER = -1
    }

    private val _uiState = MutableStateFlow(RecipeUiState())
    val uiState: StateFlow<RecipeUiState> = _uiState.asStateFlow()

    private var cachedInventory: List<Inventory> = emptyList()
    private var cachedDietType: String = "None"
    private var cachedAllergies: Set<String> = emptySet()
    private var cachedHouseholdType: String = "Student"

    private var fetchJob: Job? = null

    init {
        viewModelScope.launch {
            sessionManager.userIdFlow.flatMapLatest { userId ->
                if (userId == NO_USER) flowOf(emptyList()) else repository.cachedRecipes(userId)
            }.collect { localRecipes ->
                if (localRecipes.isNotEmpty()) {
                    _uiState.update {
                        it.copy(
                            recipes = localRecipes,
                            errorMessage = if (it.recipes.isEmpty()) null else it.errorMessage
                        )
                    }
                }
            }
        }
    }

    fun onSearchQueryChanged(newQuery: String) {
        _uiState.update { it.copy(searchQuery = newQuery) }
    }

    fun fetchAIRecipes(
        allInventoryItems: List<Inventory>,
        dietType: String = "None",
        allergies: Set<String> = emptySet(),
        householdType: String = "Student"
    ) {
        // Filter active inventory for items expiring in 3 days or fewer
        val urgentItems = allInventoryItems.filter { !it.isConsumed && it.daysLeft <= 3 }

        val itemsChanged = urgentItems != _uiState.value.expiringItems
        val preferencesChanged = dietType != cachedDietType ||
                allergies != cachedAllergies ||
                householdType != cachedHouseholdType

        cachedInventory = allInventoryItems
        cachedDietType = dietType
        cachedAllergies = allergies
        cachedHouseholdType = householdType

        _uiState.update { it.copy(expiringItems = urgentItems) }

        // Skip remote API call if state is unchanged and recipes already exist
        if (!itemsChanged && !preferencesChanged && _uiState.value.recipes.isNotEmpty()) {
            return
        }

        executeFetch(urgentItems)
    }

    fun retryFetch() {
        val urgentItems = cachedInventory.filter { !it.isConsumed && it.daysLeft <= 3 }
        executeFetch(urgentItems)
    }

    private fun executeFetch(urgentItems: List<Inventory>) {
        fetchJob?.cancel()
        fetchJob = viewModelScope.launch {
            _uiState.update {
                it.copy(expiringItems = urgentItems, isLoading = true, errorMessage = null)
            }

            // Handle empty expiring inventory case
            if (urgentItems.isEmpty()) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = if (it.recipes.isEmpty()) {
                            "No items expiring soon. Add some to your inventory to get recipe ideas!"
                        } else null
                    )
                }
                return@launch
            }

            val userId = sessionManager.userIdFlow.value
            if (userId == NO_USER) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = "Please log in to get recipe suggestions."
                    )
                }
                return@launch
            }

            try {
                val fetchedRecipes = repository.fetchAndSaveRecipes(
                    userId, urgentItems, cachedDietType, cachedAllergies, cachedHouseholdType
                )

                _uiState.update {
                    it.copy(
                        recipes = fetchedRecipes,
                        isLoading = false,
                        errorMessage = if (fetchedRecipes.isEmpty()) "No recipes found. Try again later." else null
                    )
                }
            } catch (e: Exception) {
                // Map common internal exception messages to user-friendly messages
                val msg = e.message ?: ""
                val userMessage = when {
                    msg.contains("MISSING_API_KEY") -> "Gemini API Key is missing in local.properties."
                    msg.contains("INVALID_API_KEY") -> "The API Key is invalid or not authorized."
                    msg.contains("NETWORK_ERROR") -> "Cannot reach Gemini. Showing cached data if available."
                    msg.contains("RATE_LIMIT_EXCEEDED") -> "Too many requests. Please wait a moment."
                    msg.contains("SERVER_ERROR") -> "Google server error. Please try again later."
                    else -> "Error: ${e.localizedMessage ?: "Unknown error"}"
                }

                _uiState.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = if (it.recipes.isEmpty()) userMessage else null
                    )
                }
            }
        }
    }
}