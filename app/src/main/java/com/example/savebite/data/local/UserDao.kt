package com.example.savebite.data.local

import androidx.room3.Dao
import androidx.room3.Delete
import androidx.room3.Insert
import androidx.room3.OnConflictStrategy
import androidx.room3.Query
import androidx.room3.Update
import com.example.savebite.model.Inventory
import kotlinx.coroutines.flow.Flow

@Dao
interface InventoryDao {

    // --- INSERT / CREATE ---

    /**
     * Inserts a new inventory item or replaces an existing one if the String UUID matches.
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertItem(item: Inventory)

    /**
     * Inserts a list of inventory items in batch.
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(items: List<Inventory>)


    // --- QUERY / READ ---

    /**
     * Retrieves all items sorted by items expiring soonest first.
     * Returns a reactive Flow so UI updates automatically.
     */
    @Query("SELECT * FROM inventory_table ORDER BY daysLeft ASC")
    fun getAllInventory(): Flow<List<Inventory>>

    /**
     * Fetches a specific inventory item by its String UUID.
     */
    @Query("SELECT * FROM inventory_table WHERE id = :id")
    fun getInventoryById(id: String): Flow<Inventory?>

    /**
     * Filters inventory by storage location (e.g. "Refrigerator", "Pantry", "Freezer").
     */
    @Query("SELECT * FROM inventory_table WHERE storage = :storageLocation ORDER BY daysLeft ASC")
    fun getInventoryByStorage(storageLocation: String): Flow<List<Inventory>>

    /**
     * Filter by category tab (e.g. "Dairy", "Produce").
     */
    @Query("SELECT * FROM inventory_table WHERE category = :category ORDER BY daysLeft ASC")
    fun getInventoryByCategory(category: String): Flow<List<Inventory>>

    /**
     * Searches items by name or description while matching storage/category selection.
     */
    @Query("""
        SELECT * FROM inventory_table 
        WHERE (:storage = 'All' OR storage LIKE :storage)
        AND (name LIKE '%' || :searchQuery || '%' OR description LIKE '%' || :searchQuery || '%')
        ORDER BY daysLeft ASC
    """)
    fun searchAndFilterInventory(searchQuery: String, storage: String): Flow<List<Inventory>>


    // --- UPDATE ---

    /**
     * Updates an existing inventory item in the database.
     */
    @Update
    suspend fun updateItem(item: Inventory)


    // --- DELETE ---

    /**
     * Deletes a given inventory item.
     */
    @Delete
    suspend fun deleteItem(item: Inventory)

    /**
     * Deletes an item directly by its String UUID.
     */
    @Query("DELETE FROM inventory_table WHERE id = :id")
    suspend fun deleteById(id: String)

    /**
     * Clears all items from the table.
     */
    @Query("DELETE FROM inventory_table")
    suspend fun deleteAll()
}