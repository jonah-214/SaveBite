package com.example.savebite.data.local.dao

import androidx.room.*
import com.example.savebite.data.ai.Recipe
import kotlinx.coroutines.flow.Flow
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

@Entity(tableName = "cached_recipes")
data class RecipeEntity(
    @PrimaryKey val id: Int = 1,
    val jsonContent: String, // 将 List<Recipe> 序列化为 JSON 字符串保存
    val updatedAt: Long = System.currentTimeMillis()
)

@Dao
interface RecipeDao {
    @Query("SELECT * FROM cached_recipes WHERE id = 1")
    fun getCachedRecipes(): Flow<RecipeEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRecipes(recipeEntity: RecipeEntity)

    @Query("DELETE FROM cached_recipes")
    suspend fun clearCache()
}