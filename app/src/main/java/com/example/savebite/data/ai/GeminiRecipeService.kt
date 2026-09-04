package com.example.savebite.data.ai

import android.util.Log
import com.example.savebite.model.Inventory
import dev.shreyaspatil.ai.client.generativeai.GenerativeModel
import dev.shreyaspatil.ai.client.generativeai.type.content
import dev.shreyaspatil.ai.client.generativeai.type.generationConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.Serializable
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json

@Serializable
data class Recipe(
    val title: String,
    val prepTime: String,
    val usedExpiringIngredients: List<String>,
    val otherIngredients: List<String>,
    val steps: List<String>
)

// Handles calling the Google Gemini API to generate zero-waste recipe suggestions based on
// the user's expiring inventory, dietary preferences, allergies and household size.
class GeminiRecipeService(private val apiKey: String) {
    private val generativeModel by lazy {
        GenerativeModel(
            modelName = "gemini-3.6-flash",
            apiKey = apiKey.trim(),
            generationConfig = generationConfig {
                responseMimeType = "application/json"
            }
        )
    }

    // Requests 3 distinct recipes from Gemini prioritizing items nearing expiration.
    suspend fun generateRecipes(
        expiringItems: List<Inventory>,
        dietType: String = "None",
        allergies: Set<String> = emptySet(),
        householdType: String = "Student"
    ): List<Recipe> = withContext(Dispatchers.IO) {
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

        // Household Type -> a rough serving count, so recipe quantities feel right for
        // SaveBite's target users (students cooking solo vs. a family meal).
        val servings = when (householdType) {
            "Adult" -> 2
            "Family" -> 4
            else -> 1 // "Student" and any unrecognized value fall back to 1
        }

        val dietInstruction = if (dietType != "None") {
            "The user follows a $dietType diet — every recipe must be strictly $dietType."
        } else {
            ""
        }

        val allergyInstruction = if (allergies.isNotEmpty()) {
            "The user is allergic to: ${allergies.joinToString(", ")}. " +
                "Do not include any of these ingredients, or anything derived from them, in any recipe."
        } else {
            ""
        }

        val prompt = """
            You are a creative zero-waste chef.
            I have these expiring ingredients in my kitchen: $ingredientsPrompt.

            Please suggest 3 distinct recipes that prioritize using these expiring ingredients.
            Scale each recipe for about $servings serving(s).
            $dietInstruction
            $allergyInstruction

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
            // Call the official Gemini SDK to generate content asynchronously.
            val response = generativeModel.generateContent(prompt)
            val responseText = response.text

            if (responseText.isNullOrBlank()) {
                Log.e("GeminiRecipeService", "Empty response from Gemini SDK")
                return@withContext emptyList()
            }

            try {
                Json.decodeFromString<List<Recipe>>(cleanJsonString(responseText))
            } catch (e: Exception) {
                Log.e("GeminiRecipeService", "Error parsing JSON: ${e.message}", e)
                throw Exception("PARSING_ERROR")
            }
        } catch (e: Exception) {
            Log.e("GeminiRecipeService", "Error calling Gemini SDK: ${e.message}", e)
            val msg = e.message ?: ""
            when {
                // Covers SocketTimeoutException, UnknownHostException, etc. - i.e. the
                // request never reached Google's servers at all, as opposed to a server
                // responding with an error. Checked by type rather than message text
                // since these don't carry a consistent "network"-like wording.
                e is java.io.IOException -> throw Exception("NETWORK_ERROR: ${e.message}")
                msg.contains("API_KEY_INVALID", ignoreCase = true) ||
                        msg.contains("invalid", ignoreCase = true) -> throw Exception("INVALID_API_KEY: ${e.message}")
                msg.contains("Quota", ignoreCase = true) ||
                        msg.contains("429", ignoreCase = true) -> throw Exception("RATE_LIMIT_EXCEEDED")
                else -> throw Exception("SERVER_ERROR: ${e.message}")
            }
        }
    }

    // Helper method to remove code block formatting
    // around model responses despite JSON mode settings.
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
}