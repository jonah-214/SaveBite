package com.example.savebite.ui.screen

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import coil.compose.AsyncImage
import com.example.savebite.R
import com.example.savebite.model.ExpiryItem
import com.example.savebite.model.RecipeSuggestion
import com.example.savebite.model.SyncStatus
import com.example.savebite.model.WastePeriod
import com.example.savebite.model.WasteSummary
import com.example.savebite.ui.navigation.AppRoutes
import com.example.savebite.ui.theme.expirySectionColors
import com.example.savebite.ui.viewmodel.DashboardViewModel
import com.example.savebite.utils.Currency

@Composable
fun DashboardScreen(
    navController: NavHostController,
    dashboardViewModel: DashboardViewModel
) {
    val syncStatus by dashboardViewModel.syncStatus.collectAsStateWithLifecycle()
    val username by dashboardViewModel.username.collectAsStateWithLifecycle()
    val avatarUrl by dashboardViewModel.avatarUrl.collectAsStateWithLifecycle()
    val expiringItems by dashboardViewModel.expiringItems.collectAsStateWithLifecycle()
    val inventoryCount by dashboardViewModel.inventoryCount.collectAsStateWithLifecycle()
    val shoppingCount by dashboardViewModel.shoppingListCount.collectAsStateWithLifecycle()
    val savedAmount by dashboardViewModel.savedAmount.collectAsStateWithLifecycle()
    val wasteSummary by dashboardViewModel.wasteSummary.collectAsStateWithLifecycle()
    val wastePeriod by dashboardViewModel.wastePeriod.collectAsStateWithLifecycle()
    val recipeSuggestions by dashboardViewModel.recipeSuggestions.collectAsStateWithLifecycle()

    DashboardContent(
        syncStatus = syncStatus,
        username = username,
        avatarUrl = avatarUrl,
        expiringItems = expiringItems,
        inventoryCount = inventoryCount,
        shoppingCount = shoppingCount,
        savedAmount = savedAmount,
        wasteSummary = wasteSummary,
        wastePeriod = wastePeriod,
        recipeSuggestions = recipeSuggestions,
        onRetrySync = dashboardViewModel::syncFromCloud,
        onWastePeriodSelected = dashboardViewModel::onWastePeriodSelected,
        onProfileClick = {
            navController.navigate(AppRoutes.PROFILE) {
                launchSingleTop = true
            }
        },
        onSeeAllExpiryClick = {
            navController.navigate(AppRoutes.REMINDER) {
                launchSingleTop = true
            }
        },
        onExpiryItemClick = { item ->
            navController.navigate("${AppRoutes.INVENTORY_DETAILS}/${item.id}")
        },
        onInventoryClick = {
            navController.navigate(AppRoutes.INVENTORY) {
                launchSingleTop = true
            }
        },
        onShoppingClick = {
            navController.navigate(AppRoutes.SHOPPING) {
                launchSingleTop = true
            }
        },
        onSeeAllRecipesClick = {
            navController.navigate(AppRoutes.RECIPE) {
                launchSingleTop = true
            }
        },
        onRecipeClick = { index ->
            navController.navigate("${AppRoutes.RECIPE_DETAIL}/$index")
        }
    )
}

// Main layout for the Dashboard content, organized in a vertical scrollable column
@Composable
fun DashboardContent(
    syncStatus: SyncStatus,
    username: String,
    avatarUrl: String?,
    expiringItems: List<ExpiryItem>,
    inventoryCount: Int,
    shoppingCount: Int,
    savedAmount: Double,
    wasteSummary: WasteSummary,
    wastePeriod: WastePeriod,
    recipeSuggestions: List<RecipeSuggestion>,
    onRetrySync: () -> Unit,
    onWastePeriodSelected: (WastePeriod) -> Unit,
    onProfileClick: () -> Unit,
    onSeeAllExpiryClick: () -> Unit,
    onExpiryItemClick: (ExpiryItem) -> Unit,
    onInventoryClick: () -> Unit,
    onShoppingClick: () -> Unit,
    onSeeAllRecipesClick: () -> Unit,
    onRecipeClick: (Int) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .statusBarsPadding()
            .padding(horizontal = 16.dp)
            .padding(bottom = 16.dp)
    ) {
        // Sync status banner
        SyncStatusBanner(
            status = syncStatus,
            onRetryClick = onRetrySync
        )

        // User header
        DashboardHeader(
            username = username,
            avatarUrl = avatarUrl,
            onProfileClick = onProfileClick
        )

        Spacer(modifier = Modifier.height(24.dp))

        // Expiry reminders
        ExpiryReminderCard(
            items = expiringItems,
            onSeeAllClick = onSeeAllExpiryClick,
            onItemClick = onExpiryItemClick
        )
        Spacer(modifier = Modifier.height(20.dp))

        // Inventory and shopping stats
        StatsRow(
            inventoryCount = inventoryCount,
            shoppingCount = shoppingCount,
            onInventoryClick = onInventoryClick,
            onShoppingClick = onShoppingClick
        )
        Spacer(modifier = Modifier.height(20.dp))

        // Waste report
        WasteReportSection(
            savedAmount = savedAmount,
            wasteSummary = wasteSummary,
            selectedPeriod = wastePeriod,
            onPeriodSelected = onWastePeriodSelected
        )
        Spacer(modifier = Modifier.height(20.dp))

        // Recipe suggestions
        RecipeSuggestionsRow(
            recipes = recipeSuggestions,
            onSeeAllClick = onSeeAllRecipesClick,
            onRecipeClick = onRecipeClick
        )
    }
}


// Displays a banner when background synchronization fails or is in progress.
@Composable
fun SyncStatusBanner(
    status: SyncStatus,
    onRetryClick: () -> Unit
) {
    when (status) {
        is SyncStatus.Error -> {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(MaterialTheme.colorScheme.errorContainer)
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = stringResource(R.string.dashboard_sync_error),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onErrorContainer,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f)
                )
                Text(
                    text = stringResource(R.string.dashboard_sync_retry),
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onErrorContainer,
                    modifier = Modifier
                        .clickable(onClick = onRetryClick)
                        .padding(start = 12.dp)
                )
            }
            Spacer(modifier = Modifier.height(16.dp))
        }

        SyncStatus.Syncing -> {
            Text(
                text = stringResource(R.string.dashboard_syncing),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(bottom = 8.dp)
            )
        }

        SyncStatus.Idle -> Unit
    }
}

// Header section containing the welcome message and profile navigation
@Composable
fun DashboardHeader(
    username: String,
    avatarUrl: String? = null,
    onProfileClick: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(
            modifier = Modifier.weight(1f)
        ) {
            Text(
                text = stringResource(R.string.dashboard_welcome, username),
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = stringResource(R.string.dashboard_motto),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }

        Spacer(modifier = Modifier.width(16.dp))

        IconButton(
            onClick = onProfileClick,
            modifier = Modifier
                .background(
                    MaterialTheme.colorScheme.primaryContainer,
                    shape = CircleShape
                )
                .size(48.dp)
        ) {
            if (avatarUrl != null) {
                AsyncImage(
                    model = avatarUrl,
                    contentDescription = stringResource(R.string.content_desc_profile_picture),
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                )
            } else {
                Icon(
                    painter = painterResource(id = R.drawable.account_circle),
                    contentDescription = stringResource(R.string.content_desc_profile_picture),
                    tint = MaterialTheme.colorScheme.onPrimaryContainer,
                    modifier = Modifier.size(28.dp)
                )
            }
        }
    }
}

// Card highlighting food items that are expiring soon
@Composable
fun ExpiryReminderCard(
    items: List<ExpiryItem>,
    onSeeAllClick: () -> Unit,
    onItemClick: (ExpiryItem) -> Unit = {}
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(0.dp),
        border = BorderStroke(0.5.dp, MaterialTheme.colorScheme.outlineVariant),
        modifier = Modifier
            .fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .padding(16.dp)
        ) {
            SectionHeader(
                title = stringResource(R.string.dashboard_expiry_title),
                icon = R.drawable.notifications,
                onSeeAllClick = onSeeAllClick
            )

            if (items.isEmpty()) {
                Text(
                    text = stringResource(R.string.dashboard_expiry_empty),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(vertical = 12.dp)
                )
            } else {
                val countText = if (items.size == 1) {
                    stringResource(R.string.dashboard_expiry_count_singular, items.size)
                } else {
                    stringResource(R.string.dashboard_expiry_count_plural, items.size)
                }
                Text(
                    text = countText,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(bottom = 12.dp)
                )

                // Top 3 urgent items
                items.take(3).forEach { item ->
                    ExpiryItemRow(item, onClick = { onItemClick(item) })
                    Spacer(modifier = Modifier.height(8.dp))
                }
            }
        }
    }
}

// Individual row within the expiry reminder card
@Composable
fun ExpiryItemRow(
    item: ExpiryItem,
    onClick: () -> Unit = {}
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(
                MaterialTheme.colorScheme.surfaceContainerHigh,
                RoundedCornerShape(12.dp)
            )
            .clickable(onClick = onClick)
            .padding(12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.weight(1f)
        ) {
            Column {
                Text(
                    text = item.name,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = "${item.quantity} • ${item.category}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }

        Spacer(modifier = Modifier.width(8.dp))

        val (bg, fg) = expirySectionColors(item.daysLeft)
        val dayText = if (item.daysLeft <= 0) {
            stringResource(R.string.dashboard_expiry_today)
        } else {
            stringResource(R.string.dashboard_expiry_days_left, item.daysLeft)
        }
        Text(
            text = dayText,
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.Bold,
            color = fg,
            modifier = Modifier
                .background(bg, RoundedCornerShape(8.dp))
                .padding(horizontal = 10.dp, vertical = 6.dp)
        )
    }
}

// Row layout containing summary cards for Inventory and Shopping list
@Composable
fun StatsRow(
    inventoryCount: Int,
    shoppingCount: Int,
    onInventoryClick: () -> Unit,
    onShoppingClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        StatsCard(
            count = inventoryCount,
            label = stringResource(R.string.dashboard_inventory_label),
            icon = R.drawable.inventory_2,
            modifier = Modifier
                .weight(1f),
            onClick = onInventoryClick
        )
        StatsCard(
            count = shoppingCount,
            label = stringResource(R.string.dashboard_shopping_label),
            icon = R.drawable.shopping_cart,
            modifier = Modifier
                .weight(1f),
            onClick = onShoppingClick
        )
    }
}

// Small card component used for displaying a single metric and an icon.
@Composable
fun StatsCard(
    count: Int,
    label: String,
    icon: Int,
    modifier: Modifier,
    onClick: () -> Unit
) {
    Card(
        modifier = modifier
            .clickable { onClick() },
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(0.dp)
    ) {
        Column(modifier = Modifier
            .padding(16.dp)
        ) {
            Icon(
                painter = painterResource(id = icon),
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(24.dp)
            )
            Spacer(Modifier.height(8.dp))
            Text(
                text = "$count",
                fontSize = 24.sp,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = label,
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

// Section presenting the waste report: savings for the selected period plus a simple
// summary of what was wasted (instead of a bar chart).
@Composable
fun WasteReportSection(
    savedAmount: Double,
    wasteSummary: WasteSummary,
    selectedPeriod: WastePeriod,
    onPeriodSelected: (WastePeriod) -> Unit
) {
    Column {
        SectionHeader(
            title = stringResource(R.string.dashboard_waste_title),
            icon = R.drawable.assignment
        )

        Spacer(modifier = Modifier.height(8.dp))

        val savedLabel = when (selectedPeriod) {
            WastePeriod.WEEKLY -> stringResource(R.string.dashboard_saved_weekly)
            WastePeriod.MONTHLY -> stringResource(R.string.dashboard_saved_monthly)
            WastePeriod.YEARLY -> stringResource(R.string.dashboard_saved_yearly)
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.primaryContainer, RoundedCornerShape(16.dp))
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = savedLabel,
                color = MaterialTheme.colorScheme.onPrimaryContainer,
                fontWeight = FontWeight.Medium
            )
            Text(
                text = Currency.format(savedAmount),
                color = MaterialTheme.colorScheme.onPrimaryContainer,
                fontWeight = FontWeight.Bold
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Waste summary
        if (wasteSummary.totalItemsWasted == 0) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 16.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = stringResource(R.string.dashboard_waste_empty),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        } else {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                WasteStatTile(
                    label = stringResource(R.string.dashboard_waste_items_label),
                    value = "${wasteSummary.totalItemsWasted}",
                    modifier = Modifier.weight(1f)
                )
                WasteStatTile(
                    label = stringResource(R.string.dashboard_waste_value_label),
                    value = Currency.format(wasteSummary.totalValueWasted),
                    modifier = Modifier.weight(1f)
                )
            }

            if (wasteSummary.topCategory != null) {
                Text(
                    text = stringResource(
                        R.string.dashboard_waste_top_category,
                        wasteSummary.topCategory,
                        wasteSummary.topCategoryCount
                    ),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 12.dp),
                    textAlign = TextAlign.Center
                )
            }
        }

        val caption = when (selectedPeriod) {
            WastePeriod.WEEKLY -> stringResource(R.string.dashboard_waste_caption_weekly)
            WastePeriod.MONTHLY -> stringResource(R.string.dashboard_waste_caption_monthly)
            WastePeriod.YEARLY -> stringResource(R.string.dashboard_waste_caption_yearly)
        }
        Text(
            text = caption,
            fontSize = 12.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 8.dp, bottom = 12.dp),
            textAlign = TextAlign.Center
        )

        WastePeriodToggle(
            selectedPeriod = selectedPeriod,
            onPeriodSelected = onPeriodSelected
        )
    }
}

// Small tile used inside the waste summary to show one metric (e.g. items wasted, value lost)
@Composable
fun WasteStatTile(
    label: String,
    value: String,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .background(MaterialTheme.colorScheme.surfaceContainerHigh, RoundedCornerShape(12.dp))
            .padding(12.dp)
    ) {
        Text(
            text = value,
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
        )
        Text(
            text = label,
            fontSize = 12.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}


// Segmented control for toggling the waste report time window
@Composable
fun WastePeriodToggle(
    selectedPeriod: WastePeriod,
    onPeriodSelected: (WastePeriod) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .background(MaterialTheme.colorScheme.surfaceContainerHigh)
            .padding(4.dp),
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        WastePeriod.entries.forEach { period ->
            val isSelected = period == selectedPeriod
            Box(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(8.dp))
                    .background(if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceContainerHigh)
                    .clickable { onPeriodSelected(period) }
                    .padding(vertical = 8.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = stringResource(period.labelRes),
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Medium,
                    color = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

// Horizontal row displaying filtered recipe suggestions based on expiring items
@Composable
fun RecipeSuggestionsRow(
    recipes: List<RecipeSuggestion>,
    onSeeAllClick: () -> Unit,
    onRecipeClick: (Int) -> Unit
) {
    Column {
        SectionHeader(
            title = stringResource(R.string.dashboard_recipes_title),
            icon = R.drawable.chef_hat,
            onSeeAllClick = onSeeAllClick
        )

        Spacer(modifier = Modifier.height(8.dp))

        if (recipes.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 32.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = stringResource(R.string.dashboard_recipes_empty),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center
                )
            }
        } else {
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(recipes, key = { it.name }) { recipe ->
                    RecipeCard(recipe, onClick = { onRecipeClick(recipe.index) })
                }
            }
        }
    }
}

// Individual card within the recipe suggestions row
@Composable
fun RecipeCard(
    recipe: RecipeSuggestion,
    onClick: () -> Unit = {}
) {
    Card(
        onClick = onClick,
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier
            .width(160.dp)
    ) {
        Column(modifier = Modifier
            .padding(12.dp)
        ) {
            Icon(
                painter = painterResource(id = R.drawable.book_2),
                contentDescription = null,
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = recipe.name,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            val usesText = if (recipe.expiringCount == 1) {
                stringResource(R.string.dashboard_recipe_uses_singular, recipe.expiringCount)
            } else {
                stringResource(R.string.dashboard_recipe_uses_plural, recipe.expiringCount)
            }
            Text(
                text = usesText,
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

// Simple text button used for "See All" navigation in section headers
@Composable
fun SeeAllText(onClick: () -> Unit) {
    Text(
        text = stringResource(R.string.dashboard_see_all),
        color = MaterialTheme.colorScheme.primary,
        style = MaterialTheme.typography.labelLarge,
        modifier = Modifier
            .clickable { onClick() }
            .padding(vertical = 4.dp, horizontal = 8.dp)
    )
}

// Generic header used for dashboard sections, including an optional icon and "See All" link
@Composable
fun SectionHeader(
    title: String,
    modifier: Modifier = Modifier,
    icon: Int? = null,
    onSeeAllClick: (() -> Unit)? = null
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            if (icon != null) {
                Icon(
                    painter = painterResource(id = icon),
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
            }
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
        }
        if (onSeeAllClick != null) {
            SeeAllText(onSeeAllClick)
        }
    }
}