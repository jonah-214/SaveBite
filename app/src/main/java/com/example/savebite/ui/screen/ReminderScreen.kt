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
import androidx.compose.material3.Button
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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
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
        onClearFilters = reminderViewModel::clearFilters,
        hasAnyInventory = totalItemCount > 0,
        onBackClick = { navController.popBackStack() },
        onItemClick = { item ->
            navController.navigate("${AppRoutes.INVENTORY_DETAILS}/${item.id}")
        },
        onRecipeClick = { item ->
            navController.navigate("${AppRoutes.RECIPE}?searchQuery=${item.name}") {
                launchSingleTop = true
            }
        }
    )
}

// Main content for the Reminder screen
@Composable
fun ReminderScreenContent(
    grouped: Map<ExpirySection, List<Inventory>>,
    searchQuery: String,
    onSearchQueryChange: (String) -> Unit,
    sortOrder: SortOrder,
    onSortOrderChange: (SortOrder) -> Unit,
    onClearFilters: () -> Unit,
    hasAnyInventory: Boolean,
    onBackClick: () -> Unit = {},
    onItemClick: (Inventory) -> Unit = {},
    onRecipeClick: (Inventory) -> Unit = {}
) {

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            AppTopBar(
                title = stringResource(R.string.reminder_title),
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
                placeholderText = stringResource(R.string.reminder_search_placeholder),
                sortOptions = SortOrder.entries,
                selectedSortOption = sortOrder,
                onSortOptionSelected = onSortOrderChange,
                getSortOptionLabel = { stringResource(it.labelRes) }
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Show empty state if no items found
            if (grouped.isEmpty()) {
                EmptyReminderState(
                    hasAnyInventory = hasAnyInventory,
                    onClearFilters = onClearFilters,
                    modifier = Modifier.fillMaxSize()
                )
            } else {
                // List of expiring items grouped by section
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(bottom = 16.dp)
                ) {
                    item { SummaryRow(grouped) }
                    item { Spacer(modifier = Modifier.height(16.dp)) }

                    ExpirySection.entries.forEach { section ->
                        val sectionItems = grouped[section].orEmpty()
                        if (sectionItems.isNotEmpty()) {
                            // Section header
                            item(key = "header_${section.name}") {
                                SectionHeaderText(section)
                            }
                            // Item cards
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

// Summary row showing counts for each expiry section
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
                        text = stringResource(section.labelRes),
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

// Header text for each expiry section
@Composable
private fun SectionHeaderText(section: ExpirySection) {
    Text(
        text = stringResource(section.labelRes),
        fontSize = 16.sp,
        fontWeight = FontWeight.Bold,
        color = MaterialTheme.colorScheme.onBackground,
        modifier = Modifier.padding(top = 12.dp, bottom = 8.dp)
    )
}

// Card showing item details and expiry badge
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
            // Avatar with item's first letter
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

            // Badge showing days left
            Text(
                text = if (item.daysLeft <= 0) stringResource(R.string.reminder_expiry_today) else stringResource(R.string.reminder_expiry_days_left, item.daysLeft),
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
                    contentDescription = stringResource(R.string.reminder_content_desc_recipe, item.name),
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}

// Empty state when no items match filters or are expiring
@Composable
private fun EmptyReminderState(
    hasAnyInventory: Boolean,
    onClearFilters: () -> Unit,
    modifier: Modifier = Modifier
) {
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
            text = if (hasAnyInventory) stringResource(R.string.reminder_empty_search_title) else stringResource(R.string.reminder_empty_no_items_title),
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurface
        )
        Text(
            text = if (hasAnyInventory) {
                stringResource(R.string.reminder_empty_search_body)
            } else {
                stringResource(R.string.reminder_empty_no_items_body)
            },
            fontSize = 13.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(start = 40.dp, top = 4.dp, end = 40.dp),
            textAlign = TextAlign.Center
        )

        if (hasAnyInventory) {
            Spacer(modifier = Modifier.height(16.dp))
            Button(
                onClick = onClearFilters,
                shape = RoundedCornerShape(12.dp)
            ) {
                Text(stringResource(R.string.reminder_action_clear_filters))
            }
        }
    }
}
