package com.example.savebite.data.local

import com.example.savebite.model.Inventory
import com.example.savebite.model.Storage
import kotlinx.coroutines.flow.Flow

class InventoryRepository(
    private val inventoryDao: InventoryDao,
    private val storageDao: StorageDao
) {

    val allInventory: Flow<List<Inventory>> = inventoryDao.getAllInventory()

    val allStorageNames: Flow<List<String>> = storageDao.getAllStorageNames()

    suspend fun insertItem(item: Inventory) = inventoryDao.insertItem(item)

    suspend fun updateItem(item: Inventory) = inventoryDao.updateItem(item)

    suspend fun deleteItem(item: Inventory) = inventoryDao.deleteItem(item)

    fun getItemById(id: String): Flow<Inventory?> = inventoryDao.getInventoryById(id)

    fun searchAndFilter(query: String, storage: String): Flow<List<Inventory>> {
        return inventoryDao.searchAndFilterInventory(query, storage)
    }

    suspend fun insertStorage(name: String) {
        if (name.isNotBlank()) {
            storageDao.insertStorage(Storage(name))
        }
    }
}