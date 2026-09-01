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

class ReminderViewModel(
    private val inventoryRepository: InventoryRepository
) : ViewModel() {

    // Filter state (drives the filter icon in the top bar)
    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

   // Storage filter state (drives the storage filter dropdown)
    private val _selectedStorage = MutableStateFlow<String?>(null)
    val selectedStorage: StateFlow<String?> = _selectedStorage.asStateFlow()

    val storageOptions: StateFlow<List<String>> = inventoryRepository.allStorageNames
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Filtered + grouped items
    private val filteredItems: Flow<List<Inventory>> = combine(
        inventoryRepository.allInventory,
        _searchQuery,
        _selectedStorage
    ) { items, query, storage ->
        items
            .filter { storage == null || it.storage == storage }
            .filter {
                query.isBlank() ||
                    it.name.contains(query, ignoreCase = true) ||
                    it.category.contains(query, ignoreCase = true)
            }
    }

    val groupedItems: StateFlow<Map<ExpirySection, List<Inventory>>> = filteredItems
        .map { ExpiryGrouping.group(it) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyMap())

    // Total count before filtering, so the empty state can distinguish "no items at all" from "no items match this filter"
    val totalItemCount: StateFlow<Int> = inventoryRepository.allInventory
        .map { it.size }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    fun onSearchQueryChange(query: String) {
        _searchQuery.value = query
    }

    fun onStorageFilterSelected(storage: String?) {
        _selectedStorage.value = storage
    }

    fun clearFilters() {
        _searchQuery.value = ""
        _selectedStorage.value = null
    }
}
