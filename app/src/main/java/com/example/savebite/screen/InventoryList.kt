package com.example.savebite.screen

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.R
import androidx.compose.material3.Surface
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.text.style.TextOverflow
import com.example.savebite.model.Inventory

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InventoryList(
    foods: List<Inventory> = emptyList(), // Default to empty list
    onNavigateToAddInventory: () -> Unit = {},
    onBackClick: () -> Unit = {}
) {
    var searchQuery by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf("All") }
    var showAddDialog by remember { mutableStateOf(false) }
    var showAddCategoryDialog by remember { mutableStateOf(false) }
    var newCategoryName by remember { mutableStateOf("") }

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
                navigationIcon = {
                    IconButton(onClick = { }) {
                        Icon(
                            painter = painterResource(com.example.savebite.R.drawable.back),
                            contentDescription = "Back",
                            tint = Color.White,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color(0xFF148A1E)
                )
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = {showAddDialog = true},
                containerColor = Color(0xFF2D3A31)
            ) {
                Icon(
                    painter = painterResource(com.example.savebite.R.drawable.add),
                    contentDescription = "Add button",
                    tint = Color.White,
                    modifier = Modifier.size(36.dp)

                )
            }
        },
    ) { padding ->
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
                            border = BorderStroke(1.dp, Color(0xFF148A1E))
                        ) {
                            Text(
                                text = " Inventory ",
                                fontSize = 15.sp,
                                color = Color(0xFF148A1E)
                            )
                        }

                        OutlinedButton(
                            onClick = {
                                showAddDialog = false
                                newCategoryName = ""
                                showAddCategoryDialog = true
                            },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            border = BorderStroke(1.dp, Color(0xFF2D3A31))
                        ) {
                            Text(
                                text = "Category",
                                fontSize = 15.sp,
                                color = Color(0xFF2D3A31)
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

        if (showAddCategoryDialog) {
            AlertDialog(
                containerColor = Color.White,
                onDismissRequest = { showAddCategoryDialog = false },
                shape = RoundedCornerShape(20.dp),
                title = {
                    Text(
                        text = "Add New Category",
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
                            value = newCategoryName,
                            onValueChange = { newCategoryName = it },
                            label = { Text("Category Name") },
                            placeholder = { Text("e.g. Snacks, Beverages") },
                            singleLine = true,
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth(),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = Color(0xFF148A1E),
                                focusedLabelColor = Color(0xFF148A1E)
                            )
                        )
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            if (newCategoryName.isNotBlank()) {

                                showAddCategoryDialog = false
                            }
                        },
                        enabled = newCategoryName.isNotBlank(),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF148A1E)),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text("Save", color = Color.White)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showAddCategoryDialog = false }) {
                        Text("Cancel", color = Color.Gray)
                    }
                }
            )
        }

        Column (
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .padding(5.dp)
        ) {
            CategoryTabs(
                selectedCategory = selectedCategory,
                onCategorySelected = {selectedCategory = it}
            )
            Spacer(modifier = Modifier.height(5.dp))
            SearchFoodBar(
                query = searchQuery,
                onQueryChange = {searchQuery=it}
            )

            Spacer(modifier = Modifier.height(10.dp))
            LazyColumn (
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ){

            }
        }
    }
}

@Composable
fun CategoryTabs(
    selectedCategory: String,
    onCategorySelected: (String) -> Unit
) {
    val categories = listOf(
        "All",
        "Pantry",
        "Refrigerator",
        "Freezer",
        "Box 1"
    )
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        categories.forEach { category ->
            val isSelected = selectedCategory == category
            FilterChip(
                selected = isSelected,
                onClick = { onCategorySelected(category) },
                label = { Text(category) },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = Color(0xFF2D3A31),
                    selectedLabelColor = Color.White,
                    containerColor = Color(0xFFF0F0F0),
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
            .height(50.dp), // Reduced height from default 56.dp
        textStyle = MaterialTheme.typography.bodyMedium, // Compact font size
        leadingIcon = {
            Icon(
                painter = painterResource(com.example.savebite.R.drawable.search),
                contentDescription = "Search",
                tint = Color.Gray,
                modifier = Modifier.size(18.dp) // Compact icon size
            )
        },
        trailingIcon = {
            if (query.isNotEmpty()) {
                IconButton(onClick = { onQueryChange("") }) {
                    Icon(
                        painter = painterResource(com.example.savebite.R.drawable.clear), // Built-in Material clear icon
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
            focusedContainerColor = Color(0xFFF5F5F5),
            unfocusedContainerColor = Color(0xFFF5F5F5),
            focusedBorderColor = Color.Transparent,
            unfocusedBorderColor = Color.Transparent
        )
    )
}
