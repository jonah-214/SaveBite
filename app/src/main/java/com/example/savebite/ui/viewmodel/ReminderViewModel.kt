package com.example.savebite.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.savebite.data.repo.InventoryRepository
import com.example.savebite.utils.ExpiryGrouping
import com.example.savebite.utils.ExpirySection
import com.example.savebite.model.Inventory
import com.example.savebite.R
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

// Supported sorting strategies for the Reminder screen
enum class SortOrder(val labelRes: Int) {
    EXPIRY_ASC(R.string.sort_expiry_asc),
    EXPIRY_DESC(R.string.sort_expiry_desc),
    NAME_ASC(R.string.sort_name_asc),
    NAME_DESC(R.string.sort_name_desc)
}

class ReminderViewModel(
    private val inventoryRepository: InventoryRepository
) : ViewModel() {

    private val _searchQuery = MutableStateFlow("")
    // Current text query for filtering items
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _sortOrder = MutableStateFlow(SortOrder.EXPIRY_ASC)
    // Currently selected sort strategy
    val sortOrder: StateFlow<SortOrder> = _sortOrder.asStateFlow()

    private val filteredItems: Flow<List<Inventory>> = combine(
        inventoryRepository.allInventory,
        _searchQuery,
        _sortOrder
    ) { items, query, sort ->
        items
            .filter {
                query.isBlank() ||
                    it.name.contains(query, ignoreCase = true) ||
                    it.category.contains(query, ignoreCase = true)
            }
            .let { filtered ->
                when (sort) {
                    SortOrder.EXPIRY_ASC -> filtered.sortedBy { it.daysLeft }
                    SortOrder.EXPIRY_DESC -> filtered.sortedByDescending { it.daysLeft }
                    SortOrder.NAME_ASC -> filtered.sortedBy { it.name.lowercase() }
                    SortOrder.NAME_DESC -> filtered.sortedByDescending { it.name.lowercase() }
                }
            }
    }

    // Map of inventory items grouped by their [ExpirySection] (Soon, This Week, Later)
    val groupedItems: StateFlow<Map<ExpirySection, List<Inventory>>> = filteredItems
        .map { ExpiryGrouping.group(it) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyMap())

    // Total count of items in the inventory
    val totalItemCount: StateFlow<Int> = inventoryRepository.allInventory
        .map { it.size }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    // Updates the current search query
    fun onSearchQueryChange(query: String) {
        _searchQuery.value = query
    }

    // Updates the list sort order
    fun onSortOrderChange(order: SortOrder) {
        _sortOrder.value = order
    }

    // Resets search and sort to default values
    fun clearFilters() {
        _searchQuery.value = ""
        _sortOrder.value = SortOrder.EXPIRY_ASC
    }
}