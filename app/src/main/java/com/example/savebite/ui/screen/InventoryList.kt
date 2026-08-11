package com.example.savebite.ui.screen

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
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
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.savebite.R
import com.example.savebite.model.Inventory
import com.example.savebite.ui.navigation.AppTopBar
import com.example.savebite.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InventoryList(
    foods: List<Inventory> = emptyList(),
    storageList: List<String> = listOf("Pantry", "Refrigerator", "Freezer"),
    searchQuery: String = "",
    onQueryChange: (String) -> Unit = {},
    selectedStorage: String = "All",
    onStorageSelected: (String) -> Unit = {},
    onNavigateToAddInventory: () -> Unit = {},
    onItemClick: (Inventory) -> Unit = {},
    onEditClick: (Inventory) -> Unit = {},
    onDeleteClick: (Inventory) -> Unit = {},
    onNavigateToManageStorage: () -> Unit = {}
) {
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
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .padding(8.dp)
        ) {
            StorageTab (
                storages = storageList,
                selectedStorage = selectedStorage,
                onStorageSelected = onStorageSelected,
                onNavigateToManageStorage = onNavigateToManageStorage
            )

            Spacer(modifier = Modifier.height(8.dp))

            SearchFoodBar(
                query = searchQuery,
                onQueryChange = onQueryChange
            )

            Spacer(modifier = Modifier.height(10.dp))

            // Populated LazyColumn displaying cards
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
                        onEditClick = { onEditClick(food) },
                        onDeleteClick = { onDeleteClick(food) },
                        onCardClick = { onItemClick(food) }
                    )
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

    // Automatically scroll back to the start whenever the selected storage changes
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

@Composable
fun SearchFoodBar(
    query: String,
    onQueryChange: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    OutlinedTextField(
        value = query,
        onValueChange = onQueryChange,
        modifier = modifier
            .fillMaxWidth()
            .height(50.dp),
        textStyle = MaterialTheme.typography.bodyMedium,
        leadingIcon = {
            Icon(
                painter = painterResource(R.drawable.search),
                contentDescription = "Search",
                tint = Color.Gray,
                modifier = Modifier.size(18.dp)
            )
        },
        trailingIcon = {
            if (query.isNotEmpty()) {
                IconButton(onClick = { onQueryChange("") }) {
                    Icon(
                        painter = painterResource(R.drawable.clear),
                        contentDescription = "Clear search",
                        tint = Color.Gray,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        },
        placeholder = {
            Text(
                text = "Search food...",
                fontSize = 14.sp
            )
        },
        shape = RoundedCornerShape(20.dp),
        singleLine = true,
        colors = OutlinedTextFieldDefaults.colors(
            focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
            unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
            focusedBorderColor = Color.Transparent,
            unfocusedBorderColor = Color.Transparent
        )
    )
}
