package com.example.savebite.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.savebite.data.repo.InventoryRepository
import com.example.savebite.data.repo.ShoppingRepository
import com.example.savebite.model.Inventory
import com.example.savebite.model.ShoppingItem
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class ShoppingViewModel(
    private val shoppingRepository: ShoppingRepository,
    private val inventoryRepository: InventoryRepository
) : ViewModel() {

    // 搜索关键字状态
    val searchQuery = MutableStateFlow("")

    // 将数据库列表与 searchQuery 进行动态结合过滤
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

    fun onSearchQueryChange(newQuery: String) {
        searchQuery.value = newQuery
    }

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

    fun updateItem(item: ShoppingItem) {
        viewModelScope.launch {
            shoppingRepository.updateItem(item)
        }
    }

    fun deleteItem(item: ShoppingItem) {
        viewModelScope.launch {
            shoppingRepository.deleteItem(item)
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