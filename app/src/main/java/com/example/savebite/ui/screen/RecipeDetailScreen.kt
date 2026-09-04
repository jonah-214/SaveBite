package com.example.savebite.ui.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.savebite.R
import com.example.savebite.data.ai.Recipe
import com.example.savebite.ui.navigation.AppTopBar
import com.example.savebite.ui.viewmodel.RecipeViewModel

// Shows the full recipe the AI generated — title, prep time, ingredients split into
// "from your expiring items" vs. "you'll also need", and the numbered cooking steps.
// Recipes aren't individually persisted with their own id (they're cached as one JSON
// blob for the whole list — see RecipeDao), so this screen is handed the recipe's
// position in that list rather than a stable id.
@Composable
fun RecipeDetailScreen(
    recipeIndex: Int,
    viewModel: RecipeViewModel,
    onBackClick: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val recipe = uiState.recipes.getOrNull(recipeIndex)

    Scaffold(
        topBar = {
            AppTopBar(
                title = recipe?.title ?: stringResource(R.string.recipe_detail_default_title),
                showBackButton = true,
                onBackClick = onBackClick
            )
        }
    ) { innerPadding ->
        if (recipe == null) {
            // Can happen if the cached recipe list changed (e.g. a fresh AI fetch
            // finished) between tapping the card and this screen reading it.
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    stringResource(R.string.recipe_detail_unavailable),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(horizontal = 32.dp)
                )
            }
        } else {
            RecipeDetailContent(
                recipe = recipe,
                modifier = Modifier.padding(innerPadding)
            )
        }
    }
}

@Composable
private fun RecipeDetailContent(recipe: Recipe, modifier: Modifier = Modifier) {
    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        contentPadding = PaddingValues(top = 16.dp, bottom = 32.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        // --- Header: prep time pill ---
        item {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    painter = painterResource(R.drawable.clock),
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    recipe.prepTime,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.width(16.dp))
                Surface(
                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        stringResource(R.string.recipe_difficulty_easy),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }
        }

        // Ingredients from your expiring items
        if (recipe.usedExpiringIngredients.isNotEmpty()) {
            item {
                SectionTitle(stringResource(R.string.recipe_detail_section_expiring))
            }
            items(recipe.usedExpiringIngredients) { ingredient ->
                IngredientRow(ingredient, highlighted = true)
            }
        }

        // Ingredients (everything else needed)
        if (recipe.otherIngredients.isNotEmpty()) {
            item {
                SectionTitle(stringResource(R.string.recipe_detail_section_other))
            }
            items(recipe.otherIngredients) { ingredient ->
                IngredientRow(ingredient, highlighted = false)
            }
        }

        // Step
        if (recipe.steps.isNotEmpty()) {
            item {
                SectionTitle(stringResource(R.string.recipe_detail_section_steps))
            }
            itemsIndexed(recipe.steps) { index, step ->
                StepRow(stepNumber = index + 1, text = step)
            }
        }
    }
}

@Composable
private fun SectionTitle(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.titleMedium,
        fontWeight = FontWeight.Bold,
        color = MaterialTheme.colorScheme.onBackground,
        modifier = Modifier.padding(bottom = 4.dp)
    )
}

@Composable
private fun IngredientRow(text: String, highlighted: Boolean) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier
                .size(8.dp)
                .clip(CircleShape)
                .background(
                    if (highlighted) MaterialTheme.colorScheme.primary
                    else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f)
                )
        )
        Spacer(modifier = Modifier.width(12.dp))
        Text(
            text,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}

@Composable
private fun StepRow(stepNumber: Int, text: String) {
    Row(modifier = Modifier.padding(vertical = 4.dp)) {
        Box(
            modifier = Modifier
                .size(26.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.primary),
            contentAlignment = Alignment.Center
        ) {
            Text(
                "$stepNumber",
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onPrimary
            )
        }
        Spacer(modifier = Modifier.width(12.dp))
        Text(
            text,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.padding(top = 2.dp)
        )
    }
}
