package com.example.savebite.ui.screen

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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberTopAppBarState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.savebite.R
import com.example.savebite.model.DefaultStorages
import com.example.savebite.model.Inventory
import com.example.savebite.model.InventorySortOption
import com.example.savebite.ui.navigation.AppSearchBar
import com.example.savebite.ui.navigation.AppTopBar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InventoryList(
    foods: List<Inventory> = emptyList(),
    storageList: List<String> = DefaultStorages.ALL,
    isOffline: Boolean = false,
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
    val scrollBehavior = TopAppBarDefaults.enterAlwaysScrollBehavior(rememberTopAppBarState())

    Scaffold(
        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            AppTopBar(
                title = stringResource(R.string.inventory_list_title),
                showBackButton = false,
                scrollBehavior = scrollBehavior
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
                    contentDescription = stringResource(R.string.content_desc_add_button),
                    modifier = Modifier.size(24.dp)
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
                                text = stringResource(R.string.inventory_selected_items, consumedCount),
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp,
                                color = MaterialTheme.colorScheme.onSecondaryContainer
                            )
                            Text(
                                text = stringResource(R.string.inventory_report_status_hint),
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
                                Text(stringResource(R.string.inventory_status_consumed), fontSize = 12.sp)
                            }

                            Button(
                                onClick = { onMoveConsumedToReport("WASTED") },
                                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                            ) {
                                Text(stringResource(R.string.inventory_status_wasted), fontSize = 12.sp)
                            }
                        }
                    }
                }
            }
        }
    ) { padding ->
        LazyColumn(
            contentPadding = PaddingValues(
                top = padding.calculateTopPadding() + 8.dp,
                bottom = padding.calculateBottomPadding() + 80.dp,
                start = 8.dp,
                end = 8.dp
            ),
            verticalArrangement = Arrangement.spacedBy(10.dp),
            modifier = Modifier.fillMaxSize()
        ) {
            if (isOffline) {
                item {
                    OfflineBanner()
                    Spacer(modifier = Modifier.height(8.dp))
                }
            }

            item {
                StorageTab(
                    storages = storageList,
                    selectedStorage = selectedStorage,
                    onStorageSelected = onStorageSelected,
                    onNavigateToManageStorage = onNavigateToManageStorage
                )
                Spacer(modifier = Modifier.height(8.dp))
            }

            item {
                AppSearchBar(
                    query = searchQuery,
                    onQueryChange = onQueryChange,
                    placeholderText = stringResource(R.string.inventory_search_placeholder),
                    sortOptions = InventorySortOption.entries,
                    selectedSortOption = selectedSortOption,
                    onSortOptionSelected = onSortOptionSelected,
                    getSortOptionLabel = { it.label }
                )
                Spacer(modifier = Modifier.height(10.dp))
            }

            if (foods.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier
                            .fillParentMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = stringResource(R.string.inventory_no_data),
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            } else {
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

// Banner shown when the device is offline or synchronization failed.
@Composable
fun OfflineBanner() {
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.errorContainer,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                painter = painterResource(R.drawable.info),
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onErrorContainer,
                modifier = Modifier.size(16.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = stringResource(R.string.inventory_offline_banner),
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onErrorContainer
            )
        }
    }
}

// Horizontal scrollable tabs for selecting storage locations
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