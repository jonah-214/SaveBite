package com.example.savebite.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.savebite.data.repo.InventoryRepository
import com.example.savebite.data.local.db.AppDatabase
import com.example.savebite.model.Inventory
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

@OptIn(ExperimentalCoroutinesApi::class)
class InventoryViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: InventoryRepository
    val searchQuery = MutableStateFlow("")
    val selectedStorage = MutableStateFlow("All")

    // Default Storage options
    private val defaultStorages = listOf("Pantry", "Refrigerator", "Freezer")

    val storageList: StateFlow<List<String>>

    @OptIn(ExperimentalCoroutinesApi::class)
    val inventoryList: StateFlow<List<Inventory>>

    init {
        val db = AppDatabase.getDatabase(application)
        repository = InventoryRepository(db.inventoryDao(), db.storageDao(), db.wastedItemDao())

        // Combine default storages with dynamic storages from Room DB
        storageList = repository.allStorageNames.map { dbStorages ->
            (defaultStorages + dbStorages).distinct()
        }.stateIn(viewModelScope, SharingStarted.Lazily, defaultStorages)

        inventoryList = searchQuery.flatMapLatest { query ->
            selectedStorage.flatMapLatest { storage ->
                repository.searchAndFilter(query, storage)
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

    fun markAsWaste(item: Inventory) = viewModelScope.launch {
        repository.markAsWaste(item)
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
}
