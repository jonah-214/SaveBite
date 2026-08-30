package com.example.savebite.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.savebite.data.repo.InventoryRepository
import com.example.savebite.data.local.db.AppDatabase
import com.example.savebite.model.Inventory
import com.example.savebite.model.InventorySortOption
import com.example.savebite.model.ReportStatus
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
class InventoryViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: InventoryRepository
    val searchQuery = MutableStateFlow("")
    val selectedStorage = MutableStateFlow("All")

    val selectedSortOption = MutableStateFlow(InventorySortOption.PRIORITY)
    // Default Storage options
    private val defaultStorages = listOf("Refrigerator", "Pantry", "Freezer")

    val storageList: StateFlow<List<String>>

    var selectedReportStatus = MutableStateFlow(ReportStatus.CONSUMED)

    val inventoryList: StateFlow<List<Inventory>>

    init {
        val db = AppDatabase.getDatabase(application)
        repository = InventoryRepository(db.inventoryDao(), db.storageDao(), db.reportDao())

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
                InventorySortOption.DATE_NEW_TO_OLD -> filteredItems.sortedByDescending { it.expiry }
                InventorySortOption.DATE_OLD_TO_NEW -> filteredItems.sortedBy { it.expiry }
            }
        }.stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

        // Automatically clean up expired items on startup
        viewModelScope.launch {
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
