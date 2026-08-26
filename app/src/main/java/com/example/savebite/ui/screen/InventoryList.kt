package com.example.savebite.ui.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
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
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.savebite.R
import com.example.savebite.model.Inventory
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
    onNavigateToAddInventory: () -> Unit = {},
    onItemClick: (Inventory) -> Unit = {},
    onEditClick: (Inventory) -> Unit = {},
    onDeleteClick: (Inventory) -> Unit = {},
    onNavigateToManageStorage: () -> Unit = {},
    onToggleConsume: (Inventory) -> Unit = {},
    onMoveConsumedToReport: () -> Unit = {}
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
        // 关键改动 1：利用 Scaffold 的 bottomBar 放置 Consume 提示卡片
        bottomBar = {
            if (consumedCount > 0) {
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                        .clickable { onMoveConsumedToReport() }
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Mark as Consumed ($consumedCount)",
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp,
                                color = MaterialTheme.colorScheme.onSecondaryContainer
                            )
                            Text(
                                text = "Move selected items to consumption report",
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.7f)
                            )
                        }
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .background(MaterialTheme.colorScheme.surface, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.ChevronRight,
                                contentDescription = "Proceed to report",
                                tint = MaterialTheme.colorScheme.onSurface
                            )
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

            SearchFoodBar(
                query = searchQuery,
                onQueryChange = onQueryChange
            )

            Spacer(modifier = Modifier.height(10.dp))

            // 关键改动 2：列表占满剩余控件，底部留出边距避免最后一张卡片被 FAB 挡住
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