package com.example.savebite.data.ai

import android.util.Log
import com.example.savebite.model.Inventory
import com.google.ai.client.generativeai.GenerativeModel
import com.google.ai.client.generativeai.type.content
import com.google.ai.client.generativeai.type.generationConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.Serializable
import org.json.JSONArray
import org.json.JSONObject

@Serializable
data class Recipe(
    val title: String,
    val prepTime: String,
    val usedExpiringIngredients: List<String>,
    val otherIngredients: List<String>,
    val steps: List<String>
)

class GeminiRecipeService(private val apiKey: String) {

    // 使用官方 SDK 实例，配合 JSON Schema 输出配置
    private val generativeModel by lazy {
        GenerativeModel(
            modelName = "gemini-1.5-flash",
            apiKey = apiKey.trim(),
            generationConfig = generationConfig {
                responseMimeType = "application/json"
            }
        )
    }

    suspend fun generateRecipes(expiringItems: List<Inventory>): List<Recipe> = withContext(Dispatchers.IO) {
        if (apiKey.isBlank()) {
            Log.e("GeminiRecipeService", "API key is blank")
            throw Exception("MISSING_API_KEY")
        }
        if (expiringItems.isEmpty()) {
            return@withContext emptyList()
        }

        val ingredientsPrompt = expiringItems.joinToString(", ") {
            "${it.name} (${it.quantity} ${it.unit}, ${it.daysLeft} days left)"
        }

        val prompt = """
            You are a creative zero-waste chef. 
            I have these expiring ingredients in my kitchen: $ingredientsPrompt.
            
            Please suggest 3 distinct recipes that prioritize using these expiring ingredients.
            
            Return JSON in this exact structure:
            [
              {
                "title": "Recipe Name",
                "prepTime": "15 mins",
                "usedExpiringIngredients": ["Ingredient 1"],
                "otherIngredients": ["Salt", "Water"],
                "steps": ["Step 1", "Step 2"]
              }
            ]
        """.trimIndent()

        try {
            // 调用官方 SDK 生成文本
            val response = generativeModel.generateContent(prompt)
            val responseText = response.text

            if (responseText.isNullOrBlank()) {
                Log.e("GeminiRecipeService", "Empty response from Gemini SDK")
                return@withContext emptyList()
            }

            parseRecipesJson(cleanJsonString(responseText))
        } catch (e: Exception) {
            Log.e("GeminiRecipeService", "Error calling Gemini SDK: ${e.message}", e)
            val msg = e.message ?: ""
            when {
                msg.contains("API_KEY_INVALID", ignoreCase = true) ||
                        msg.contains("invalid", ignoreCase = true) -> throw Exception("INVALID_API_KEY: ${e.message}")
                msg.contains("Quota", ignoreCase = true) ||
                        msg.contains("429", ignoreCase = true) -> throw Exception("RATE_LIMIT_EXCEEDED")
                else -> throw Exception("SERVER_ERROR: ${e.message}")
            }
        }
    }

    private fun cleanJsonString(raw: String): String {
        var clean = raw.trim()
        if (clean.startsWith("```json")) {
            clean = clean.substringAfter("```json")
        } else if (clean.startsWith("```")) {
            clean = clean.substringAfter("```")
        }
        if (clean.endsWith("```")) {
            clean = clean.substringBeforeLast("```")
        }
        return clean.trim()
    }

    private fun parseRecipesJson(jsonStr: String): List<Recipe> {
        val recipeList = mutableListOf<Recipe>()
        try {
            val jsonArray = JSONArray(jsonStr)

            for (i in 0 until jsonArray.length()) {
                val obj = jsonArray.getJSONObject(i)

                val usedIngredients = mutableListOf<String>()
                val usedArray = obj.optJSONArray("usedExpiringIngredients")
                if (usedArray != null) {
                    for (j in 0 until usedArray.length()) usedIngredients.add(usedArray.getString(j))
                }

                val otherIngredients = mutableListOf<String>()
                val otherArray = obj.optJSONArray("otherIngredients")
                if (otherArray != null) {
                    for (j in 0 until otherArray.length()) otherIngredients.add(otherArray.getString(j))
                }

                val steps = mutableListOf<String>()
                val stepsArray = obj.optJSONArray("steps")
                if (stepsArray != null) {
                    for (j in 0 until stepsArray.length()) steps.add(stepsArray.getString(j))
                }

                recipeList.add(
                    Recipe(
                        title = obj.optString("title", "Delicious Recipe"),
                        prepTime = obj.optString("prepTime", "15 mins"),
                        usedExpiringIngredients = usedIngredients,
                        otherIngredients = otherIngredients,
                        steps = steps
                    )
                )
            }
        } catch (e: Exception) {
            Log.e("GeminiRecipeService", "Error parsing JSON: ${e.message}", e)
            throw Exception("PARSING_ERROR")
        }
        return recipeList
    }
}