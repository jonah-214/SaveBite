package com.example.savebite.data.repo

import com.example.savebite.data.local.dao.ShoppingDao
import com.example.savebite.data.remote.toRoom
import com.example.savebite.data.remote.toSupabase
import com.example.savebite.model.ShoppingItem
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

class ShoppingRepository(
    private val shoppingDao: ShoppingDao,
    private val supabaseDataRepository: SupabaseDataRepository
) {
    private val syncMutex = Mutex()

    val allShoppingItems: Flow<List<ShoppingItem>> = shoppingDao.getAllShoppingItems()

    suspend fun insertItem(item: ShoppingItem) = syncMutex.withLock {
        val itemToSave = item.copy(isSynced = false, isDeleted = false)
        shoppingDao.insertShoppingItem(itemToSave)

        val result = supabaseDataRepository.upsertShoppingItem(itemToSave.toSupabase())
        if (result.isSuccess) {
            shoppingDao.updateSyncStatus(itemToSave.id, itemToSave.isPurchased, true)
        }
    }

    suspend fun updateItem(item: ShoppingItem) = syncMutex.withLock {
        val itemToSave = item.copy(isSynced = false)
        shoppingDao.updateShoppingItem(itemToSave)

        val result = supabaseDataRepository.upsertShoppingItem(itemToSave.toSupabase())
        if (result.isSuccess) {
            shoppingDao.updateSyncStatus(itemToSave.id, itemToSave.isPurchased, true)
        }
    }

    suspend fun toggleItemPurchased(id: String) = syncMutex.withLock {
        // Fetch the most recent state directly from the DB inside the lock
        val currentItem = shoppingDao.getShoppingItemById(id) ?: return@withLock
        val updatedItem = currentItem.copy(isPurchased = !currentItem.isPurchased, isSynced = false)

        // Update local DB
        shoppingDao.updateShoppingItem(updatedItem)

        // Sync to cloud
        val result = supabaseDataRepository.upsertShoppingItem(updatedItem.toSupabase())
        if (result.isSuccess) {
            // Only mark as synced if the purchased state hasn't changed again locally
            shoppingDao.updateSyncStatus(updatedItem.id, updatedItem.isPurchased, true)
        }
    }

    suspend fun deleteItem(item: ShoppingItem) = syncMutex.withLock {
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
    suspend fun syncFromCloud(): Result<Unit> = syncMutex.withLock {
        runCatching {
            val rawLocalItems = shoppingDao.getAllShoppingItemsRaw()
            val justSyncedIds = mutableSetOf<String>()

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
                    shoppingDao.updateSyncStatus(item.id, item.isPurchased, true)
                    justSyncedIds.add(item.id)
                }
            }

            // 3. Fetch latest cloud snapshot and merge remote changes, preserving locally soft-deleted tombstones.
            val remoteResult = supabaseDataRepository.fetchShoppingItems()
            if (remoteResult.isSuccess) {
                val remoteItems = remoteResult.getOrThrow().map { it.toRoom() }

                // Refresh the local state to get the most up-to-date sync status
                // before deciding whether to overwrite with remote data.
                val currentLocalItems = shoppingDao.getAllShoppingItemsRaw()
                val localMap = currentLocalItems.associateBy { it.id }
                val deletedIds = currentLocalItems
                    .filter { it.isDeleted }
                    .map { it.id }
                    .toSet()

                remoteItems.forEach { remoteItem ->
                    // Skip items that were just deleted or just pushed to the cloud in this cycle,
                    // as the cloud fetch might still return stale data for them.
                    if (remoteItem.id !in deletedIds && remoteItem.id !in justSyncedIds) {
                        val localItem = localMap[remoteItem.id]

                        // Only overwrite if the local item is already synced or doesn't exist.
                        // This prevents cloud data from overwriting newer, unsynced local changes.
                        if (localItem == null || (localItem.isSynced && localItem != remoteItem.copy(isSynced = true))) {
                            shoppingDao.insertShoppingItem(remoteItem.copy(isSynced = true, isDeleted = false))
                        }
                    }
                }
            }
        }
    }
}