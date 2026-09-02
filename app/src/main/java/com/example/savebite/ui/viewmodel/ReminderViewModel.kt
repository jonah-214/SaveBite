package com.example.savebite.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.savebite.data.repo.InventoryRepository
import com.example.savebite.utils.ExpiryGrouping
import com.example.savebite.utils.ExpirySection
import com.example.savebite.model.Inventory
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

enum class SortOrder(val label: String) {
    EXPIRY_ASC("Soonest First"),
    EXPIRY_DESC("Latest First"),
    NAME_ASC("Name (A-Z)"),
    NAME_DESC("Name (Z-A)")
}

class ReminderViewModel(
    private val inventoryRepository: InventoryRepository
) : ViewModel() {

    // Filter state
    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    // Sort state
    private val _sortOrder = MutableStateFlow(SortOrder.EXPIRY_ASC)
    val sortOrder: StateFlow<SortOrder> = _sortOrder.asStateFlow()

    // Filtered + sorted + grouped items
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

    val groupedItems: StateFlow<Map<ExpirySection, List<Inventory>>> = filteredItems
        .map { ExpiryGrouping.group(it) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyMap())

    // Total count before filtering
    val totalItemCount: StateFlow<Int> = inventoryRepository.allInventory
        .map { it.size }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    fun onSearchQueryChange(query: String) {
        _searchQuery.value = query
    }

    fun onSortOrderChange(order: SortOrder) {
        _sortOrder.value = order
    }

    fun clearFilters() {
        _searchQuery.value = ""
        _sortOrder.value = SortOrder.EXPIRY_ASC
    }
}
