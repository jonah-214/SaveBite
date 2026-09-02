package com.example.savebite.data.repo

import com.example.savebite.data.local.dao.InventoryDao
import com.example.savebite.data.local.dao.ReportDao
import com.example.savebite.data.local.dao.StorageDao
import com.example.savebite.data.remote.toRoom
import com.example.savebite.data.remote.toSupabase
import com.example.savebite.model.Inventory
import com.example.savebite.model.ReportItem
import com.example.savebite.model.ReportStatus
import com.example.savebite.model.ReportStatus.WASTED
import com.example.savebite.model.Storage
import com.example.savebite.utils.DateFormats
import kotlinx.coroutines.flow.Flow
import java.util.Date
import java.util.concurrent.TimeUnit

class InventoryRepository(
    private val inventoryDao: InventoryDao,
    private val storageDao: StorageDao,
    private val reportDao: ReportDao,
    private val supabaseDataRepository: SupabaseDataRepository = SupabaseDataRepository()
) {

    val allInventory: Flow<List<Inventory>> = inventoryDao.getAllInventory()

    val allStorageNames: Flow<List<String>> = storageDao.getAllStorageNames()

    suspend fun insertItem(item: Inventory) {
        inventoryDao.insertItem(item)
        supabaseDataRepository.upsertInventoryItem(item.toSupabase())
    }

    suspend fun updateItem(item: Inventory) {
        inventoryDao.updateItem(item)
        supabaseDataRepository.upsertInventoryItem(item.toSupabase())
    }

    suspend fun deleteItem(item: Inventory) {
        inventoryDao.deleteItem(item)
        supabaseDataRepository.deleteInventoryItem(item.id)
    }

    fun getItemById(id: String): Flow<Inventory?> = inventoryDao.getInventoryById(id)

    fun searchAndFilter(query: String, storage: String): Flow<List<Inventory>> {
        return inventoryDao.searchAndFilterInventory(query, storage)
    }

    suspend fun insertStorage(name: String) {
        if (name.isNotBlank()) {
            val storage = Storage(name)
            storageDao.insertStorage(storage)
            supabaseDataRepository.upsertStorage(storage.toSupabase())
        }
    }

    suspend fun deleteStorageAndReassign(name: String, defaultStorage: String = "Refrigerator") {
        inventoryDao.reassignStorage(name, defaultStorage)
        storageDao.deleteStorage(Storage(name))
        supabaseDataRepository.deleteStorage(name)

        val updatedItems = inventoryDao.getAllInventorySync().filter { it.storage == defaultStorage }
        updatedItems.forEach { item ->
            supabaseDataRepository.upsertInventoryItem(item.toSupabase())
        }
    }

    suspend fun markAsWaste(item: Inventory, reason: String) {
        val unitPrice = if (item.quantity > 0) item.price / item.quantity else item.price
        val reportItem = ReportItem(
            name = item.name,
            category = item.category,
            price = unitPrice,
            quantity = item.quantity,
            unit = item.unit,
            status = WASTED,
            reason = reason
        )
        reportDao.insertReportItem(reportItem)
        inventoryDao.deleteItem(item)

        // Supabase 同步
        supabaseDataRepository.insertReportItem(reportItem.toSupabase())
        supabaseDataRepository.deleteInventoryItem(item.id)
    }

    suspend fun toggleConsumed(item: Inventory) {
        val updated = item.copy(isConsumed = !item.isConsumed)
        inventoryDao.updateItem(updated)
        supabaseDataRepository.upsertInventoryItem(updated.toSupabase())
    }

    suspend fun moveConsumedToReport() {
        val consumedList = inventoryDao.getConsumedItems()
        if (consumedList.isNotEmpty()) {
            val reportItems = consumedList.map { item ->
                val unitPrice = if (item.quantity > 0) item.price / item.quantity else item.price
                ReportItem(
                    name = item.name,
                    category = item.category,
                    price = unitPrice,
                    quantity = item.quantity,
                    unit = item.unit,
                    status = ReportStatus.CONSUMED,
                    reason = "Consumed"
                )
            }
            reportDao.insertReportItems(reportItems)
            inventoryDao.deleteConsumedItems()

            // 批量同步云端
            reportItems.forEach { supabaseDataRepository.insertReportItem(it.toSupabase()) }
            consumedList.forEach { supabaseDataRepository.deleteInventoryItem(it.id) }
        }
    }

    suspend fun cleanupExpiredItems() {
        val allItems = inventoryDao.getAllInventorySync()
        val today = Date()

        allItems.forEach { item ->
            val expiryDate = DateFormats.parseExpiryOrNull(item.expiry) ?: return@forEach
            val diffInMillis = expiryDate.time - today.time
            val days = TimeUnit.DAYS.convert(diffInMillis, TimeUnit.MILLISECONDS).toInt()

            if (days < 0) {
                markAsWaste(item, "Expired")
            } else {
                val newDaysLeft = days + 1
                if (item.daysLeft != newDaysLeft) {
                    val updatedItem = item.copy(daysLeft = newDaysLeft)
                    inventoryDao.updateItem(updatedItem)
                    supabaseDataRepository.upsertInventoryItem(updatedItem.toSupabase())
                }
            }
        }
    }

    suspend fun moveItemsToReport(
        itemsWithQty: List<Pair<Inventory, Int>>,
        status: ReportStatus,
        reason: String
    ) {
        itemsWithQty.forEach { (item, moveQty) ->
            if (moveQty > 0) {
                val unitPrice = if (item.quantity > 0) item.price / item.quantity else item.price
                val reportItem = ReportItem(
                    name = item.name,
                    category = item.category,
                    price = unitPrice,
                    quantity = moveQty,
                    unit = item.unit,
                    status = status,
                    reason = if (status == WASTED) reason else "Consumed"
                )
                reportDao.insertReportItem(reportItem)
                supabaseDataRepository.insertReportItem(reportItem.toSupabase())

                val remainingQty = item.quantity - moveQty
                if (remainingQty <= 0) {
                    inventoryDao.deleteItem(item)
                    supabaseDataRepository.deleteInventoryItem(item.id)
                } else {
                    val newTotalPrice = (remainingQty.toDouble() / item.quantity.toDouble()) * item.price
                    val updatedItem = item.copy(
                        quantity = remainingQty,
                        price = newTotalPrice,
                        isConsumed = false
                    )
                    inventoryDao.updateItem(updatedItem)
                    supabaseDataRepository.upsertInventoryItem(updatedItem.toSupabase())
                }
            }
        }
    }

    suspend fun syncStorageFromCloud(): Result<Unit> {
        return supabaseDataRepository.fetchStorageList().map { remoteStorageList ->
            val localStorageList = remoteStorageList.map { it.toRoom() }
            localStorageList.forEach { storageDao.insertStorage(it) }
        }
    }

    suspend fun syncFromCloud(): Result<Unit> {
        syncStorageFromCloud()

        return supabaseDataRepository.fetchInventoryItems().map { remoteItems ->
            val localItems = remoteItems.map { it.toRoom() }
            localItems.forEach { inventoryDao.insertItem(it) }
        }
    }
}