package com.example.savebite.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.savebite.data.repo.InventoryRepository
import com.example.savebite.data.repo.ShoppingRepository

class ShoppingViewModelFactory(
    private val shoppingRepository: ShoppingRepository,
    private val inventoryRepository: InventoryRepository
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ShoppingViewModel::class.java)) {
            return ShoppingViewModel(shoppingRepository, inventoryRepository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
