package com.example.savebite.ui.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.savebite.data.repo.InventoryRepository
import com.example.savebite.model.DefaultStorages
import com.example.savebite.model.Inventory
import com.example.savebite.model.InventorySortOption
import com.example.savebite.model.ReportStatus
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

@OptIn(ExperimentalCoroutinesApi::class)
class InventoryViewModel(private val repository: InventoryRepository) : ViewModel() {

    // Current search query for filtering food items
    val searchQuery = MutableStateFlow("")

    // Currently selected storage location for filtering
    val selectedStorage = MutableStateFlow("All")

    // Current sorting strategy for the inventory list
    val selectedSortOption = MutableStateFlow(InventorySortOption.PRIORITY)

    private val defaultStorages = DefaultStorages.ALL

    // Stream of all available storage locations (default + custom)
    val storageList: StateFlow<List<String>>

    // Current report status (Consumed/Wasted) for batch processing
    var selectedReportStatus = MutableStateFlow(ReportStatus.CONSUMED)

    // The reactive stream of filtered and sorted inventory items
    val inventoryList: StateFlow<List<Inventory>>

    // True if the last cloud sync attempt failed
    private val _isOffline = MutableStateFlow(false)
    val isOffline: StateFlow<Boolean> = _isOffline

    init {
        // Combine default storages with dynamic storages from Room DB
        storageList = repository.allStorageNames.map { dbStorages ->
            (defaultStorages + dbStorages).distinct()
        }.stateIn(viewModelScope, SharingStarted.Lazily, defaultStorages)

        inventoryList = combine(
            searchQuery.flatMapLatest { query ->
                selectedStorage.flatMapLatest { storage ->
                    repository.searchAndFilter(query, storage)
                }
            },
            selectedSortOption
        ) { filteredItems, sortOption ->
            when (sortOption) {
                InventorySortOption.PRIORITY -> filteredItems.sortedBy { it.daysLeft }
                InventorySortOption.NAME_A_TO_Z -> filteredItems.sortedBy { it.name.lowercase() }
                InventorySortOption.NAME_Z_TO_A -> filteredItems.sortedByDescending { it.name.lowercase() }
                InventorySortOption.DATE_NEW_TO_OLD -> filteredItems.sortedByDescending { it.purchaseDate }
                InventorySortOption.DATE_OLD_TO_NEW -> filteredItems.sortedBy { it.purchaseDate }
            }
        }.stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

        viewModelScope.launch(Dispatchers.IO) {
            val syncResult = repository.syncFromCloud()
            _isOffline.value = syncResult.isFailure
            syncResult.onFailure {
                Log.e("InventoryViewModel", "Cloud sync failed, falling back to local data", it)
            }
            repository.cleanupExpiredItems()
        }
    }

    // Persists an item and syncs with the cloud
    fun saveItem(item: Inventory) = viewModelScope.launch {
        repository.insertItem(item)
    }

    // Removes an item from inventory and the cloud
    fun deleteItem(item: Inventory) = viewModelScope.launch {
        repository.deleteItem(item)
    }

    // Moves a specific quantity of an item to the waste report
    fun markAsWaste(item: Inventory, qty: Int, reason: String) = viewModelScope.launch {
        repository.moveItemsToReport(listOf(item to qty), ReportStatus.WASTED, reason)
    }

    // Moves a specific quantity of an item to the consumption report
    fun consumeItemQuantity(item: Inventory, qty: Int) = viewModelScope.launch {
        repository.moveItemsToReport(listOf(item to qty), ReportStatus.CONSUMED, "Consumed")
    }

    // Adds a custom storage location
    fun addStorage(name: String) = viewModelScope.launch {
        repository.insertStorage(name)
    }

    // Deletes a storage location and reassigns items to "Other"
    fun deleteStorage(name: String) = viewModelScope.launch {
        if (selectedStorage.value == name) {
            selectedStorage.value = "All"
        }
        repository.deleteStorageAndReassign(name)
    }

    // Returns an observable stream for a single item
    fun getItemById(id: String) = repository.getItemById(id)

    // Toggles item selection for batch reporting
    fun toggleConsumed(item: Inventory) = viewModelScope.launch {
        repository.toggleConsumed(item)
    }

    // Transfers all items currently selected (marked as consumed) to the report
    fun transferSelectedToReport(onSuccess: () -> Unit) = viewModelScope.launch {
        repository.moveConsumedToReport()
        onSuccess()
    }

    // Sets the reporting mode (Consumed/Wasted) for batch processing
    fun setReportStatus(status: ReportStatus) {
        selectedReportStatus.value = status
    }

    // Executes a custom transfer of multiple items to the report
    fun processCustomTransfer(
        itemsWithQty: List<Pair<Inventory, Int>>,
        status: ReportStatus,
        reason: String = "Normal Consumption",
        onSuccess: () -> Unit
    ) = viewModelScope.launch {
        repository.moveItemsToReport(itemsWithQty, status, reason)
        onSuccess()
    }

    // Updates the list sorting strategy
    fun onSortOptionSelected(sortOption: InventorySortOption) {
        selectedSortOption.value = sortOption
    }
}