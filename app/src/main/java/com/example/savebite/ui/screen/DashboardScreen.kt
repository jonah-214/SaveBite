package com.example.savebite.ui.screen

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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.RestaurantMenu
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import com.example.savebite.ui.navigation.AppRoutes
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
    dashboardViewModel: DashboardViewModel = viewModel()
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        DashboardHeader(
            username = "John Doe",
            onProfileClick = {
                navController.navigate(AppRoutes.PROFILE)
            }
        )

        Spacer(modifier = Modifier.height(16.dp))

        ExpiryReminderCard(
            items = dashboardViewModel.expiringItems,
            onSeeAllClick = { navController.navigate(AppRoutes.REMINDER) }
        )
        Spacer(modifier = Modifier.height(16.dp))

        StatsRow(
            inventoryCount = dashboardViewModel.inventoryCount,
            shoppingCount = dashboardViewModel.shoppingListCount,
            onInventoryClick = { navController.navigate(AppRoutes.INVENTORY) },
            onShoppingClick = { navController.navigate(AppRoutes.SHOPPING) }
        )
        Spacer(modifier = Modifier.height(16.dp))

        WasteReportSection(
            savedAmount = dashboardViewModel.savedThisMonth,
            wasteByWeek = dashboardViewModel.wasteTrackerData,
            onSeeAllClick = { /* navigate to Waste Tracker later */ }
        )
        Spacer(modifier = Modifier.height(16.dp))

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
        modifier = Modifier
            .fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column {
            Text("Good Morning, $username", fontWeight = FontWeight.Bold, fontSize = 20.sp)
            Text("Here's what needs your attention", fontSize = 13.sp)
        }

        IconButton(
            onClick = onProfileClick
        ) {
            Icon(
                imageVector = Icons.Default.AccountCircle,
                contentDescription = "Profile",
                modifier = Modifier
                    .size(40.dp)
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
        colors = CardDefaults.cardColors(containerColor = Color(0xFFF6D9B0)),
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier
            .fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
            .padding(16.dp)
        ) {

            Row(
                modifier = Modifier
                    .fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Notifications,
                        contentDescription = null
                    )

                    Spacer(modifier = Modifier.width(8.dp))

                    Text(
                        text = "Expiry Reminder",
                        fontWeight = FontWeight.Bold
                    )
                }

                Text(
                    text = "See All",
                    color = Color.Blue,
                    modifier = Modifier
                        .clickable { onSeeAllClick() }
                )
            }

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
            .background(Color(0xFFE0E0E0),
                RoundedCornerShape(12.dp))
            .padding(12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text("${item.name} • ${item.quantity}")

        val badgeColor = if (item.daysLeft <= 1) Color(0xFFE57373) else Color(0xFFF6B96C)
        Text(
            text = "${item.daysLeft} days",
            color = Color.Black,
            modifier = Modifier
                .background(badgeColor, RoundedCornerShape(50))
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
            backgroundColor = Color(0xFF3E7B3E),
            modifier = Modifier
                .weight(1f),
            onClick = onInventoryClick
        )
        StatsCard(
            count = shoppingCount,
            label = "Shopping List",
            backgroundColor = Color(0xFFE8A33D),
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
    backgroundColor: Color,
    modifier: Modifier,
    onClick: () -> Unit
) {
    Card(
        modifier = modifier
            .clickable { onClick() },
        colors = CardDefaults.cardColors(containerColor = backgroundColor),
        shape = RoundedCornerShape(16.dp),
    ) {
        Column(
            modifier = Modifier
                .padding(16.dp),
        ) {
            Text(
                text = "$count",
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
            Text(
                text = label,
                color = Color.White
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
        Row(
            modifier = Modifier
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text ="Waste Report",
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp
            )
            Text(
                text = "See All",
                color = Color.Blue,
                modifier = Modifier
                    .clickable { onSeeAllClick() }
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(0xFFB9D3F7),
                    RoundedCornerShape(16.dp))
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = "Saved This Month",
                color = Color(0xFF1A3D7C),
                fontWeight = FontWeight.Bold
            )
            Text(
                "RM $savedAmount",
                color = Color(0xFF1A3D7C),
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
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(barHeight.dp)
                        .align(Alignment.Bottom)
                        .background(Color(0xFF3E7B3E), RoundedCornerShape(6.dp))
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
        Row(
            modifier = Modifier
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = "Recipe Suggestions",
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp
            )
            Text(
                text = "See All",
                color = Color.Blue,
                modifier = Modifier.clickable { onSeeAllClick() }
            )
        }

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