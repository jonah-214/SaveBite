package com.example.savebite.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.savebite.data.local.AppDatabase
import com.example.savebite.data.local.InventoryRepository
import com.example.savebite.model.Inventory
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class InventoryViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: InventoryRepository
    val searchQuery = MutableStateFlow("")
    val selectedStorage = MutableStateFlow("All")

    @OptIn(ExperimentalCoroutinesApi::class)
    val inventoryList: StateFlow<List<Inventory>>

    init {
        val dao = AppDatabase.getDatabase(application).inventoryDao()
        repository = InventoryRepository(dao)

        // Dynamically re-query based on Search query & Storage Filter Tab
        inventoryList = searchQuery.flatMapLatest { query ->
            selectedStorage.flatMapLatest { storage ->
                repository.searchAndFilter(query, storage)
            }
        }.stateIn(viewModelScope, kotlinx.coroutines.flow.SharingStarted.Lazily, emptyList())
    }

    fun saveItem(item: Inventory) = viewModelScope.launch {
        repository.insertItem(item)
    }

    fun deleteItem(item: Inventory) = viewModelScope.launch {
        repository.deleteItem(item)
    }

    fun getItemById(id: String) = repository.getItemById(id)
}