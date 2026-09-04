package com.example.savebite.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.savebite.model.Inventory
import kotlinx.coroutines.flow.Flow

@Dao
interface InventoryDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertItem(item: Inventory)

    @Query("SELECT * FROM inventory_table ORDER BY daysLeft ASC")
    fun getAllInventory(): Flow<List<Inventory>>

    @Query("SELECT * FROM inventory_table")
    suspend fun getAllInventorySync(): List<Inventory>

    @Query("SELECT * FROM inventory_table WHERE storage = :storage")
    suspend fun getItemsByStorageSync(storage: String): List<Inventory>

    @Query("SELECT * FROM inventory_table WHERE id = :id")
    fun getInventoryById(id: String): Flow<Inventory?>

    @Query("SELECT name FROM inventory_table WHERE daysLeft <= :thresholdDays")
    suspend fun getExpiringItemNames(thresholdDays: Int): List<String>

    // Performs live full-text search and storage filtering simultaneously.
    // Matches queries against item names and descriptions, returning results ordered by expiration.
    @Query("""
        SELECT * FROM inventory_table 
        WHERE (:storage = 'All' OR storage = :storage)
        AND (name LIKE '%' || :searchQuery || '%' OR description LIKE '%' || :searchQuery || '%')
        ORDER BY daysLeft ASC
    """)
    fun searchAndFilterInventory(searchQuery: String, storage: String): Flow<List<Inventory>>

    @Update
    suspend fun updateItem(item: Inventory)

    @Delete
    suspend fun deleteItem(item: Inventory)

    // Bulk-reassigns items from one storage location to another.
    // Used when a user renames or deletes a custom storage location (e.g., migrating "Freezer 1" items to "Refrigerator").
    @Query("UPDATE inventory_table SET storage = :newStorage WHERE storage = :oldStorage")
    suspend fun reassignStorage(oldStorage: String, newStorage: String)

    // Retrieves all items flagged as consumed for food waste reduction metrics and analytics reporting.
    @Query("SELECT * FROM inventory_table WHERE isConsumed = 1")
    suspend fun getConsumedItems(): List<Inventory>

    // Bulk deletes all consumed items to clean up local table size after history sync or periodic maintenance.
    @Query("DELETE FROM inventory_table WHERE isConsumed = 1")
    suspend fun deleteConsumedItems()
}
