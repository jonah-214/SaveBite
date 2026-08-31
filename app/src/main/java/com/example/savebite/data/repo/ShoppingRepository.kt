package com.example.savebite.data.repo

import com.example.savebite.data.local.dao.ShoppingDao
import com.example.savebite.data.remote.toRoom
import com.example.savebite.data.remote.toSupabase
import com.example.savebite.model.ShoppingItem
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first

class ShoppingRepository(
    private val shoppingDao: ShoppingDao,
    private val supabaseDataRepository: SupabaseDataRepository = SupabaseDataRepository()
) {
    val allShoppingItems: Flow<List<ShoppingItem>> = shoppingDao.getAllShoppingItems()

    suspend fun insertItem(item: ShoppingItem) {
        shoppingDao.insertShoppingItem(item)
        supabaseDataRepository.upsertShoppingItem(item.toSupabase())
    }

    suspend fun updateItem(item: ShoppingItem) {
        shoppingDao.updateShoppingItem(item)
        supabaseDataRepository.upsertShoppingItem(item.toSupabase())
    }

    suspend fun deleteItem(item: ShoppingItem) {
        shoppingDao.deleteShoppingItem(item)
        supabaseDataRepository.deleteShoppingItem(item.id)
    }

    suspend fun clearPurchasedItems() {
        val currentItems = allShoppingItems.first()
        val purchased = currentItems.filter { it.isPurchased }

        shoppingDao.deletePurchasedItems()

        purchased.forEach { item ->
            supabaseDataRepository.deleteShoppingItem(item.id)
        }
    }

    suspend fun syncFromCloud(): Result<Unit> {
        val remoteResult = supabaseDataRepository.fetchShoppingItems()
        return if (remoteResult.isSuccess) {
            val remoteItems = remoteResult.getOrDefault(emptyList())
            remoteItems.forEach { supabaseItem ->
                shoppingDao.insertShoppingItem(supabaseItem.toRoom())
            }
            Result.success(Unit)
        } else {
            Result.failure(remoteResult.exceptionOrNull() ?: Exception("Sync failed"))
        }
    }
}