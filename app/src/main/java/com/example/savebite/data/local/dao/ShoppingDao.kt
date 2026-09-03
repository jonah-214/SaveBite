package com.example.savebite.data.local.dao

import androidx.room.*
import com.example.savebite.model.ShoppingItem
import kotlinx.coroutines.flow.Flow

@Dao
interface ShoppingDao {

    @Query("SELECT * FROM shopping_table WHERE isDeleted = 0")
    fun getAllShoppingItems(): Flow<List<ShoppingItem>>

    @Query("SELECT * FROM shopping_table WHERE id = :id LIMIT 1")
    suspend fun getShoppingItemById(id: String): ShoppingItem?

    @Query("SELECT * FROM shopping_table")
    suspend fun getAllShoppingItemsRaw(): List<ShoppingItem>

    @Query("DELETE FROM shopping_table WHERE isDeleted = 1 AND isSynced = 1")
    suspend fun purgeDeletedItems()

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertShoppingItem(item: ShoppingItem)

    @Update
    suspend fun updateShoppingItem(item: ShoppingItem)

    @Delete
    suspend fun deleteShoppingItem(item: ShoppingItem)

    @Query("UPDATE shopping_table SET isSynced = :isSynced WHERE id = :id AND isPurchased = :purchasedState")
    suspend fun updateSyncStatus(id: String, purchasedState: Boolean, isSynced: Boolean)

    @Query("DELETE FROM shopping_table WHERE isPurchased = 1")
    suspend fun deletePurchasedItems()
}