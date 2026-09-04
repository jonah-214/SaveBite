package com.example.savebite.ui.screen

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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.LinearProgressIndicator
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.savebite.model.ReportStatus
import com.example.savebite.ui.navigation.AppTopBar
import com.example.savebite.ui.viewmodel.ReportViewModel
import com.example.savebite.utils.Currency

@Composable
fun ReportItemListScreen(
    type: ReportStatus,
    viewModel: ReportViewModel,
    onBackClick: () -> Unit
) {
    val state by viewModel.uiState.collectAsState()

    val isWasted = type == ReportStatus.WASTED
    val title = if (isWasted) "Wasted Items History" else "Consumed Items History"
    val items = if (isWasted) state.topWastedItems else state.topConsumedItems

    val totalCount = items.sumOf { it.count }
    val totalCost = items.sumOf { it.totalPrice }

    val themeColor = if (isWasted) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary
    val headerBgColor = if (isWasted) MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.4f)
    else MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)

    Scaffold(
        topBar = {
            AppTopBar(
                title = title,
                showBackButton = true,
                onBackClick = onBackClick
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp)
        ) {
            Spacer(modifier = Modifier.height(16.dp))

            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = headerBgColor),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = if (isWasted) "Food Waste Cost" else "Consumed Food Value",
                            fontSize = 13.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontWeight = FontWeight.Medium
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = Currency.format(totalCost),
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Bold,
                            color = themeColor
                        )
                    }

                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = themeColor.copy(alpha = 0.2f)
                    ) {
                        Text(
                            text = "$totalCount Items",
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = themeColor
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            if (items.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "No items found for this month",
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    contentPadding = PaddingValues(bottom = 24.dp)
                ) {
                    items(items) { item ->
                        ReportItemRowCard(
                            name = item.name,
                            count = item.count,
                            percentage = item.percentage,
                            totalPrice = item.totalPrice,
                            progressColor = themeColor
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun ReportItemRowCard(
    name: String,
    count: Int,
    percentage: Float,
    totalPrice: Double,
    progressColor: Color
) {
    // Read via LocalConfiguration (observable) instead of Locale.getDefault() (not observable) —
    // so this recomposes if the user changes the system language while the app is running.
    val locale = LocalConfiguration.current.locales[0]

    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(14.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = name.replaceFirstChar { if (it.isLowerCase()) it.titlecase(locale) else it.toString() },
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "$count items (${percentage.toInt()}%)",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Text(
                    text = Currency.format(totalPrice),
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp,
                    color = progressColor
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            LinearProgressIndicator(
                progress = { (percentage / 100f).coerceIn(0f, 1f) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(6.dp)
                    .clip(RoundedCornerShape(3.dp)),
                color = progressColor,
                trackColor = progressColor.copy(alpha = 0.15f)
            )
        }
    }
}
