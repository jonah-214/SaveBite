package com.example.savebite.ui.screen

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.savebite.R
import com.example.savebite.ui.viewmodel.RecipeGetStartedViewModel

// Shared option keys — reused by RecipeGetStartedScreen (first-run onboarding) and
// EditRecipePreferencesScreen (Profile & Settings, for changing preferences later).
// These keys are also the values stored in DataStore and sent to the Gemini prompt, so
// they stay in English regardless of locale — only the displayed label is localized,
// via dietOptionLabel()/allergyOptionLabel()/householdOptionLabel() below.
val DIET_OPTION_KEYS = listOf("None", "Vegetarian", "Vegan", "Halal")

val ALLERGY_OPTION_KEYS = listOf("Peanuts", "Seafood", "Milk", "Eggs", "Soy", "Gluten")

// Mirrors SaveBite's own target audience (students, adults, families) so the option the
// user picks here reads as a direct answer to "who are you" rather than an arbitrary scale.
val HOUSEHOLD_OPTION_KEYS = listOf("Student", "Adult", "Family")

@Composable
private fun dietOptionLabel(key: String): String = when (key) {
    "Vegetarian" -> stringResource(R.string.recipe_diet_vegetarian)
    "Vegan" -> stringResource(R.string.recipe_diet_vegan)
    "Halal" -> stringResource(R.string.recipe_diet_halal)
    else -> stringResource(R.string.recipe_diet_none)
}

@Composable
private fun allergyOptionLabel(key: String): String = when (key) {
    "Seafood" -> stringResource(R.string.recipe_allergy_seafood)
    "Milk" -> stringResource(R.string.recipe_allergy_milk)
    "Eggs" -> stringResource(R.string.recipe_allergy_eggs)
    "Soy" -> stringResource(R.string.recipe_allergy_soy)
    "Gluten" -> stringResource(R.string.recipe_allergy_gluten)
    else -> stringResource(R.string.recipe_allergy_peanuts)
}

@Composable
private fun householdOptionLabel(key: String): String = when (key) {
    "Adult" -> stringResource(R.string.recipe_household_adult)
    "Family" -> stringResource(R.string.recipe_household_family)
    else -> stringResource(R.string.recipe_household_student)
}

@Composable
fun RecipeGetStartedScreen(
    onCompleted: () -> Unit,
    viewModel: RecipeGetStartedViewModel = viewModel()
) {
    // Collect state from ViewModel
    val selectedDiet by viewModel.selectedDiet.collectAsState()
    val selectedAllergies by viewModel.selectedAllergies.collectAsState()
    val selectedHousehold by viewModel.selectedHousehold.collectAsState()

    // Pass state to the stateless content Composable
    RecipeGetStartedContent(
        selectedDiet = selectedDiet,
        selectedAllergies = selectedAllergies,
        selectedHousehold = selectedHousehold,
        onDietSelected = { viewModel.selectDiet(it) },
        onAllergyToggled = { viewModel.toggleAllergy(it) },
        onHouseholdSelected = { viewModel.selectHousehold(it) },
        onSubmit = { viewModel.completeOnboarding(onCompleted) }
    )
}

@OptIn(ExperimentalLayoutApi::class, ExperimentalMaterial3Api::class)
@Composable
fun RecipeGetStartedContent(
    selectedDiet: String,
    selectedAllergies: Set<String>,
    selectedHousehold: String,
    onDietSelected: (String) -> Unit,
    onAllergyToggled: (String) -> Unit,
    onHouseholdSelected: (String) -> Unit,
    onSubmit: () -> Unit,
    buttonLabel: String = stringResource(R.string.recipe_get_started_button),
    headerTitle: String = stringResource(R.string.recipe_get_started_header_title),
    headerSubtitle: String = stringResource(R.string.recipe_get_started_header_subtitle)
) {
    Scaffold(
        bottomBar = {
            Surface(
                shadowElevation = 8.dp,
                color = MaterialTheme.colorScheme.surface
            ) {
                Button(
                    onClick = onSubmit,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp, vertical = 16.dp)
                        .height(52.dp),
                    shape = MaterialTheme.shapes.medium
                ) {
                    Text(
                        text = buttonLabel,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
                .padding(horizontal = 24.dp)
        ) {
            item {
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = headerTitle,
                    fontSize = 25.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )
                Text(
                    text = headerSubtitle,
                    fontSize = 15.sp,
                    lineHeight = 22.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = 8.dp, bottom = 24.dp)
                )
            }

            // 1. Dietary Preference (Single Select)
            item {
                Text(
                    text = stringResource(R.string.recipe_pref_diet_title),
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(bottom = 12.dp)
                )

                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    DIET_OPTION_KEYS.forEach { key ->
                        val isSelected = (selectedDiet == key)
                        FilterChip(
                            selected = isSelected,
                            onClick = { onDietSelected(key) },
                            label = { Text(text = dietOptionLabel(key), fontSize = 14.sp) },
                            leadingIcon = if (isSelected) {
                                {
                                    Icon(
                                        painter = painterResource(R.drawable.check),
                                        contentDescription = stringResource(R.string.content_desc_selected),
                                        modifier = Modifier.size(FilterChipDefaults.IconSize)
                                    )
                                }
                            } else null
                        )
                    }
                }
                Spacer(modifier = Modifier.height(28.dp))
            }

            // 2. Allergies & Intolerances (Multi-Select)
            item {
                Text(
                    text = stringResource(R.string.recipe_pref_allergies_title),
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(bottom = 4.dp)
                )
                Text(
                    text = stringResource(R.string.recipe_pref_allergies_subtitle),
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(bottom = 12.dp)
                )

                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    ALLERGY_OPTION_KEYS.forEach { key ->
                        val isChecked = selectedAllergies.contains(key)
                        FilterChip(
                            selected = isChecked,
                            onClick = { onAllergyToggled(key) },
                            label = { Text(text = allergyOptionLabel(key), fontSize = 14.sp) },
                            leadingIcon = if (isChecked) {
                                {
                                    Icon(
                                        painter = painterResource(R.drawable.check),
                                        contentDescription = stringResource(R.string.content_desc_selected),
                                        modifier = Modifier.size(FilterChipDefaults.IconSize)
                                    )
                                }
                            } else null
                        )
                    }
                }
                Spacer(modifier = Modifier.height(28.dp))
            }

            // 3. Household Type (Single Select)
            item {
                Text(
                    text = stringResource(R.string.recipe_pref_household_title),
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(bottom = 4.dp)
                )
                Text(
                    text = stringResource(R.string.recipe_pref_household_subtitle),
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(bottom = 12.dp)
                )

                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    HOUSEHOLD_OPTION_KEYS.forEach { key ->
                        val isSelected = (selectedHousehold == key)
                        FilterChip(
                            selected = isSelected,
                            onClick = { onHouseholdSelected(key) },
                            label = { Text(text = householdOptionLabel(key), fontSize = 14.sp) },
                            leadingIcon = if (isSelected) {
                                {
                                    Icon(
                                        painter = painterResource(R.drawable.check),
                                        contentDescription = stringResource(R.string.content_desc_selected),
                                        modifier = Modifier.size(FilterChipDefaults.IconSize)
                                    )
                                }
                            } else null
                        )
                    }
                }
                Spacer(modifier = Modifier.height(32.dp))
            }
        }
    }
}
