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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.example.savebite.R
import com.example.savebite.model.Inventory
import com.example.savebite.ui.navigation.AppRoutes
import com.example.savebite.ui.navigation.AppSearchBar
import com.example.savebite.ui.navigation.AppTopBar
import com.example.savebite.ui.theme.expirySectionColors
import com.example.savebite.ui.viewmodel.ReminderViewModel
import com.example.savebite.ui.viewmodel.SortOrder
import com.example.savebite.utils.ExpiryGrouping
import com.example.savebite.utils.ExpirySection


@Composable
fun ReminderScreen(
    navController: NavHostController,
    reminderViewModel: ReminderViewModel
) {
    val grouped by reminderViewModel.groupedItems.collectAsState()
    val searchQuery by reminderViewModel.searchQuery.collectAsState()
    val totalItemCount by reminderViewModel.totalItemCount.collectAsState()
    val sortOrder by reminderViewModel.sortOrder.collectAsState()

    ReminderScreenContent(
        grouped = grouped,
        searchQuery = searchQuery,
        onSearchQueryChange = reminderViewModel::onSearchQueryChange,
        sortOrder = sortOrder,
        onSortOrderChange = reminderViewModel::onSortOrderChange,
        hasAnyInventory = totalItemCount > 0,
        onBackClick = { navController.popBackStack() },
        onItemClick = { item ->
            navController.navigate("${AppRoutes.INVENTORY_DETAILS}/${item.id}")
        },
        onRecipeClick = {
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
    sortOrder: SortOrder,
    onSortOrderChange: (SortOrder) -> Unit,
    hasAnyInventory: Boolean,
    onBackClick: () -> Unit = {},
    onItemClick: (Inventory) -> Unit = {},
    onRecipeClick: (Inventory) -> Unit = {}
) {
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
                placeholderText = "Search items...",
                sortOptions = SortOrder.entries,
                selectedSortOption = sortOrder,
                onSortOptionSelected = onSortOrderChange,
                getSortOptionLabel = { it.label }
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
            val (bg, fg) = expirySectionColors(section)
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
private fun ReminderCard(
    item: Inventory,
    onClick: () -> Unit,
    onRecipeClick: () -> Unit
) {
    val section = ExpiryGrouping.sectionFor(item.daysLeft)
    val (badgeBg, badgeFg) = expirySectionColors(section)

    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(14.dp),
        color = MaterialTheme.colorScheme.surfaceContainerHigh,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Expiry-colored Avatar with first letter
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(badgeBg),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = item.name.take(1).uppercase(),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = badgeFg
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = item.name,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = "${item.quantity} ${item.unit} • ${item.category}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Spacer(modifier = Modifier.width(8.dp))

            // Expiry Badge
            Text(
                text = if (item.daysLeft <= 0) "Today" else "${item.daysLeft}d left",
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold,
                color = badgeFg,
                modifier = Modifier
                    .background(badgeBg, RoundedCornerShape(8.dp))
                    .padding(horizontal = 10.dp, vertical = 6.dp)
            )

            Spacer(modifier = Modifier.width(4.dp))

            IconButton(
                onClick = onRecipeClick,
                modifier = Modifier.size(32.dp)
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.chef_hat),
                    contentDescription = "See recipes using ${item.name}",
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(20.dp)
                )
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