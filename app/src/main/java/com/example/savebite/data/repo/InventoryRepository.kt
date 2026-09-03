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

    val allInventory: Flow<List<Inventory>> = inventoryDao.getAllInventory()

    val allStorageNames: Flow<List<String>> = storageDao.getAllStorageNames()

    suspend fun insertItem(item: Inventory) {
        inventoryDao.insertItem(item)
        supabaseDataRepository.upsertInventoryItem(item.toSupabase())
            .onFailure { Log.e(TAG, "Supabase upsert failed for item ${item.id} (${item.name})", it) }
    }

    suspend fun updateItem(item: Inventory) {
        inventoryDao.updateItem(item)
        supabaseDataRepository.upsertInventoryItem(item.toSupabase())
            .onFailure { Log.e(TAG, "Supabase upsert failed for item ${item.id} (${item.name})", it) }
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

    suspend fun deleteStorageAndReassign(name: String, defaultStorage: String = DefaultStorages.FALLBACK) {
        inventoryDao.reassignStorage(name, defaultStorage)
        storageDao.deleteStorage(Storage(name))
        supabaseDataRepository.deleteStorage(name)

        val updatedItems = inventoryDao.getItemsByStorageSync(defaultStorage)
        updatedItems.forEach { item ->
            supabaseDataRepository.upsertInventoryItem(item.toSupabase())
        }
    }

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

        // Supabase 同步
        supabaseDataRepository.insertReportItem(reportItem.toSupabase())
        supabaseDataRepository.deleteInventoryItem(item.id)
    }

    suspend fun toggleConsumed(item: Inventory) {
        val updated = item.copy(isConsumed = !item.isConsumed)
        inventoryDao.updateItem(updated)
        supabaseDataRepository.upsertInventoryItem(updated.toSupabase())
            .onFailure { Log.e(TAG, "Supabase upsert failed for item ${item.id} (${item.name})", it) }
    }

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

            // 批量同步云端
            reportItems.forEach { supabaseDataRepository.insertReportItem(it.toSupabase()) }
            consumedList.forEach { supabaseDataRepository.deleteInventoryItem(it.id) }
        }
    }

    suspend fun cleanupExpiredItems() {
        val allItems = inventoryDao.getAllInventorySync()
        val today = Date()

        // Each item's local update + Supabase sync runs concurrently instead of one
        // item at a time, so this doesn't get linearly slower as the inventory grows -
        // most of the time here is spent waiting on the network, not the CPU.
        coroutineScope {
            allItems.forEach { item ->
                launch {
                    val expiryDate = DateFormats.parseExpiryOrNull(item.expiry) ?: return@launch
                    val diffInMillis = expiryDate.time - today.time
                    val days = TimeUnit.DAYS.convert(diffInMillis, TimeUnit.MILLISECONDS).toInt()

                    // Re-save dates in the current storage format so items created by an older
                    // app version (or a different screen) end up sorting correctly, without
                    // needing a manual data migration.
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

    suspend fun getExpiringItemNames(thresholdDays: Int): List<String> {
        return inventoryDao.getExpiringItemNames(thresholdDays)
    }

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
            // Additive only: fills in items that exist remotely but not yet locally
            // (e.g. restoring data after a reinstall or a first login on a new device).
            // It deliberately never overwrites an item that already exists locally -
            // Inventory has no last-modified timestamp to compare against, so a blind
            // overwrite here can't tell a stale remote copy from a genuine update, and
            // would silently revert a just-made local change (e.g. toggling "consumed")
            // the next time this sync runs, such as on the next app launch.
            val localIds = inventoryDao.getAllInventorySync().map { it.id }.toSet()
            val newItems = remoteItems.map { it.toRoom() }.filter { it.id !in localIds }
            newItems.forEach { inventoryDao.insertItem(it) }
        }
    }
}