package com.example.savebite.data.repo

import android.util.Log
import com.example.savebite.data.local.dao.InventoryDao
import com.example.savebite.data.local.dao.ReportDao
import com.example.savebite.data.local.dao.StorageDao
import com.example.savebite.data.remote.toRoom
import com.example.savebite.data.remote.toSupabase
import com.example.savebite.model.DefaultStorages
import com.example.savebite.model.Inventory
import com.example.savebite.model.ReportItem
import com.example.savebite.model.ReportStatus
import com.example.savebite.model.ReportStatus.WASTED
import com.example.savebite.model.Storage
import com.example.savebite.model.unitPrice
import com.example.savebite.utils.DateFormats
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import java.util.Date
import java.util.concurrent.TimeUnit

class InventoryRepository(
    private val inventoryDao: InventoryDao,
    private val storageDao: StorageDao,
    private val reportDao: ReportDao,
    private val supabaseDataRepository: SupabaseDataRepository
) {

    companion object {
        private const val TAG = "InventoryRepository"
    }

    // Stream of all non-deleted inventory items
    val allInventory: Flow<List<Inventory>> = inventoryDao.getAllInventory()

    // Stream of all registered storage location names
    val allStorageNames: Flow<List<String>> = storageDao.getAllStorageNames()

    // Inserts a new inventory item locally and syncs it to the cloud
    suspend fun insertItem(item: Inventory) {
        inventoryDao.insertItem(item)
        supabaseDataRepository.upsertInventoryItem(item.toSupabase())
            .onFailure { Log.e(TAG, "Supabase upsert failed for item ${item.id} (${item.name})", it) }
    }

    // Updates an existing inventory item locally and syncs it to the cloud
    suspend fun updateItem(item: Inventory) {
        inventoryDao.updateItem(item)
        supabaseDataRepository.upsertInventoryItem(item.toSupabase())
            .onFailure { Log.e(TAG, "Supabase upsert failed for item ${item.id} (${item.name})", it) }
    }

    // Deletes an inventory item locally and from the cloud
    suspend fun deleteItem(item: Inventory) {
        inventoryDao.deleteItem(item)
        supabaseDataRepository.deleteInventoryItem(item.id)
    }

    // Returns a stream of a specific inventory item by its ID
    fun getItemById(id: String): Flow<Inventory?> = inventoryDao.getInventoryById(id)

    // Filters inventory based on a name query and storage location
    fun searchAndFilter(query: String, storage: String): Flow<List<Inventory>> {
        return inventoryDao.searchAndFilterInventory(query, storage)
    }

    // Adds a new storage location
    suspend fun insertStorage(name: String) {
        if (name.isNotBlank()) {
            val storage = Storage(name)
            storageDao.insertStorage(storage)
            supabaseDataRepository.upsertStorage(storage.toSupabase())
        }
    }

    // Deletes a storage location and reassigns all items within it to a fallback location
    // Safely removes a storage location by reassigning affected inventory items
    suspend fun deleteStorageAndReassign(name: String, defaultStorage: String = DefaultStorages.FALLBACK) {
        inventoryDao.reassignStorage(name, defaultStorage)
        storageDao.deleteStorage(Storage(name))
        supabaseDataRepository.deleteStorage(name)

        val updatedItems = inventoryDao.getItemsByStorageSync(defaultStorage)
        updatedItems.forEach { item ->
            supabaseDataRepository.upsertInventoryItem(item.toSupabase())
        }
    }

    // Marks an item as wasted, removes it from inventory, and logs it in the report
    // Converts an inventory item into a waste report record
    suspend fun markAsWaste(item: Inventory, reason: String) {
        val reportItem = ReportItem(
            name = item.name,
            category = item.category,
            price = item.unitPrice(),
            quantity = item.quantity,
            unit = item.unit,
            status = WASTED,
            reason = reason
        )
        reportDao.insertReportItem(reportItem)
        inventoryDao.deleteItem(item)

        supabaseDataRepository.insertReportItem(reportItem.toSupabase())
            .onFailure { Log.e(TAG, "Supabase insert failed for wasted item ${reportItem.name}", it) }
        supabaseDataRepository.deleteInventoryItem(item.id)
            .onFailure { Log.e(TAG, "Supabase delete failed for item ${item.id} (${item.name})", it) }
    }

    // Toggles the "isConsumed" flag on an item (used for batch selection)
    suspend fun toggleConsumed(item: Inventory) {
        val updated = item.copy(isConsumed = !item.isConsumed)
        inventoryDao.updateItem(updated)
        supabaseDataRepository.upsertInventoryItem(updated.toSupabase())
            .onFailure { Log.e(TAG, "Supabase upsert failed for item ${item.id} (${item.name})", it) }
    }

    // Transfers all items currently marked as "consumed" to the report table
    // Batch converts all items marked as consumed into report items
    suspend fun moveConsumedToReport() {
        val consumedList = inventoryDao.getConsumedItems()
        if (consumedList.isNotEmpty()) {
            val reportItems = consumedList.map { item ->
                ReportItem(
                    name = item.name,
                    category = item.category,
                    price = item.unitPrice(),
                    quantity = item.quantity,
                    unit = item.unit,
                    status = ReportStatus.CONSUMED,
                    reason = "Consumed"
                )
            }
            reportDao.insertReportItems(reportItems)
            inventoryDao.deleteConsumedItems()

            reportItems.forEach {
                supabaseDataRepository.insertReportItem(it.toSupabase())
                    .onFailure { error -> Log.e(TAG, "Supabase insert failed for consumed item ${it.name}", error) }
            }
            consumedList.forEach {
                supabaseDataRepository.deleteInventoryItem(it.id)
                    .onFailure { error -> Log.e(TAG, "Supabase delete failed for consumed item ${it.id} (${it.name})", error) }
            }
        }
    }

    // Recalculates days left for all items and automatically marks expired items as waste
    // Also normalizes date formats for backward compatibility
    suspend fun cleanupExpiredItems() {
        val allItems = inventoryDao.getAllInventorySync()
        val today = Date()

        coroutineScope {
            allItems.forEach { item ->
                launch {
                    val expiryDate = DateFormats.parseExpiryOrNull(item.expiry) ?: return@launch
                    val diffInMillis = expiryDate.time - today.time
                    val days = TimeUnit.DAYS.convert(diffInMillis, TimeUnit.MILLISECONDS).toInt()

                    val normalizedExpiry = DateFormats.toStorageString(expiryDate)
                    val normalizedPurchaseDate = DateFormats.parseExpiryOrNull(item.purchaseDate)
                        ?.let { DateFormats.toStorageString(it) } ?: item.purchaseDate

                    if (days < 0) {
                        markAsWaste(item, "Expired")
                    } else {
                        val newDaysLeft = days + 1
                        if (item.daysLeft != newDaysLeft || item.expiry != normalizedExpiry || item.purchaseDate != normalizedPurchaseDate) {
                            val updatedItem = item.copy(
                                daysLeft = newDaysLeft,
                                expiry = normalizedExpiry,
                                purchaseDate = normalizedPurchaseDate
                            )
                            inventoryDao.updateItem(updatedItem)
                            supabaseDataRepository.upsertInventoryItem(updatedItem.toSupabase())
                        }
                    }
                }
            }
        }
    }

    // Returns names of items expiring within a given threshold
    suspend fun getExpiringItemNames(thresholdDays: Int): List<String> {
        return inventoryDao.getExpiringItemNames(thresholdDays)
    }

    // Logic for transferring specific quantities of items to the report.
    // Updates inventory quantity or deletes the item if fully consumed/wasted.
    suspend fun moveItemsToReport(
        itemsWithQty: List<Pair<Inventory, Int>>,
        status: ReportStatus,
        reason: String
    ) {
        itemsWithQty.forEach { (item, moveQty) ->
            if (moveQty > 0) {
                val reportItem = ReportItem(
                    name = item.name,
                    category = item.category,
                    price = item.unitPrice(),
                    quantity = moveQty,
                    unit = item.unit,
                    status = status,
                    reason = if (status == WASTED) reason else "Consumed"
                )
                reportDao.insertReportItem(reportItem)
                supabaseDataRepository.insertReportItem(reportItem.toSupabase())
                    .onFailure { Log.e(TAG, "Supabase insert failed for report item ${reportItem.name}", it) }

                val remainingQty = item.quantity - moveQty
                if (remainingQty <= 0) {
                    inventoryDao.deleteItem(item)
                    supabaseDataRepository.deleteInventoryItem(item.id)
                        .onFailure { Log.e(TAG, "Supabase delete failed for item ${item.id} (${item.name})", it) }
                } else {
                    val newTotalPrice = (remainingQty.toDouble() / item.quantity.toDouble()) * item.price
                    val updatedItem = item.copy(
                        quantity = remainingQty,
                        price = newTotalPrice,
                        isConsumed = false
                    )
                    inventoryDao.updateItem(updatedItem)
                    supabaseDataRepository.upsertInventoryItem(updatedItem.toSupabase())
                        .onFailure { Log.e(TAG, "Supabase upsert failed for item ${updatedItem.id} (${updatedItem.name})", it) }
                }
            }
        }
    }

    // Synchronizes storage locations from Supabase to Room
    suspend fun syncStorageFromCloud(): Result<Unit> {
        return supabaseDataRepository.fetchStorageList().map { remoteStorageList ->
            val localStorageList = remoteStorageList.map { it.toRoom() }
            localStorageList.forEach { storageDao.insertStorage(it) }
        }
    }

    // Synchronizes inventory items from Supabase
    // Restores items that exist in the cloud but not locally
    suspend fun syncFromCloud(): Result<Unit> {
        syncStorageFromCloud()

        return supabaseDataRepository.fetchInventoryItems().map { remoteItems ->
            val localIds = inventoryDao.getAllInventorySync().map { it.id }.toSet()
            val newItems = remoteItems.map { it.toRoom() }.filter { it.id !in localIds }
            newItems.forEach { inventoryDao.insertItem(it) }
        }
    }
}