package com.example.savebite.ui.screen

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.FormatListBulleted
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.outlined.CheckCircleOutline
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.FormatListBulleted
import androidx.compose.material.icons.outlined.PictureAsPdf
import androidx.compose.material.icons.outlined.Savings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.savebite.ui.navigation.AppTopBar
import com.example.savebite.ui.theme.VegGreen
import com.example.savebite.ui.theme.getCategoryColor
import com.example.savebite.ui.viewmodel.ReportViewModel
import com.example.savebite.ui.viewmodel.TimeFrame
import com.example.savebite.utils.PdfReportGenerator
import java.text.DateFormatSymbols
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReportScreen(
    viewModel: ReportViewModel,
    onNavigateToCategoryBreakdown: () -> Unit = {},
    onNavigateToWastedItems: () -> Unit = {},
    onNavigateToConsumedItems: () -> Unit = {}
) {
    val state by viewModel.uiState.collectAsState()
    var showMonthPicker by remember { mutableStateOf(false) }
    val context = LocalContext.current

    if (showMonthPicker) {
        MonthYearPicker(
            initialMonth = state.selectedMonth,
            initialYear = state.selectedYear,
            onDismiss = { showMonthPicker = false },
            onConfirm = { month, year ->
                viewModel.selectMonthYear(month, year)
                showMonthPicker = false
            }
        )
    }

    val monthName = DateFormatSymbols().months[state.selectedMonth]
    val dateDisplay = if (state.selectedTimeFrame == TimeFrame.WEEKLY) {
        "$monthName ${state.selectedYear} (Week ${state.selectedWeek})"
    } else {
        "$monthName ${state.selectedYear}"
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            AppTopBar(
                title = "Food Waste Report",
                showBackButton = false,
                actions = {
                    IconButton(onClick = { PdfReportGenerator.generateAndSharePdf(context, state) }) {
                        Icon(Icons.Outlined.PictureAsPdf, contentDescription = "Export PDF", tint = MaterialTheme.colorScheme.onPrimary)
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
            Spacer(modifier = Modifier.height(16.dp))

            // Time Filter Section
            SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
                SegmentedButton(
                    selected = state.selectedTimeFrame == TimeFrame.MONTHLY,
                    onClick = { viewModel.setTimeFrame(TimeFrame.MONTHLY) },
                    shape = SegmentedButtonDefaults.itemShape(index = 0, count = 2)
                ) { Text("Monthly") }
                SegmentedButton(
                    selected = state.selectedTimeFrame == TimeFrame.WEEKLY,
                    onClick = { viewModel.setTimeFrame(TimeFrame.WEEKLY) },
                    shape = SegmentedButtonDefaults.itemShape(index = 1, count = 2)
                ) { Text("Weekly") }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Summary", fontWeight = FontWeight.Bold, fontSize = 18.sp)
                OutlinedButton(
                    onClick = { showMonthPicker = true },
                    shape = RoundedCornerShape(16.dp),
                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 2.dp),
                    modifier = Modifier.height(32.dp)
                ) {
                    Icon(Icons.Default.CalendarMonth, contentDescription = null, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(dateDisplay, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                }
            }

            if (state.selectedTimeFrame == TimeFrame.WEEKLY) {
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    (1..5).forEach { weekNum ->
                        FilterChip(
                            selected = state.selectedWeek == weekNum,
                            onClick = { viewModel.selectWeek(weekNum) },
                            label = { Text("Week $weekNum") }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Metrics
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                MetricCard(
                    icon = Icons.Outlined.Delete,
                    iconBg = MaterialTheme.colorScheme.primaryContainer,
                    iconTint = MaterialTheme.colorScheme.primary,
                    title = "Waste Cost",
                    value = String.format(Locale.getDefault(), "RM %.2f", state.totalWastedCost),
                    subtitle = "${state.totalWastedItems} items",
                    modifier = Modifier.weight(1f)
                )
                MetricCard(
                    icon = Icons.Outlined.Savings,
                    iconBg = VegGreen.copy(alpha = 0.2f),
                    iconTint = VegGreen,
                    title = "Saved Value",
                    value = String.format(Locale.getDefault(), "RM %.2f", state.totalSavedCost),
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
                            Icon(Icons.Default.ChevronRight, contentDescription = null, modifier = Modifier.size(16.dp))
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
                            Icon(Icons.Default.ChevronRight, contentDescription = null, modifier = Modifier.size(16.dp))
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
                            Icon(Icons.Outlined.CheckCircleOutline, contentDescription = null, tint = VegGreen, modifier = Modifier.size(20.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Consumed Items", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                        }
                        TextButton(onClick = onNavigateToConsumedItems) {
                            Text("View More", fontSize = 12.sp)
                            Icon(Icons.Default.ChevronRight, contentDescription = null, modifier = Modifier.size(16.dp))
                        }
                    }
                    if (state.topConsumedItems.isNotEmpty()) {
                        state.topConsumedItems.take(5).forEach {
                            ItemStatRow(name = it.name, count = it.count, percentage = it.percentage, price = it.totalPrice, progressColor = VegGreen)
                        }
                    } else {
                        Text("No consumed items recorded", color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.padding(vertical = 8.dp))
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            ReportSectionCard(
                title = "Waste Reasons",
                icon = Icons.Outlined.FormatListBulleted,
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