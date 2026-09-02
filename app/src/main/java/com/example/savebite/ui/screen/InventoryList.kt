package com.example.savebite.ui.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.savebite.R
import com.example.savebite.model.Inventory
import com.example.savebite.model.InventorySortOption
import com.example.savebite.ui.navigation.AppSearchBar
import com.example.savebite.ui.navigation.AppTopBar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InventoryList(
    foods: List<Inventory> = emptyList(),
    storageList: List<String> = listOf("Pantry", "Refrigerator", "Freezer"),
    searchQuery: String = "",
    onQueryChange: (String) -> Unit = {},
    selectedStorage: String = "All",
    onStorageSelected: (String) -> Unit = {},
    selectedSortOption: InventorySortOption = InventorySortOption.PRIORITY,
    onSortOptionSelected: (InventorySortOption) -> Unit = {},
    onNavigateToAddInventory: () -> Unit = {},
    onItemClick: (Inventory) -> Unit = {},
    onEditClick: (Inventory) -> Unit = {},
    onDeleteClick: (Inventory) -> Unit = {},
    onNavigateToManageStorage: () -> Unit = {},
    onToggleConsume: (Inventory) -> Unit = {},
    onMoveConsumedToReport: (String) -> Unit = {}
) {
    val consumedCount = foods.count { it.isConsumed }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            AppTopBar(
                title = "Food Inventory",
                showBackButton = false
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = onNavigateToAddInventory,
                containerColor = MaterialTheme.colorScheme.primaryContainer,
                contentColor = MaterialTheme.colorScheme.onPrimaryContainer
            ) {
                Icon(
                    painter = painterResource(R.drawable.add),
                    contentDescription = "Add button",
                    modifier = Modifier.size(36.dp)
                )
            }
        },

        bottomBar = {
            if (consumedCount > 0) {
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Selected Items ($consumedCount)",
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp,
                                color = MaterialTheme.colorScheme.onSecondaryContainer
                            )
                            Text(
                                text = "Choose status to report",
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.7f)
                            )
                        }

                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Button(
                                onClick = { onMoveConsumedToReport("CONSUMED") },
                                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                            ) {
                                Text("Consumed", fontSize = 12.sp)
                            }

                            Button(
                                onClick = { onMoveConsumedToReport("WASTED") },
                                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                            ) {
                                Text("Wasted", fontSize = 12.sp)
                            }
                        }
                    }
                }
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .padding(horizontal = 8.dp)
        ) {
            StorageTab(
                storages = storageList,
                selectedStorage = selectedStorage,
                onStorageSelected = onStorageSelected,
                onNavigateToManageStorage = onNavigateToManageStorage
            )

            Spacer(modifier = Modifier.height(8.dp))

            AppSearchBar(
                query = searchQuery,
                onQueryChange = onQueryChange,
                placeholderText = "Search food...",
                sortOptions = InventorySortOption.values().toList(),
                selectedSortOption = selectedSortOption,
                onSortOptionSelected = onSortOptionSelected,
                getSortOptionLabel = { it.label }
            )

            Spacer(modifier = Modifier.height(10.dp))

            if (foods.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "No Data Recorded",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(
                        items = foods,
                        key = { item -> item.id }
                    ) { food ->
                        InventoryCard(
                            food = food,
                            onToggleConsume = onToggleConsume,
                            onEditClick = onEditClick,
                            onDeleteClick = onDeleteClick,
                            onCardClick = onItemClick
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun StorageTab(
    storages: List<String>,
    selectedStorage: String,
    onStorageSelected: (String) -> Unit,
    onNavigateToManageStorage: () -> Unit
) {
    val scrollState = rememberScrollState()

    LaunchedEffect(selectedStorage) {
        scrollState.animateScrollTo(0)
    }

    val categories = remember(storages, selectedStorage) {
        val list = mutableListOf("All")
        if (selectedStorage != "All" && storages.contains(selectedStorage)) {
            list.add(selectedStorage)
        }
        storages.forEach { storage ->
            if (storage != selectedStorage) {
                list.add(storage)
            }
        }
        list
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(scrollState),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        categories.forEach { storage ->
            val isSelected = selectedStorage == storage
            FilterChip(
                selected = isSelected,
                onClick = { onStorageSelected(storage) },
                label = { Text(storage) },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = MaterialTheme.colorScheme.primary,
                    selectedLabelColor = MaterialTheme.colorScheme.onPrimary,
                    containerColor = MaterialTheme.colorScheme.surfaceVariant,
                    labelColor = MaterialTheme.colorScheme.onSurfaceVariant
                ),
                border = FilterChipDefaults.filterChipBorder(
                    enabled = true,
                    selected = isSelected,
                    borderColor = MaterialTheme.colorScheme.outline,
                    selectedBorderColor = Color.Transparent
                )
            )
        }

        IconButton(
            onClick = onNavigateToManageStorage,
            modifier = Modifier.size(32.dp)
        ) {
            Text(
                text = "...",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
        }
    }
}