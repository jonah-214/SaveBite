package com.example.savebite.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.savebite.data.repo.InventoryRepository
import com.example.savebite.data.repo.ShoppingRepository
import com.example.savebite.model.Inventory
import com.example.savebite.model.ShoppingItem
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class ShoppingViewModel(
    private val shoppingRepository: ShoppingRepository,
    private val inventoryRepository: InventoryRepository
) : ViewModel() {

    val items: StateFlow<List<ShoppingItem>> = shoppingRepository.allShoppingItems
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun togglePurchased(item: ShoppingItem) {
        viewModelScope.launch {
            shoppingRepository.updateItem(item.copy(isPurchased = !item.isPurchased))
        }
    }

    fun addItem(name: String, quantity: Int, unit: String, category: String) {
        viewModelScope.launch {
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

    fun transferSelectedToInventory(onComplete: () -> Unit) {
        viewModelScope.launch {
            val purchased = items.value.filter { it.isPurchased }
            val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            val currentDate = dateFormat.format(Date())

            purchased.forEach { item ->
                val newInventoryItem = Inventory(
                    name = item.name,
                    quantity = item.quantity,
                    unit = item.unit,
                    category = item.category,
                    storage = "Refrigerator",
                    daysLeft = 7,
                    purchaseDate = currentDate,
                    expiry = currentDate
                )
                inventoryRepository.insertItem(newInventoryItem)
            }

            shoppingRepository.clearPurchasedItems()
            onComplete()
        }
    }
}