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

class RecipeRepository(
    private val aiService: GeminiRecipeService,
    private val recipeDao: RecipeDao
) {
    // 监听本地缓存 Flow
    val cachedRecipes: Flow<List<Recipe>> = recipeDao.getCachedRecipes().map { entity ->
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
    suspend fun fetchAndSaveRecipes(
        expiringItems: List<Inventory>,
        dietType: String = "None",
        allergies: Set<String> = emptySet(),
        householdType: String = "Student"
    ): List<Recipe> {
        val newRecipes = aiService.generateRecipes(expiringItems, dietType, allergies, householdType)
        if (newRecipes.isNotEmpty()) {
            val jsonString = Json.encodeToString(newRecipes)
            recipeDao.insertRecipes(RecipeEntity(jsonContent = jsonString))
        }
        return newRecipes
    }
}