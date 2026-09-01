package com.example.savebite.ui.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.example.savebite.R
import com.example.savebite.utils.ExpiryGrouping
import com.example.savebite.utils.ExpirySection
import com.example.savebite.model.Inventory
import com.example.savebite.ui.navigation.AppRoutes
import com.example.savebite.ui.navigation.AppSearchBar
import com.example.savebite.ui.navigation.AppTopBar
import com.example.savebite.ui.theme.SaveBiteTheme
import com.example.savebite.ui.viewmodel.ReminderViewModel


@Composable
fun ReminderScreen(
    navController: NavHostController,
    reminderViewModel: ReminderViewModel
) {
    val grouped by reminderViewModel.groupedItems.collectAsState()
    val searchQuery by reminderViewModel.searchQuery.collectAsState()
    val selectedStorage by reminderViewModel.selectedStorage.collectAsState()
    val storageOptions by reminderViewModel.storageOptions.collectAsState()
    val totalItemCount by reminderViewModel.totalItemCount.collectAsState()

    ReminderScreenContent(
        grouped = grouped,
        searchQuery = searchQuery,
        onSearchQueryChange = reminderViewModel::onSearchQueryChange,
        storageOptions = storageOptions,
        selectedStorage = selectedStorage,
        onStorageSelected = reminderViewModel::onStorageFilterSelected,
        hasAnyInventory = totalItemCount > 0,
        onBackClick = { navController.popBackStack() },
        onItemClick = { item ->
            navController.navigate("${AppRoutes.INVENTORY_DETAILS}/${item.id}")
        },
        onRecipeClick = {
            // RecipeScreen doesn't yet support pre-filtering by a single
            // ingredient — for now this just opens the Recipe tab.
            navController.navigate(AppRoutes.RECIPE) {
                launchSingleTop = true
            }
        }
    )
}

@Composable
fun ReminderScreenContent(
    grouped: Map<ExpirySection, List<Inventory>>,
    searchQuery: String,
    onSearchQueryChange: (String) -> Unit,
    storageOptions: List<String>,
    selectedStorage: String?,
    onStorageSelected: (String?) -> Unit,
    hasAnyInventory: Boolean,
    onBackClick: () -> Unit = {},
    onItemClick: (Inventory) -> Unit = {},
    onRecipeClick: (Inventory) -> Unit = {}
) {
    val allStorageLabel = "All Storages"
    val sortOptions = listOf(allStorageLabel) + storageOptions

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            AppTopBar(
                title = "Expiry Reminder",
                showBackButton = true,
                onBackClick = onBackClick
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .padding(horizontal = 16.dp)
        ) {
            Spacer(modifier = Modifier.height(12.dp))

            AppSearchBar(
                query = searchQuery,
                onQueryChange = onSearchQueryChange,
                placeholderText = "Search by name or category...",
                sortOptions = sortOptions,
                selectedSortOption = selectedStorage ?: allStorageLabel,
                onSortOptionSelected = { option ->
                    onStorageSelected(if (option == allStorageLabel) null else option)
                },
                getSortOptionLabel = { it }
            )

            Spacer(modifier = Modifier.height(12.dp))

            if (grouped.isEmpty()) {
                EmptyReminderState(
                    hasAnyInventory = hasAnyInventory,
                    modifier = Modifier.fillMaxSize()
                )
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(bottom = 16.dp)
                ) {
                    item { SummaryRow(grouped) }
                    item { Spacer(modifier = Modifier.height(16.dp)) }

                    ExpirySection.entries.forEach { section ->
                        val sectionItems = grouped[section].orEmpty()
                        if (sectionItems.isNotEmpty()) {
                            item(key = "header_${section.name}") {
                                SectionHeaderText(section)
                            }
                            items(sectionItems, key = { it.id }) { inventoryItem ->
                                ReminderCard(
                                    item = inventoryItem,
                                    onClick = { onItemClick(inventoryItem) },
                                    onRecipeClick = { onRecipeClick(inventoryItem) }
                                )
                                Spacer(modifier = Modifier.height(10.dp))
                            }
                            item(key = "spacer_${section.name}") {
                                Spacer(modifier = Modifier.height(8.dp))
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun SummaryRow(grouped: Map<ExpirySection, List<Inventory>>) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        ExpirySection.entries.forEach { section ->
            val count = grouped[section]?.size ?: 0
            val (bg, fg) = colorsForSection(section)
            Surface(
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(12.dp),
                color = bg
            ) {
                Column(
                    modifier = Modifier.padding(vertical = 10.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "$count",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = fg
                    )
                    Text(
                        text = section.label,
                        fontSize = 11.sp,
                        color = fg,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }
    }
}

@Composable
private fun SectionHeaderText(section: ExpirySection) {
    Text(
        text = section.label,
        fontSize = 16.sp,
        fontWeight = FontWeight.Bold,
        color = MaterialTheme.colorScheme.onBackground,
        modifier = Modifier.padding(top = 12.dp, bottom = 8.dp)
    )
}

@Composable
private fun colorsForSection(section: ExpirySection): Pair<androidx.compose.ui.graphics.Color, androidx.compose.ui.graphics.Color> {
    return when (section) {
        ExpirySection.TODAY -> MaterialTheme.colorScheme.errorContainer to MaterialTheme.colorScheme.onErrorContainer
        ExpirySection.THIS_WEEK -> MaterialTheme.colorScheme.tertiaryContainer to MaterialTheme.colorScheme.onTertiaryContainer
        ExpirySection.LATER -> MaterialTheme.colorScheme.primaryContainer to MaterialTheme.colorScheme.onPrimaryContainer
    }
}

@Composable
private fun ReminderCard(
    item: Inventory,
    onClick: () -> Unit,
    onRecipeClick: () -> Unit
) {
    val section = ExpiryGrouping.sectionFor(item.daysLeft)
    val (badgeBg, badgeFg) = colorsForSection(section)
    val accentColor = badgeFg

    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(14.dp),
        color = MaterialTheme.colorScheme.surfaceContainerLow,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.surfaceContainerLow)
                .padding(start = 4.dp)
        ) {
            // Left accent bar communicates urgency at a glance
            Box(
                modifier = Modifier
                    .width(4.dp)
                    .fillMaxWidth()
                    .background(accentColor, RoundedCornerShape(2.dp))
            )

            Row(
                modifier = Modifier
                    .weight(1f)
                    .padding(start = 8.dp, top = 12.dp, bottom = 12.dp, end = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Category initial badge (no per-food icon set yet)
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .background(badgeBg, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = item.name.take(1).uppercase(),
                        color = badgeFg,
                        fontWeight = FontWeight.Bold
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "${item.name} • ${item.quantity} ${item.unit}",
                        fontWeight = FontWeight.SemiBold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = item.category,
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Spacer(modifier = Modifier.width(8.dp))

                Text(
                    text = if (item.daysLeft <= 0) "Today" else "${item.daysLeft} days",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium,
                    color = badgeFg,
                    modifier = Modifier
                        .background(badgeBg, RoundedCornerShape(50))
                        .padding(horizontal = 10.dp, vertical = 4.dp)
                )

                Spacer(modifier = Modifier.width(4.dp))

                IconButton(onClick = onRecipeClick) {
                    Icon(
                        painter = painterResource(id = R.drawable.chef_hat),
                        contentDescription = "See recipes using ${item.name}",
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }
    }
}

@Composable
private fun EmptyReminderState(hasAnyInventory: Boolean, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            painter = painterResource(id = R.drawable.notifications),
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(48.dp)
        )
        Spacer(modifier = Modifier.height(12.dp))
        Text(
            text = if (hasAnyInventory) "No items match your filter" else "Nothing expiring soon",
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurface
        )
        Text(
            text = if (hasAnyInventory) {
                "Try clearing the search or storage filter."
            } else {
                "Items you add to Inventory will show up here as they approach their expiry date."
            },
            fontSize = 13.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(start = 40.dp, top = 4.dp, end = 40.dp)
        )
    }
}

// --------------------------------------------------------------------------
// Preview only — fake data standing in for the ViewModel
// --------------------------------------------------------------------------
private val fakeReminderItems = listOf(
    Inventory(
        name = "Apple",
        category = "Fruit",
        storage = "Refrigerator",
        quantity = 4,
        unit = "pcs",
        daysLeft = 0,
        expiry = "01 Sep 2026"
    ),
    Inventory(
        name = "Milk",
        category = "Dairy",
        storage = "Refrigerator",
        quantity = 1,
        unit = "L",
        daysLeft = 2,
        expiry = "03 Sep 2026"
    ),
    Inventory(
        name = "Bread",
        category = "Bakery",
        storage = "Pantry",
        quantity = 1,
        unit = "loaf",
        daysLeft = 3,
        expiry = "04 Sep 2026"
    ),
    Inventory(
        name = "Fish",
        category = "Fish",
        storage = "Freezer",
        quantity = 500,
        unit = "g",
        daysLeft = 6,
        expiry = "07 Sep 2026"
    ),
    Inventory(
        name = "Rice",
        category = "Grain",
        storage = "Pantry",
        quantity = 5,
        unit = "kg",
        daysLeft = 30,
        expiry = "01 Oct 2026"
    )
)

@Preview(showBackground = true)
@Composable
private fun ReminderScreenPreview() {
    SaveBiteTheme {
        ReminderScreenContent(
            grouped = ExpiryGrouping.group(fakeReminderItems),
            searchQuery = "",
            onSearchQueryChange = {},
            storageOptions = listOf("Refrigerator", "Freezer", "Pantry"),
            selectedStorage = null,
            onStorageSelected = {},
            hasAnyInventory = true
        )
    }
}

@Preview(showBackground = true, name = "Empty state")
@Composable
private fun ReminderScreenEmptyPreview() {
    SaveBiteTheme {
        ReminderScreenContent(
            grouped = emptyMap(),
            searchQuery = "",
            onSearchQueryChange = {},
            storageOptions = emptyList(),
            selectedStorage = null,
            onStorageSelected = {},
            hasAnyInventory = false
        )
    }
}
