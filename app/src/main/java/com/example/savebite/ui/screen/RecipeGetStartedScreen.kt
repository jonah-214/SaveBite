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

@Composable
fun RecipeGetStartedScreen(
    onCompleted: () -> Unit,
    viewModel: RecipeGetStartedViewModel = viewModel()
) {
    // Collect state from ViewModel
    val selectedDiet by viewModel.selectedDiet.collectAsState()
    val selectedAllergies by viewModel.selectedAllergies.collectAsState()

    // Pass state to the stateless content Composable
    RecipeGetStartedContent(
        selectedDiet = selectedDiet,
        selectedAllergies = selectedAllergies,
        onDietSelected = { viewModel.selectDiet(it) },
        onAllergyToggled = { viewModel.toggleAllergy(it) },
        onGetStarted = { viewModel.completeOnboarding(onCompleted) }
    )
}

@OptIn(ExperimentalLayoutApi::class, ExperimentalMaterial3Api::class)
@Composable
fun RecipeGetStartedContent(
    selectedDiet: String,
    selectedAllergies: Set<String>,
    onDietSelected: (String) -> Unit,
    onAllergyToggled: (String) -> Unit,
    onGetStarted: () -> Unit
) {
    val dietOptions = listOf(
        "None" to "No Preference",
        "Vegetarian" to "Vegetarian",
        "Vegan" to "Vegan",
        "Halal" to "Halal"
    )

    val allergyOptions = listOf(
        "Peanuts" to "Peanuts",
        "Seafood" to "Seafood / Shellfish",
        "Milk" to "Dairy & Lactose",
        "Eggs" to "Eggs",
        "Soy" to "Soy / Soybeans",
        "Gluten" to "Gluten"
    )

    Scaffold(
        bottomBar = {
            Surface(
                shadowElevation = 8.dp,
                color = MaterialTheme.colorScheme.surface
            ) {
                Button(
                    onClick = onGetStarted,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp, vertical = 16.dp)
                        .height(52.dp),
                    shape = MaterialTheme.shapes.medium
                ) {
                    Text(
                        text = "Get Started",
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
                    text = "Welcome Recipe Suggestion! ",
                    fontSize = 25.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )
                Text(
                    text = "Tailor your AI recipe recommendations by setting up your dietary preferences.",
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
                    dietOptions.forEach { (key, label) ->
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
                    allergyOptions.forEach { (key, label) ->
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
                Spacer(modifier = Modifier.height(32.dp))
            }
        }
    }
}
