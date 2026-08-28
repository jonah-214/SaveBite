package com.example.savebite.data.ai

import android.util.Log
import com.example.savebite.model.Inventory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.Serializable
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.io.IOException
import java.util.concurrent.TimeUnit

@Serializable
data class Recipe(
    val title: String,
    val prepTime: String,
    val usedExpiringIngredients: List<String>,
    val otherIngredients: List<String>,
    val steps: List<String>
)

class GeminiRecipeService(private val apiKey: String) {

    // 使用官方稳定的模型名称
    private val modelName = "gemini-1.5-flash"

    private val client = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .build()

    private val jsonMediaType = "application/json; charset=utf-8".toMediaType()

    suspend fun generateRecipes(expiringItems: List<Inventory>): List<Recipe> = withContext(Dispatchers.IO) {
        if (apiKey.isBlank()) {
            Log.e("GeminiRecipeService", "API key is blank")
            return@withContext emptyList()
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

        val requestBodyJson = JSONObject().apply {
            put("contents", JSONArray().put(
                JSONObject().put("parts", JSONArray().put(
                    JSONObject().put("text", prompt)
                ))
            ))
            put("generationConfig", JSONObject().put("response_mime_type", "application/json"))
        }.toString()

        val url = "https://generativelanguage.googleapis.com/v1beta/models/$modelName:generateContent"

        val request = Request.Builder()
            .url(url)
            .addHeader("x-goog-api-key", apiKey)
            .addHeader("Content-Type", "application/json")
            .post(requestBodyJson.toRequestBody(jsonMediaType))
            .build()

        try {
            client.newCall(request).execute().use { response ->
                val bodyStr = response.body?.string().orEmpty()

                if (!response.isSuccessful) {
                    Log.e("GeminiRecipeService", "Gemini request failed: HTTP ${response.code} - $bodyStr")
                    return@withContext emptyList()
                }

                if (bodyStr.isEmpty()) {
                    Log.e("GeminiRecipeService", "Empty response body from Gemini")
                    return@withContext emptyList()
                }

                val text = extractTextFromResponse(bodyStr)
                if (text.isNullOrBlank()) {
                    Log.e("GeminiRecipeService", "No text content in Gemini response: $bodyStr")
                    return@withContext emptyList()
                }

                parseRecipesJson(cleanJsonString(text))
            }
        } catch (e: IOException) {
            Log.e("GeminiRecipeService", "Network error calling Gemini: ${e.message}", e)
            emptyList()
        } catch (e: Exception) {
            Log.e("GeminiRecipeService", "Error generating recipes: ${e.message}", e)
            emptyList()
        }
    }

    private fun extractTextFromResponse(bodyStr: String): String? {
        return try {
            val root = JSONObject(bodyStr)
            root.getJSONArray("candidates")
                .getJSONObject(0)
                .getJSONObject("content")
                .getJSONArray("parts")
                .getJSONObject(0)
                .optString("text", null)
        } catch (e: Exception) {
            Log.e("GeminiRecipeService", "Error extracting text from response: ${e.message}", e)
            null
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
        }
        return recipeList
    }
}