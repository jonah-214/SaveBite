package com.example.savebite.data.repo

import com.example.savebite.data.local.dao.InventoryDao
import com.example.savebite.data.local.dao.StorageDao
import com.example.savebite.data.local.dao.ReportDao
import com.example.savebite.model.Inventory
import com.example.savebite.model.Storage
import com.example.savebite.model.ReportItem
import com.example.savebite.model.ReportStatus
import com.example.savebite.model.ReportStatus.WASTED
import kotlinx.coroutines.flow.Flow
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.concurrent.TimeUnit

class InventoryRepository(
    private val inventoryDao: InventoryDao,
    private val storageDao: StorageDao,
    private val reportDao: ReportDao
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
        this@InventoryRepository.reportDao.insertReportItem(reportItem)
        inventoryDao.deleteItem(item)
    }

    suspend fun toggleConsumed(item: Inventory) {
        val updated = item.copy(isConsumed = !item.isConsumed)
        inventoryDao.updateItem(updated)
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
            this@InventoryRepository.reportDao.insertReportItems(reportItems) // 批量插入
            inventoryDao.deleteConsumedItems()
        }
    }

    suspend fun cleanupExpiredItems() {
        val allItems = inventoryDao.getAllInventorySync()
        val today = Date()
        val formatter = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())

        allItems.forEach { item ->
            try {
                val expiryDate = formatter.parse(item.expiry)
                if (expiryDate != null) {
                    val diffInMillis = expiryDate.time - today.time
                    val days = TimeUnit.DAYS.convert(diffInMillis, TimeUnit.MILLISECONDS).toInt()

                    if (days < 0) {
                        // Expired
                        markAsWaste(item, "Expired")
                    } else {
                        // Update daysLeft if it changed
                        val newDaysLeft = days + 1
                        if (item.daysLeft != newDaysLeft) {
                            inventoryDao.updateItem(item.copy(daysLeft = newDaysLeft))
                        }
                    }
                }
            } catch (e: Exception) {
                // Ignore parsing errors
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
                    reason = if (status == ReportStatus.WASTED) reason else "Consumed"
                )
                reportDao.insertReportItem(reportItem)

                val remainingQty = item.quantity - moveQty
                if (remainingQty <= 0) {
                    inventoryDao.deleteItem(item)
                } else {
                    // Update the remaining items' total price proportionally
                    val newTotalPrice = (remainingQty.toDouble() / item.quantity.toDouble()) * item.price
                    
                    inventoryDao.updateItem(
                        item.copy(
                            quantity = remainingQty, 
                            price = newTotalPrice,
                            isConsumed = false
                        )
                    )
                }
            }
        }
    }
}
