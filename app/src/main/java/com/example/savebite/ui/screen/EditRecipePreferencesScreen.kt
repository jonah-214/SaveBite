package com.example.savebite.ui.screen

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import com.example.savebite.R
import com.example.savebite.ui.navigation.AppTopBar
import com.example.savebite.ui.viewmodel.EditRecipePreferencesViewModel

// Lets the user change the Diet / Allergies / Household Type they picked on the Recipe
// "Get Started" screen, from Profile & Settings — reuses RecipeGetStartedContent so the
// two screens always look and behave the same.
@Composable
fun EditRecipePreferencesScreen(
    navController: NavHostController,
    viewModel: EditRecipePreferencesViewModel = viewModel()
) {
    val selectedDiet by viewModel.selectedDiet.collectAsState()
    val selectedAllergies by viewModel.selectedAllergies.collectAsState()
    val selectedHousehold by viewModel.selectedHousehold.collectAsState()
    val isLoaded by viewModel.isLoaded.collectAsState()

    Scaffold(
        topBar = {
            AppTopBar(
                title = stringResource(R.string.edit_recipe_preferences_title),
                showBackButton = true,
                onBackClick = { navController.popBackStack() }
            )
        }
    ) { innerPadding ->
        if (!isLoaded) {
            // Avoids a one-frame flash of the defaults before the saved values load in.
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        } else {
            Box(modifier = Modifier.padding(innerPadding)) {
                RecipeGetStartedContent(
                    selectedDiet = selectedDiet,
                    selectedAllergies = selectedAllergies,
                    selectedHousehold = selectedHousehold,
                    onDietSelected = { viewModel.selectDiet(it) },
                    onAllergyToggled = { viewModel.toggleAllergy(it) },
                    onHouseholdSelected = { viewModel.selectHousehold(it) },
                    onSubmit = { viewModel.savePreferences { navController.popBackStack() } },
                    buttonLabel = stringResource(R.string.edit_recipe_preferences_save),
                    headerTitle = stringResource(R.string.edit_recipe_preferences_header_title),
                    headerSubtitle = stringResource(R.string.edit_recipe_preferences_header_subtitle)
                )
            }
        }
    }
}
