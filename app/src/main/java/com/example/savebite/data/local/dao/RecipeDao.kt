package com.example.savebite.data.local.dao

import androidx.room.Dao
import androidx.room.Entity
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.PrimaryKey
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

// id holds the owning user's id (not a fixed constant) so each account gets its own
// cached recipe list - see RecipeRepositoryImpl. Kept as plain Int (no rename, no type
// change) since AppDatabase uses fallbackToDestructiveMigration(): a real schema change
// here would force a version bump that wipes the *entire* local database on upgrade,
// not just this table.
@Entity(tableName = "cached_recipes")
data class RecipeEntity(
    @PrimaryKey val id: Int,
    val jsonContent: String,
    val updatedAt: Long = System.currentTimeMillis()
)

@Dao
interface RecipeDao {
    @Query("SELECT * FROM cached_recipes WHERE id = :userId")
    fun getCachedRecipes(userId: Int): Flow<RecipeEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRecipes(recipeEntity: RecipeEntity)

    @Query("DELETE FROM cached_recipes WHERE id = :userId")
    suspend fun clearCache(userId: Int)
}
