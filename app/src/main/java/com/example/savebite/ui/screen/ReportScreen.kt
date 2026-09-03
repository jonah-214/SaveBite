package com.example.savebite.ui.screen

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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.savebite.R
import com.example.savebite.ui.navigation.AppTopBar
import com.example.savebite.ui.theme.getCategoryColor
import com.example.savebite.ui.viewmodel.ReportViewModel
import com.example.savebite.ui.viewmodel.TimeFrame
import com.example.savebite.utils.Currency
import com.example.savebite.utils.PdfReportGenerator

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReportScreen(
    viewModel: ReportViewModel,
    onNavigateToCategoryBreakdown: () -> Unit = {},
    onNavigateToWastedItems: () -> Unit = {},
    onNavigateToConsumedItems: () -> Unit = {}
) {
    val state by viewModel.uiState.collectAsState()
    val context = LocalContext.current

    // 控制下拉菜单显隐
    var dropdownExpanded by remember { mutableStateOf(false) }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            AppTopBar(
                title = "Food Waste Report",
                showBackButton = false,
                actions = {
                    IconButton(onClick = { PdfReportGenerator.generateAndSharePdf(context, state) }) {
                        Icon(
                            painter = painterResource(R.drawable.picture_as_pdf), 
                            contentDescription = "Export PDF", 
                            tint = MaterialTheme.colorScheme.onPrimary,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .padding(horizontal = 16.dp)
                .verticalScroll(rememberScrollState())
        ) {
            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // 左侧：前后切换箭头 + 当前日期范围显示
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    IconButton(
                        onClick = { viewModel.navigatePrevious() },
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            painter = painterResource(R.drawable.chevron_left),
                            contentDescription = "Previous",
                            modifier = Modifier.size(24.dp)
                        )
                    }

                    Text(
                        text = state.dateDisplay,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 4.dp)
                    )

                    IconButton(
                        onClick = { viewModel.navigateNext() },
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            painter = painterResource(R.drawable.chevron_right),
                            contentDescription = "Next",
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.width(8.dp))

                Box {
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant,
                        modifier = Modifier.clickable { dropdownExpanded = true }
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            val label = when (state.selectedTimeFrame) {
                                TimeFrame.WEEKLY -> "Weekly"
                                TimeFrame.MONTHLY -> "Monthly"
                                TimeFrame.YEARLY -> "Yearly"
                            }
                            Text(
                                text = label,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Medium,
                                maxLines = 1
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Icon(
                                painter = painterResource(R.drawable.arrow_down),
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }

                    // 下拉选项列表
                    DropdownMenu(
                        expanded = dropdownExpanded,
                        onDismissRequest = { dropdownExpanded = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text("Weekly") },
                            onClick = {
                                viewModel.setTimeFrame(TimeFrame.WEEKLY)
                                dropdownExpanded = false
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("Monthly") },
                            onClick = {
                                viewModel.setTimeFrame(TimeFrame.MONTHLY)
                                dropdownExpanded = false
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("Yearly") },
                            onClick = {
                                viewModel.setTimeFrame(TimeFrame.YEARLY)
                                dropdownExpanded = false
                            }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Metrics
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                MetricCard(
                    icon = R.drawable.delete,
                    iconBg = MaterialTheme.colorScheme.primaryContainer,
                    iconTint = MaterialTheme.colorScheme.primary,
                    title = "Waste Cost",
                    value = Currency.format(state.totalWastedCost),
                    subtitle = "${state.totalWastedItems} items",
                    modifier = Modifier.weight(1f)
                )
                MetricCard(
                    icon = R.drawable.savings,
                    iconBg = MaterialTheme.colorScheme.primary.copy(alpha = 0.2f),
                    iconTint = MaterialTheme.colorScheme.primary,
                    title = "Saved Value",
                    value = Currency.format(state.totalSavedCost),
                    subtitle = "${state.totalConsumedItems} items",
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Waste Breakdown Card
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Waste Breakdown", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                        TextButton(onClick = onNavigateToCategoryBreakdown) {
                            Text("View More", fontSize = 12.sp)
                            Icon(painter = painterResource(R.drawable.chevron_right), contentDescription = null, modifier = Modifier.size(16.dp))
                        }
                    }
                    if (state.totalWastedItems > 0) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            SolidPieChart(
                                percentages = state.wastedBreakdowns.map { it.percentage },
                                colors = state.wastedBreakdowns.map { getCategoryColor(it.category) },
                                modifier = Modifier.size(140.dp)
                            )
                            Spacer(modifier = Modifier.width(16.dp))
                            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                state.wastedBreakdowns.forEach {
                                    LegendRow(color = getCategoryColor(it.category), label = it.category, count = it.count, percentage = it.percentage.toInt())
                                }
                            }
                        }
                    } else {
                        Box(modifier = Modifier.fillMaxWidth().height(100.dp), contentAlignment = Alignment.Center) {
                            Text("No waste data recorded", color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Top Wasted Card
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Most Wasted Items", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                        TextButton(onClick = onNavigateToWastedItems) {
                            Text("View More", fontSize = 12.sp)
                            Icon(painter = painterResource(R.drawable.chevron_right), contentDescription = null, modifier = Modifier.size(16.dp))
                        }
                    }
                    if (state.topWastedItems.isNotEmpty()) {
                        state.topWastedItems.take(5).forEach {
                            ItemStatRow(name = it.name, count = it.count, percentage = it.percentage, price = it.totalPrice, progressColor = MaterialTheme.colorScheme.primary)
                        }
                    } else {
                        Text("No items found", color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.padding(vertical = 8.dp))
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Consumed Card
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(painter = painterResource(R.drawable.check), contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(20.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Consumed Items", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                        }
                        TextButton(onClick = onNavigateToConsumedItems) {
                            Text("View More", fontSize = 12.sp)
                            Icon(painter = painterResource(R.drawable.chevron_right), contentDescription = null, modifier = Modifier.size(16.dp))
                        }
                    }
                    if (state.topConsumedItems.isNotEmpty()) {
                        state.topConsumedItems.take(5).forEach {
                            ItemStatRow(name = it.name, count = it.count, percentage = it.percentage, price = it.totalPrice, progressColor = MaterialTheme.colorScheme.primary)
                        }
                    } else {
                        Text("No consumed items recorded", color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.padding(vertical = 8.dp))
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            ReportSectionCard(
                title = "Waste Reasons",
                icon = R.drawable.list,
                iconTint = MaterialTheme.colorScheme.primary,
                onViewMore = null
            ) {
                if (state.reasonBreakdowns.isNotEmpty()) {
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        state.reasonBreakdowns.forEach { reason ->
                            ItemStatRow(
                                name = reason.reason,
                                count = reason.count,
                                percentage = reason.percentage,
                                progressColor = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                } else {
                    EmptyStatePlaceholder(message = "No waste reasons recorded for this period")
                }
            }
        }
    }
}