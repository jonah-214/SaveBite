package com.example.savebite.data.repo

import com.example.savebite.data.local.dao.ShoppingDao
import com.example.savebite.model.ShoppingItem
import kotlinx.coroutines.flow.Flow

class ShoppingRepository(private val shoppingDao: ShoppingDao) {

    val allShoppingItems: Flow<List<ShoppingItem>> = shoppingDao.getAllShoppingItems()

    suspend fun insertItem(item: ShoppingItem) {
        shoppingDao.insertShoppingItem(item)
    }

    suspend fun updateItem(item: ShoppingItem) {
        shoppingDao.updateShoppingItem(item)
    }

    suspend fun deleteItem(item: ShoppingItem) {
        shoppingDao.deleteShoppingItem(item)
    }

    suspend fun clearPurchasedItems() {
        shoppingDao.deletePurchasedItems()
    }
}