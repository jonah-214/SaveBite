package com.example.savebite.data.repo

import com.example.savebite.data.ai.GeminiRecipeService
import com.example.savebite.data.ai.Recipe
import com.example.savebite.data.local.dao.RecipeDao
import com.example.savebite.data.local.dao.RecipeEntity
import com.example.savebite.model.Inventory
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

interface RecipeRepository {
    // Recipes cached for one specific user (see RecipeDao - cache is keyed by userId).
    fun cachedRecipes(userId: Int): Flow<List<Recipe>>

    suspend fun fetchAndSaveRecipes(
        userId: Int,
        expiringItems: List<Inventory>,
        dietType: String = "None",
        allergies: Set<String> = emptySet(),
        householdType: String = "Student"
    ): List<Recipe>
}

class RecipeRepositoryImpl(
    private val aiService: GeminiRecipeService,
    private val recipeDao: RecipeDao
) : RecipeRepository {

    // 监听本地缓存 Flow
    override fun cachedRecipes(userId: Int): Flow<List<Recipe>> =
        recipeDao.getCachedRecipes(userId).map { entity ->
            if (entity != null) {
                try {
                    Json.decodeFromString<List<Recipe>>(entity.jsonContent)
                } catch (e: Exception) {
                    emptyList()
                }
            } else {
                emptyList()
            }
        }

    // 从 API 获取新数据并更新本地数据库
    override suspend fun fetchAndSaveRecipes(
        userId: Int,
        expiringItems: List<Inventory>,
        dietType: String,
        allergies: Set<String>,
        householdType: String
    ): List<Recipe> {
        val newRecipes = aiService.generateRecipes(expiringItems, dietType, allergies, householdType)
        if (newRecipes.isNotEmpty()) {
            val jsonString = Json.encodeToString(newRecipes)
            recipeDao.insertRecipes(RecipeEntity(id = userId, jsonContent = jsonString))
        }
        return newRecipes
    }
}
