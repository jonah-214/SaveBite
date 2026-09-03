package com.example.savebite.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.savebite.data.repo.InventoryRepository
import com.example.savebite.data.repo.ShoppingRepository
import com.example.savebite.model.DefaultStorages
import com.example.savebite.model.Inventory
import com.example.savebite.model.ShoppingItem
import com.example.savebite.utils.DateFormats
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.Date


class ShoppingViewModel(
    private val shoppingRepository: ShoppingRepository,
    private val inventoryRepository: InventoryRepository
) : ViewModel() {

    // Current search query for filtering the shopping list
    val searchQuery = MutableStateFlow("")

    // The observable list of shopping items, filtered by the current [searchQuery].
    val items: StateFlow<List<ShoppingItem>> = combine(
        shoppingRepository.allShoppingItems,
        searchQuery
    ) { itemList, query ->
        if (query.isBlank()) {
            itemList
        } else {
            itemList.filter { it.name.contains(query, ignoreCase = true) }
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    init {
        syncFromCloud()
    }

    // Triggers a synchronization of shopping items from Supabase
    fun syncFromCloud() {
        viewModelScope.launch(Dispatchers.IO) {
            shoppingRepository.syncFromCloud()
        }
    }


    // Updates the search query to filter the list
    fun onSearchQueryChange(newQuery: String) {
        searchQuery.value = newQuery
    }

    // Toggles the purchased state of a shopping item
    fun togglePurchased(item: ShoppingItem) {
        viewModelScope.launch(Dispatchers.IO) {
            shoppingRepository.toggleItemPurchased(item.id)
        }
    }

    // Creates and persists a new shopping item
    fun addItem(name: String, quantity: Int, unit: String, category: String) {
        viewModelScope.launch(Dispatchers.IO) {
            val newItem = ShoppingItem(
                name = name,
                quantity = quantity,
                unit = unit,
                category = category,
                isPurchased = false
            )
            shoppingRepository.insertItem(newItem)
        }
    }

    // Updates an existing shopping item
    fun updateItem(item: ShoppingItem) {
        viewModelScope.launch(Dispatchers.IO) {
            shoppingRepository.updateItem(item)
        }
    }

    // Permanently removes a shopping item
    fun deleteItem(item: ShoppingItem) {
        viewModelScope.launch(Dispatchers.IO) {
            shoppingRepository.deleteItem(item)
        }
    }

    // Moves all purchased items into the user's inventory and clears them from the shopping list
    // Uses default storage and expiry values as placeholders
    fun transferSelectedToInventory(onComplete: () -> Unit) {
        viewModelScope.launch(Dispatchers.IO) {
            val purchased = items.value.filter { it.isPurchased }
            val currentDate = DateFormats.toStorageString(Date())

            purchased.forEach { item ->
                val newInventoryItem = Inventory(
                    name = item.name,
                    quantity = item.quantity,
                    unit = item.unit,
                    category = item.category,
                    storage = DefaultStorages.FALLBACK,
                    daysLeft = 7,
                    purchaseDate = currentDate,
                    expiry = currentDate
                )
                inventoryRepository.insertItem(newInventoryItem)
            }

            shoppingRepository.clearPurchasedItems()

            launch(Dispatchers.Main) {
                onComplete()
            }
        }
    }
}