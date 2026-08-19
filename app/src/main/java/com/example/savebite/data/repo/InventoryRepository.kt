package com.example.savebite.data.repo

import com.example.savebite.data.local.dao.InventoryDao
import com.example.savebite.data.local.dao.StorageDao
import com.example.savebite.data.local.dao.WastedItemDao
import com.example.savebite.model.Inventory
import com.example.savebite.model.Storage
import com.example.savebite.model.WastedItem
import kotlinx.coroutines.flow.Flow

class InventoryRepository(
    private val inventoryDao: InventoryDao,
    private val storageDao: StorageDao,
    private val wastedItemDao: WastedItemDao
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

    suspend fun deleteStorageAndReassign(name: String, defaultStorage: String = "Refrigerator") {
        inventoryDao.reassignStorage(name, defaultStorage)
        storageDao.deleteStorage(Storage(name))
    }

    suspend fun markAsWaste(item: Inventory) {
        val wastedItem = WastedItem(
            name = item.name,
            category = item.category,
            quantity = item.quantity,
            unit = item.unit
        )
        wastedItemDao.insertWastedItem(wastedItem)
        inventoryDao.deleteItem(item)
    }
}
