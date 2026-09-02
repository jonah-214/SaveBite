package com.example.savebite.ui.screen

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.res.painterResource
import com.example.savebite.R
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.savebite.model.ShoppingItem
import com.example.savebite.ui.navigation.AppSearchBar
import com.example.savebite.ui.navigation.AppTopBar
import com.example.savebite.ui.viewmodel.ShoppingViewModel

@Composable
fun ShoppingListScreen(
    viewModel: ShoppingViewModel,
    onNavigateToAddItem: () -> Unit = {},
    onNavigateToEditItem: (ShoppingItem) -> Unit = {},
    onNavigateToAddToInventory: () -> Unit = {}
) {
    val items by viewModel.items.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()
    val purchasedCount = items.count { it.isPurchased }
    val totalCount = items.size

    // Track collapsed/expanded state for each category. Defaults to expanded (true).
    val categoryExpandedStates = remember { mutableStateMapOf<String, Boolean>() }

    val progressAnim by animateFloatAsState(
        targetValue = if (totalCount > 0) purchasedCount.toFloat() / totalCount else 0f,
        animationSpec = tween(durationMillis = 300),
        label = "progressAnimation"
    )

    var itemToDelete by remember { mutableStateOf<ShoppingItem?>(null) }

    itemToDelete?.let { item ->
        AlertDialog(
            onDismissRequest = { itemToDelete = null },
            title = { Text("Delete Item", fontWeight = FontWeight.Bold) },
            text = { Text("Are you sure you want to delete \"${item.name}\"?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.deleteItem(item)
                        itemToDelete = null
                    },
                    colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error)
                ) {
                    Text("Delete", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { itemToDelete = null }) {
                    Text("Cancel")
                }
            }
        )
    }

    Scaffold(
        topBar = {
            AppTopBar(
                title = "Shopping List",
                showBackButton = false
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp)
        ) {
            // Progress Header Card
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp)
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "🧺",
                        fontSize = 32.sp,
                        modifier = Modifier.semantics { contentDescription = "Shopping basket icon" }
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        // Main Label: Handles empty list / zero items / singular / plural
                        Text(
                            text = when {
                                totalCount == 0 -> "No items in list"
                                purchasedCount == 0 -> "No items purchased"
                                purchasedCount == 1 -> "1 / $totalCount item purchased"
                                else -> "$purchasedCount / $totalCount items purchased"
                            },
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp,
                            color = MaterialTheme.colorScheme.onSurface
                        )

                        Spacer(modifier = Modifier.height(6.dp))

                        // Smoothly animated progress bar
                        LinearProgressIndicator(
                            progress = { progressAnim },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(6.dp)
                                .clip(RoundedCornerShape(3.dp)),
                            color = MaterialTheme.colorScheme.primary,
                            trackColor = MaterialTheme.colorScheme.surfaceVariant
                        )

                        Spacer(modifier = Modifier.height(4.dp))

                        // Contextual encouragement subtext
                        Text(
                            text = when {
                                totalCount == 0 -> "Add some items to get started!"
                                purchasedCount == 0 -> "Ready to go? Start checking off your list!"
                                purchasedCount == totalCount -> "All set! Everything has been purchased! 🎉"
                                else -> "Keep going! You're making progress!"
                            },
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            AppSearchBar<String>(
                query = searchQuery,
                onQueryChange = { viewModel.onSearchQueryChange(it) },
                placeholderText = "Search shopping list..."
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Categorized List with Collapsible Logic
            val grouped = items.groupBy { it.category }

            if (items.isEmpty()) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth(),
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
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    grouped.forEach { (category, list) ->
                        val isExpanded = if (searchQuery.isNotEmpty()) true else categoryExpandedStates.getOrDefault(category, true)

                        item(key = "header_$category") {
                            CategoryHeader(
                                category = category,
                                count = list.size,
                                isExpanded = isExpanded,
                                onToggleExpand = {
                                    if (searchQuery.isEmpty()) {
                                        categoryExpandedStates[category] = !isExpanded
                                    }
                                }
                            )
                        }

                        if (isExpanded) {
                            items(list, key = { it.id }) { item ->
                                ShoppingItemRow(
                                    item = item,
                                    onToggle = { viewModel.togglePurchased(item) },
                                    onEdit = { onNavigateToEditItem(item) },
                                    onDelete = { itemToDelete = item }
                                )
                            }
                        }
                    }
                }
            }

            // Bottom Add Item Row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = onNavigateToAddItem,
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp)
                ) {
                    Icon(painter = painterResource(R.drawable.add), contentDescription = null, modifier = Modifier.size(20.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Add Item", fontSize = 15.sp, fontWeight = FontWeight.Bold)
                }
            }

            // Transfer to Inventory Banner Prompt
            if (purchasedCount > 0) {
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 12.dp)
                        .clickable { onNavigateToAddToInventory() }
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column (modifier = Modifier.weight(1f)) {
                            Text(
                                "Already purchased?",
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp,
                                color = MaterialTheme.colorScheme.onSecondaryContainer
                            )
                            Text(
                                "Move items to your inventory",
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
                                painter = painterResource(R.drawable.chevron_right),
                                contentDescription = "Proceed",
                                tint = MaterialTheme.colorScheme.onSurface,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun CategoryHeader(
    category: String,
    count: Int,
    isExpanded: Boolean,
    onToggleExpand: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onToggleExpand() }
            .padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            category,
            fontWeight = FontWeight.Bold,
            fontSize = 15.sp,
            color = MaterialTheme.colorScheme.onBackground
        )
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.surfaceVariant)
                    .padding(horizontal = 8.dp, vertical = 2.dp)
            ) {
                Text(
                    count.toString(),
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Spacer(modifier = Modifier.width(4.dp))
            Icon(
                painter = painterResource(id = if (isExpanded) R.drawable.arrow_up else R.drawable.arrow_down),
                contentDescription = if (isExpanded) "Collapse" else "Expand",
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}

@Composable
fun ShoppingItemRow(
    item: ShoppingItem,
    onToggle: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 2.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Checkbox(
            checked = item.isPurchased,
            onCheckedChange = { onToggle() },
            colors = CheckboxDefaults.colors(checkedColor = MaterialTheme.colorScheme.primary)
        )
        Text(
            text = item.name,
            modifier = Modifier.weight(1f),
            color = if (item.isPurchased) MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f) else MaterialTheme.colorScheme.onSurface
        )
        Text(
            "${item.quantity} ${item.unit}",
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            fontSize = 13.sp
        )

        Spacer(modifier = Modifier.weight(0.1f))

        IconButton(onClick = onEdit) {
            Icon(
                painter = painterResource(R.drawable.edit),
                contentDescription = "Edit Item",
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(20.dp)
            )
        }
        IconButton(onClick = onDelete) {
            Icon(
                painter = painterResource(R.drawable.delete),
                contentDescription = "Delete Item",
                tint = MaterialTheme.colorScheme.error,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}