package com.example.savebite.data.local

import com.example.savebite.model.Inventory
import kotlinx.coroutines.flow.Flow

class InventoryRepository(private val inventoryDao: InventoryDao) {

    val allInventory: Flow<List<Inventory>> = inventoryDao.getAllInventory()

    suspend fun insertItem(item: Inventory) {
        inventoryDao.insertItem(item)
    }

    suspend fun deleteItem(item: Inventory) {
        inventoryDao.deleteItem(item)
    }

    fun searchAndFilter(query: String, storage: String): Flow<List<Inventory>> {
        return inventoryDao.searchAndFilterInventory(query, storage)
    }
}