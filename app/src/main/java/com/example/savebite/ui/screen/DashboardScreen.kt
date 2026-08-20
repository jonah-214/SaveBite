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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Inventory2
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.RestaurantMenu
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.example.savebite.ui.navigation.AppRoutes
import com.example.savebite.ui.theme.onWarningContainerLight
import com.example.savebite.ui.theme.warningContainerLight
import com.example.savebite.ui.viewmodel.DashboardViewModel


// Hardcoded data - Expiry & Recipe suggestions
data class ExpiryItem(
    val name: String,
    val quantity: String,
    val daysLeft: Int,
)

data class RecipeSuggestion(
    val name: String,
    val usesText: String,
)

@Composable
fun DashboardScreen(
    navController: NavHostController,
    dashboardViewModel: DashboardViewModel
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp)
            .padding(top = 24.dp, bottom = 16.dp)
    ) {
        DashboardHeader(
            username = dashboardViewModel.username.value,
            onProfileClick = {
                navController.navigate(AppRoutes.PROFILE) {
                    launchSingleTop = true
                }
            }
        )

        Spacer(modifier = Modifier.height(24.dp))

        ExpiryReminderCard(
            items = dashboardViewModel.expiringItems.collectAsState().value,
            onSeeAllClick = {
                navController.navigate(AppRoutes.REMINDER) {
                    launchSingleTop = true
                }
            }
        )
        Spacer(modifier = Modifier.height(20.dp))

        StatsRow(
            inventoryCount = dashboardViewModel.inventoryCount.collectAsState().value,
            shoppingCount = dashboardViewModel.shoppingListCount.collectAsState().value,
            onInventoryClick = {
                navController.navigate(AppRoutes.INVENTORY) {
                    launchSingleTop = true
                }
            },
            onShoppingClick = {
                navController.navigate(AppRoutes.SHOPPING) {
                    launchSingleTop = true
                }
            }
        )
        Spacer(modifier = Modifier.height(20.dp))

        WasteReportSection(
            savedAmount = dashboardViewModel.savedThisMonth,
            wasteByWeek = dashboardViewModel.wasteTrackerData,
            onSeeAllClick = { /* navigate to Waste Tracker later */ }
        )
        Spacer(modifier = Modifier.height(20.dp))

        RecipeSuggestionsRow(
            recipes = dashboardViewModel.recipeSuggestions,
            onSeeAllClick = { /* navigate to Recipe tab */ }
        )
    }
}

@Composable
fun DashboardHeader(
    username: String,
    onProfileClick: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column {
            Text(
                text = "Welcome, $username",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )
            Text(
                text = "Track your food, save the planet",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        IconButton(
            onClick = onProfileClick,
            modifier = Modifier
                .background(
                    MaterialTheme.colorScheme.primaryContainer,
                    shape = CircleShape
                )
                .size(48.dp)
        ) {
            Icon(
                imageVector = Icons.Default.AccountCircle,
                contentDescription = "Profile",
                tint = MaterialTheme.colorScheme.onPrimaryContainer,
                modifier = Modifier.size(28.dp)
            )
        }
    }
}


@Composable
fun ExpiryReminderCard(
    items: List<ExpiryItem>,
    onSeeAllClick: () -> Unit
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
                title = "Expiry Reminder",
                icon = Icons.Default.Notifications,
                onSeeAllClick = onSeeAllClick
            )

            Text(
                text = "${items.size} items expiring soon",
                fontSize = 14.sp,
                modifier = Modifier
                    .padding(vertical = 4.dp)
            )

            items.forEach { item ->
                ExpiryItemRow(item)

                Spacer(modifier = Modifier.height(8.dp))
            }
        }
    }
}

@Composable
fun ExpiryItemRow(
    item: ExpiryItem
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surfaceContainerHigh,
                RoundedCornerShape(12.dp))
            .padding(12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            "${item.name} • ${item.quantity}",
            color = MaterialTheme.colorScheme.onSurface
        )

        val (bg, fg) = if (item.daysLeft <= 1) {
            MaterialTheme.colorScheme.errorContainer to MaterialTheme.colorScheme.onErrorContainer
        } else {
            warningContainerLight to onWarningContainerLight
        }
        Text(
            text = "${item.daysLeft} days",
            color = fg,
            modifier = Modifier
                .background(bg, RoundedCornerShape(50))
                .padding(horizontal = 10.dp, vertical = 4.dp)
        )
    }
}

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
            label = "Inventory",
            icon = Icons.Default.Inventory2,
            modifier = Modifier
                .weight(1f),
            onClick = onInventoryClick
        )
        StatsCard(
            count = shoppingCount,
            label = "Shopping List",
            icon = Icons.Default.ShoppingCart,
            modifier = Modifier
                .weight(1f),
            onClick = onShoppingClick
        )
    }
}

@Composable
fun StatsCard(
    count: Int,
    label: String,
    icon: ImageVector,
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
                icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant
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

@Composable
fun WasteReportSection(
    savedAmount: Int,
    wasteByWeek: List<Int>,
    onSeeAllClick: () -> Unit
) {
    Column {
        SectionHeader(
            title = "Waste Report",
            onSeeAllClick = onSeeAllClick
        )

        Spacer(modifier = Modifier.height(8.dp))

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.primaryContainer, RoundedCornerShape(16.dp))
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = "Saved This Month",
                color = MaterialTheme.colorScheme.onPrimaryContainer,
                fontWeight = FontWeight.Medium
            )
            Text(
                text = "RM $savedAmount",
                color = MaterialTheme.colorScheme.onPrimaryContainer,
                fontWeight = FontWeight.Bold
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Simple bar row - height scales relative to the max value in the list
        val maxValue = wasteByWeek.maxOrNull() ?: 1
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            wasteByWeek.forEach { value ->
                val barHeight = (value.toFloat() / maxValue) * 50.dp.value
                val barColor = if (value == maxValue) warningContainerLight else MaterialTheme.colorScheme.primary
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(barHeight.dp)
                        .align(Alignment.Bottom)
                        .background(barColor, RoundedCornerShape(6.dp))
                )
            }
        }

        Text(
            "Items wasted, last 4 weeks",
            fontSize = 12.sp,
            color = Color.Gray,
            modifier = Modifier.fillMaxWidth(),
            textAlign = TextAlign.Center
        )
    }
}

@Composable
fun RecipeSuggestionsRow(
    recipes: List<RecipeSuggestion>,
    onSeeAllClick: () -> Unit
) {
    Column {
        SectionHeader(
            title = "Recipe Suggestions",
            onSeeAllClick = onSeeAllClick
        )

        Spacer(modifier = Modifier.height(8.dp))

        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(recipes) { recipe ->
                RecipeCard(recipe)
            }
        }
    }
}

@Composable
fun RecipeCard(
    recipe: RecipeSuggestion
) {
    Card(
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier
            .width(160.dp)
    ) {
        Column(modifier = Modifier
            .padding(12.dp)
        ) {
            Icon(Icons.Default.RestaurantMenu, contentDescription = null)
            Spacer(modifier = Modifier.height(8.dp))
            Text(recipe.name, fontWeight = FontWeight.Bold)
            Text(recipe.usesText, fontSize = 12.sp, color = Color.Gray)
        }
    }
}

@Composable
fun SeeAllText(onClick: () -> Unit) {
    Text(
        text = "See All",
        color = MaterialTheme.colorScheme.primary,
        style = MaterialTheme.typography.labelLarge,
        modifier = Modifier
            .clickable { onClick() }
            .padding(vertical = 4.dp, horizontal = 8.dp)
    )
}

@Composable
fun SectionHeader(
    title: String,
    modifier: Modifier = Modifier,
    icon: ImageVector? = null,
    onSeeAllClick: () -> Unit
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            if (icon != null) {
                Icon(
                    imageVector = icon,
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
        SeeAllText(onSeeAllClick)
    }
}
