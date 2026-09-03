package com.example.savebite.ui.screen

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.savebite.R
import com.example.savebite.ui.viewmodel.RecipeGetStartedViewModel

// Shared option lists — reused by RecipeGetStartedScreen (first-run onboarding) and
// EditRecipePreferencesScreen (Profile & Settings, for changing preferences later).
val DIET_OPTIONS = listOf(
    "None" to "No Preference",
    "Vegetarian" to "Vegetarian",
    "Vegan" to "Vegan",
    "Halal" to "Halal"
)

val ALLERGY_OPTIONS = listOf(
    "Peanuts" to "Peanuts",
    "Seafood" to "Seafood / Shellfish",
    "Milk" to "Dairy & Lactose",
    "Eggs" to "Eggs",
    "Soy" to "Soy / Soybeans",
    "Gluten" to "Gluten"
)

// Mirrors SaveBite's own target audience (students, adults, families) so the option the
// user picks here reads as a direct answer to "who are you" rather than an arbitrary scale.
val HOUSEHOLD_OPTIONS = listOf(
    "Student" to "Student — cooking for myself",
    "Adult" to "Adult / Couple",
    "Family" to "Family with kids"
)

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
    buttonLabel: String = "Get Started",
    headerTitle: String = "Welcome Recipe Suggestion! ",
    headerSubtitle: String = "Tailor your AI recipe recommendations by setting up your dietary preferences."
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

            // --- Section 1: Dietary Preference (Single Select) ---
            item {
                Text(
                    text = "Dietary Preferences",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(bottom = 12.dp)
                )

                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    DIET_OPTIONS.forEach { (key, label) ->
                        val isSelected = (selectedDiet == key)
                        FilterChip(
                            selected = isSelected,
                            onClick = { onDietSelected(key) },
                            label = { Text(text = label, fontSize = 14.sp) },
                            leadingIcon = if (isSelected) {
                                {
                                    Icon(
                                        painter = painterResource(R.drawable.check),
                                        contentDescription = "Selected",
                                        modifier = Modifier.size(FilterChipDefaults.IconSize)
                                    )
                                }
                            } else null
                        )
                    }
                }
                Spacer(modifier = Modifier.height(28.dp))
            }

            // --- Section 2: Allergies & Intolerances (Multi-Select) ---
            item {
                Text(
                    text = "Allergies & Dietary Restrictions",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(bottom = 4.dp)
                )
                Text(
                    text = "Select all that apply:",
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(bottom = 12.dp)
                )

                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    ALLERGY_OPTIONS.forEach { (key, label) ->
                        val isChecked = selectedAllergies.contains(key)
                        FilterChip(
                            selected = isChecked,
                            onClick = { onAllergyToggled(key) },
                            label = { Text(text = label, fontSize = 14.sp) },
                            leadingIcon = if (isChecked) {
                                {
                                    Icon(
                                        painter = painterResource(R.drawable.check),
                                        contentDescription = "Selected",
                                        modifier = Modifier.size(FilterChipDefaults.IconSize)
                                    )
                                }
                            } else null
                        )
                    }
                }
                Spacer(modifier = Modifier.height(28.dp))
            }

            // --- Section 3: Household Type (Single Select) ---
            item {
                Text(
                    text = "Household Type",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(bottom = 4.dp)
                )
                Text(
                    text = "Helps us suggest the right serving size:",
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(bottom = 12.dp)
                )

                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    HOUSEHOLD_OPTIONS.forEach { (key, label) ->
                        val isSelected = (selectedHousehold == key)
                        FilterChip(
                            selected = isSelected,
                            onClick = { onHouseholdSelected(key) },
                            label = { Text(text = label, fontSize = 14.sp) },
                            leadingIcon = if (isSelected) {
                                {
                                    Icon(
                                        painter = painterResource(R.drawable.check),
                                        contentDescription = "Selected",
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
