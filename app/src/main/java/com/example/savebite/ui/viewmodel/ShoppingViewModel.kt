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

    val searchQuery = MutableStateFlow("")

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

    fun syncFromCloud() {
        viewModelScope.launch(Dispatchers.IO) {
            shoppingRepository.syncFromCloud()
        }
    }

    fun onSearchQueryChange(newQuery: String) {
        searchQuery.value = newQuery
    }

    fun togglePurchased(item: ShoppingItem) {
        viewModelScope.launch(Dispatchers.IO) {
            shoppingRepository.updateItem(item.copy(isPurchased = !item.isPurchased))
        }
    }

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

    fun updateItem(item: ShoppingItem) {
        viewModelScope.launch(Dispatchers.IO) {
            shoppingRepository.updateItem(item)
        }
    }

    fun deleteItem(item: ShoppingItem) {
        viewModelScope.launch(Dispatchers.IO) {
            shoppingRepository.deleteItem(item)
        }
    }

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