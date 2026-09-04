package com.example.savebite.data.repo

import com.example.savebite.data.local.dao.ShoppingDao
import com.example.savebite.data.remote.toRoom
import com.example.savebite.data.remote.toSupabase
import com.example.savebite.model.ShoppingItem
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first

class ShoppingRepository(
    private val shoppingDao: ShoppingDao,
    private val supabaseDataRepository: SupabaseDataRepository
) {
    val allShoppingItems: Flow<List<ShoppingItem>> = shoppingDao.getAllShoppingItems()

    suspend fun insertItem(item: ShoppingItem) {
        val itemToSave = item.copy(isSynced = false, isDeleted = false)
        shoppingDao.insertShoppingItem(itemToSave)

        val result = supabaseDataRepository.upsertShoppingItem(itemToSave.toSupabase())
        if (result.isSuccess) {
            shoppingDao.updateShoppingItem(itemToSave.copy(isSynced = true))
        }
    }

    suspend fun updateItem(item: ShoppingItem) {
        val itemToSave = item.copy(isSynced = false)
        shoppingDao.updateShoppingItem(itemToSave)

        val result = supabaseDataRepository.upsertShoppingItem(itemToSave.toSupabase())
        if (result.isSuccess) {
            shoppingDao.updateShoppingItem(itemToSave.copy(isSynced = true))
        }
    }

    suspend fun deleteItem(item: ShoppingItem) {
        val itemToDelete = item.copy(isDeleted = true, isSynced = false)
        shoppingDao.updateShoppingItem(itemToDelete)

        val result = supabaseDataRepository.deleteShoppingItem(item.id)
        if (result.isSuccess) {
            shoppingDao.deleteShoppingItem(itemToDelete)
        }
    }

    suspend fun clearPurchasedItems() {
        val currentItems = allShoppingItems.first()
        val purchased = currentItems.filter { it.isPurchased }

        purchased.forEach { deleteItem(it) }
    }

    // Two-way synchronization
    suspend fun syncFromCloud(): Result<Unit> {
        return runCatching {
            val rawLocalItems = shoppingDao.getAllShoppingItemsRaw()

            // 1.Flush pending local deletions to cloud first to prevent remote items from re-appearing.
            val pendingDeletes = rawLocalItems.filter { it.isDeleted && !it.isSynced }
            for (deletedItem in pendingDeletes) {
                val delResult = supabaseDataRepository.deleteShoppingItem(deletedItem.id)
                if (delResult.isSuccess) {
                    shoppingDao.deleteShoppingItem(deletedItem)
                }
            }

            // 2. Push pending local inserts/updates created while offline.
            val pendingUpserts = rawLocalItems.filter { !it.isDeleted && !it.isSynced }
            for (item in pendingUpserts) {
                val upResult = supabaseDataRepository.upsertShoppingItem(item.toSupabase())
                if (upResult.isSuccess) {
                    shoppingDao.updateShoppingItem(item.copy(isSynced = true))
                }
            }

            // 3. Fetch latest cloud snapshot and merge remote changes, preserving locally soft-deleted tombstones.
            val remoteResult = supabaseDataRepository.fetchShoppingItems()
            if (remoteResult.isSuccess) {
                val remoteItems = remoteResult.getOrThrow().map { it.toRoom() }

                val deletedIds = shoppingDao.getAllShoppingItemsRaw()
                    .filter { it.isDeleted }
                    .map { it.id }
                    .toSet()

                remoteItems.forEach { remoteItem ->
                    if (remoteItem.id !in deletedIds) {
                        shoppingDao.insertShoppingItem(remoteItem.copy(isSynced = true, isDeleted = false))
                    }
                }
            }
        }
    }
}