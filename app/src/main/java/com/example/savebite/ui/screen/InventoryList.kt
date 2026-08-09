package com.example.savebite.ui.screen

import androidx.compose.foundation.BorderStroke
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
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.savebite.R
import com.example.savebite.model.Inventory
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
    onAddStorageClick: (String) -> Unit = {},
    onItemClick: (Inventory) -> Unit = {},
    onEditClick: (Inventory) -> Unit = {},
    onDeleteClick: (Inventory) -> Unit = {},
    onBackClick: () -> Unit = {}
) {
    var showAddDialog by remember { mutableStateOf(false) }
    var showAddStorageDialog by remember { mutableStateOf(false) }
    var newStorageName by remember { mutableStateOf("") }

    Scaffold(
        containerColor = Color.White,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Food Inventory",
                        color = Color.White,
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = primaryLight
                )
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showAddDialog = true },
                containerColor = onPrimaryContainerLight
            ) {
                Icon(
                    painter = painterResource(R.drawable.add),
                    contentDescription = "Add button",
                    tint = Color.White,
                    modifier = Modifier.size(36.dp)
                )
            }
        },
    ) { padding ->
        // --- ADD CHOICES DIALOG ---
        if (showAddDialog) {
            AlertDialog(
                containerColor = Color.White,
                onDismissRequest = { showAddDialog = false },
                shape = RoundedCornerShape(20.dp),
                title = {
                    Text(
                        text = "What would you like to add?",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                },
                text = {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        OutlinedButton(
                            onClick = {
                                showAddDialog = false
                                onNavigateToAddInventory()
                            },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            border = BorderStroke(1.dp, primaryLight)
                        ) {
                            Text(
                                text = " Inventory ",
                                fontSize = 15.sp,
                                color = primaryLight
                            )
                        }

                        OutlinedButton(
                            onClick = {
                                showAddDialog = false
                                newStorageName = ""
                                showAddStorageDialog = true
                            },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            border = BorderStroke(1.dp, onPrimaryContainerLight)
                        ) {
                            Text(
                                text = "Storage",
                                fontSize = 15.sp,
                                color = onPrimaryContainerLight
                            )
                        }
                    }
                },
                confirmButton = {},
                dismissButton = {
                    TextButton(onClick = { showAddDialog = false }) {
                        Text("Cancel", color = Color.Gray)
                    }
                }
            )
        }

        if (showAddStorageDialog) {
            AlertDialog(
                containerColor = Color.White,
                onDismissRequest = { showAddStorageDialog = false },
                shape = RoundedCornerShape(20.dp),
                title = {
                    Text(
                        text = "Add New Storage Location",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                },
                text = {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp)
                    ) {
                        OutlinedTextField(
                            value = newStorageName,
                            onValueChange = { newStorageName = it },
                            label = { Text("Storage Name") },
                            placeholder = { Text("e.g. Snack Box, Cellar") },
                            singleLine = true,
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth(),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = primaryLight,
                                focusedLabelColor = primaryLight
                            )
                        )
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            if (newStorageName.isNotBlank()) {
                                onAddStorageClick(newStorageName.trim())
                                showAddStorageDialog = false
                            }
                        },
                        enabled = newStorageName.isNotBlank(),
                        colors = ButtonDefaults.buttonColors(containerColor = primaryLight),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text("Save", color = Color.White)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showAddStorageDialog = false }) {
                        Text("Cancel", color = Color.Gray)
                    }
                }
            )
        }

        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .padding(8.dp)
        ) {
            StorageTab (
                storages = storageList,
                selectedStorage = selectedStorage,
                onStorageSelected = onStorageSelected
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
    onStorageSelected: (String) -> Unit
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
                    selectedContainerColor = onPrimaryContainerLight,
                    selectedLabelColor = Color.White,
                    containerColor = outlineVariantLight,
                    labelColor = Color.Black
                ),
                border = FilterChipDefaults.filterChipBorder(
                    enabled = true,
                    selected = isSelected,
                    borderColor = Color.Transparent,
                    selectedBorderColor = Color.Transparent
                )
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
            focusedContainerColor = surfaceContainerLowLight,
            unfocusedContainerColor = surfaceContainerLowLight,
            focusedBorderColor = Color.Transparent,
            unfocusedBorderColor = Color.Transparent
        )
    )
}