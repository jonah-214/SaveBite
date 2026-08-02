package com.example.savebite.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.savebite.data.local.AppDatabase
import com.example.savebite.data.local.InventoryRepository
import com.example.savebite.model.Inventory
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class InventoryViewModel(
    application: Application,
    WhileSubsubscribed: SharingStarted.Companion.(Int) -> SharingStarted
) : AndroidViewModel(application) {

    private val repository: InventoryRepository

    // Converts Room Flow into a Compose-friendly StateFlow
    val inventoryList: StateFlow<List<Inventory>>

    init {
        val dao = AppDatabase.getDatabase(application).inventoryDao()
        repository = InventoryRepository(dao)

        inventoryList = repository.allInventory.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubsubscribed(5000),
            initialValue = emptyList()
        )
    }

    fun addInventoryItem(item: Inventory) {
        viewModelScope.launch {
            repository.insertItem(item)
        }
    }

    fun deleteInventoryItem(item: Inventory) {
        viewModelScope.launch {
            repository.deleteItem(item)
        }
    }
}