package com.example.savebite.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.savebite.data.ai.Recipe
import com.example.savebite.data.repo.RecipeRepository
import com.example.savebite.model.Inventory
import com.example.savebite.utils.SessionManager
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.launch

data class RecipeUiState(
    val expiringItems: List<Inventory> = emptyList(),
    val recipes: List<Recipe> = emptyList(),
    val isLoading: Boolean = false,
    val errorMessage: String? = null
)

@OptIn(ExperimentalCoroutinesApi::class)
class RecipeViewModel(
    private val repository: RecipeRepository,
    private val sessionManager: SessionManager
) : ViewModel() {

    companion object {
        // Matches SessionManager's own "not logged in" sentinel (kept as a local
        // literal since that constant isn't exposed - DashboardViewModel does the same).
        private const val NO_USER = -1
    }

    private val _uiState = MutableStateFlow(RecipeUiState())
    val uiState: StateFlow<RecipeUiState> = _uiState.asStateFlow()

    private var cachedInventory: List<Inventory> = emptyList()

    // Cached alongside cachedInventory so retryFetch() can re-send the same request
    // without the caller having to pass preferences in a second time.
    private var cachedDietType: String = "None"
    private var cachedAllergies: Set<String> = emptySet()
    private var cachedHouseholdType: String = "Student"

    // Tracks the in-flight AI request so a new one (e.g. from fetchAIRecipes firing
    // again before the previous call returned) cancels it instead of letting both
    // run concurrently and possibly having the older response overwrite the newer one.
    private var fetchJob: Job? = null

    init {
        // ViewModel 初始化时立即监听 Room 缓存数据 - scoped to whichever user is
        // currently logged in, so switching accounts doesn't show the previous
        // user's cached recipes (see RecipeDao / RecipeRepositoryImpl).
        viewModelScope.launch {
            sessionManager.userIdFlow.flatMapLatest { userId ->
                if (userId == NO_USER) flowOf(emptyList()) else repository.cachedRecipes(userId)
            }.collect { localRecipes ->
                if (localRecipes.isNotEmpty() && _uiState.value.recipes.isEmpty()) {
                    // Clearing errorMessage too, since a stale "no items expiring" /
                    // "no recipes found" message would otherwise keep hiding this list
                    // once it arrives (RecipeScreen shows the error over the recipes).
                    _uiState.value = _uiState.value.copy(recipes = localRecipes, errorMessage = null)
                }
            }
        }
    }

    fun fetchAIRecipes(
        allInventoryItems: List<Inventory>,
        dietType: String = "None",
        allergies: Set<String> = emptySet(),
        householdType: String = "Student"
    ) {
        val urgentItems = allInventoryItems.filter { !it.isConsumed && it.daysLeft <= 3 }

        // Compare against the OLD cached values before overwriting them below — this is what
        // actually lets us tell whether the expiring items or the preferences changed, so a
        // preference edit in Profile & Settings correctly triggers a fresh AI request.
        val itemsChanged = urgentItems != _uiState.value.expiringItems
        val preferencesChanged = dietType != cachedDietType ||
            allergies != cachedAllergies ||
            householdType != cachedHouseholdType

        cachedInventory = allInventoryItems
        cachedDietType = dietType
        cachedAllergies = allergies
        cachedHouseholdType = householdType

        _uiState.value = _uiState.value.copy(expiringItems = urgentItems)

        // 如果已有数据且过期食材/偏好都没变，不自动刷新
        if (!itemsChanged && !preferencesChanged && _uiState.value.recipes.isNotEmpty()) {
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
        // Cancel any request already in flight before starting a new one.
        fetchJob?.cancel()
        fetchJob = viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                expiringItems = urgentItems,
                isLoading = true,
                errorMessage = null
            )

            // Nothing to ask the AI about - bail out here instead of calling
            // fetchAndSaveRecipes, which would return an empty list and wipe out
            // whatever recipes are already on screen (e.g. from Room's cache).
            if (urgentItems.isEmpty()) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = if (_uiState.value.recipes.isEmpty()) {
                        "No items expiring soon. Add some to your inventory to get recipe ideas!"
                    } else null
                )
                return@launch
            }

            val userId = sessionManager.userIdFlow.value
            if (userId == NO_USER) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = "Please log in to get recipe suggestions."
                )
                return@launch
            }

            try {
                // 调用 repository，成功后会自动存入 Room
                val fetchedRecipes = repository.fetchAndSaveRecipes(
                    userId,
                    urgentItems,
                    cachedDietType,
                    cachedAllergies,
                    cachedHouseholdType
                )

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
