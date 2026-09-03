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

    val searchQuery = MutableStateFlow("")
    val selectedStorage = MutableStateFlow("All")

    val selectedSortOption = MutableStateFlow(InventorySortOption.PRIORITY)
    // Default Storage options
    private val defaultStorages = DefaultStorages.ALL

    val storageList: StateFlow<List<String>>

    var selectedReportStatus = MutableStateFlow(ReportStatus.CONSUMED)

    val inventoryList: StateFlow<List<Inventory>>

    // True when the last cloud sync attempt failed, so the UI can let the user know
    // they're looking at local (possibly stale) data instead of failing silently.
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
            // syncFromCloud() reports failure via Result rather than throwing, so check
            // it directly - wrapping it in try/catch here would never actually catch
            // anything and would always leave isOffline stuck at false.
            val syncResult = repository.syncFromCloud()
            _isOffline.value = syncResult.isFailure
            syncResult.onFailure {
                Log.e("InventoryViewModel", "Cloud sync failed, falling back to local data", it)
            }
            repository.cleanupExpiredItems()
        }
    }

    fun saveItem(item: Inventory) = viewModelScope.launch {
        repository.insertItem(item)
    }

    fun deleteItem(item: Inventory) = viewModelScope.launch {
        repository.deleteItem(item)
    }

    fun markAsWaste(item: Inventory, qty: Int, reason: String) = viewModelScope.launch {
        repository.moveItemsToReport(listOf(item to qty), ReportStatus.WASTED, reason)
    }

    fun consumeItemQuantity(item: Inventory, qty: Int) = viewModelScope.launch {
        repository.moveItemsToReport(listOf(item to qty), ReportStatus.CONSUMED, "Consumed")
    }

    fun addStorage(name: String) = viewModelScope.launch {
        repository.insertStorage(name)
    }

    fun deleteStorage(name: String) = viewModelScope.launch {
        if (selectedStorage.value == name) {
            selectedStorage.value = "All"
        }
        repository.deleteStorageAndReassign(name)
    }

    fun getItemById(id: String) = repository.getItemById(id)

    fun toggleConsumed(item: Inventory) = viewModelScope.launch {
        repository.toggleConsumed(item)
    }

    fun transferSelectedToReport(onSuccess: () -> Unit) = viewModelScope.launch {
        repository.moveConsumedToReport()
        onSuccess()
    }

    fun setReportStatus(status: ReportStatus) {
        selectedReportStatus.value = status
    }

    fun processCustomTransfer(
        itemsWithQty: List<Pair<Inventory, Int>>,
        status: ReportStatus,
        reason: String = "Normal Consumption",
        onSuccess: () -> Unit
    ) = viewModelScope.launch {
        repository.moveItemsToReport(itemsWithQty, status, reason)
        onSuccess()
    }

    fun onSortOptionSelected(sortOption: InventorySortOption) {
        selectedSortOption.value = sortOption
    }
}