package com.example.savebite.ui.screen

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.outlined.CheckCircleOutline
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.FormatListBulleted
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.PieChart
import androidx.compose.material.icons.outlined.PictureAsPdf
import androidx.compose.material.icons.outlined.Savings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.savebite.ui.navigation.AppTopBar
import com.example.savebite.ui.theme.VegGreen
import com.example.savebite.ui.theme.getCategoryColor
import com.example.savebite.ui.viewmodel.ReportViewModel
import com.example.savebite.utils.PdfReportGenerator
import java.text.DateFormatSymbols
import java.util.Locale

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
                viewModel.selectMonth(month, year)
                showMonthPicker = false
            }
        )
    }

    val monthName = DateFormatSymbols().months[state.selectedMonth]
    val dateDisplay = "$monthName ${state.selectedYear}"

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            AppTopBar(
                title = "Food Waste Report",
                showBackButton = false,
                actions = {
                    IconButton(
                        onClick = {
                            PdfReportGenerator.generateAndSharePdf(context, state)
                        }
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.PictureAsPdf,
                            contentDescription = "Export PDF Report",
                            tint = MaterialTheme.colorScheme.onPrimary
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
            Spacer(modifier = Modifier.height(16.dp))

            // Waste Summary 顶部区域与筛选按钮
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Waste Summary",
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    color = MaterialTheme.colorScheme.onBackground
                )

                OutlinedButton(
                    onClick = { showMonthPicker = true },
                    shape = RoundedCornerShape(16.dp),
                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 2.dp),
                    modifier = Modifier.height(32.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.4f)),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = MaterialTheme.colorScheme.primary
                    )
                ) {
                    Icon(
                        imageVector = Icons.Default.CalendarMonth,
                        contentDescription = null,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = dateDisplay,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // 1. Metric Cards
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                MetricCard(
                    icon = Icons.Outlined.Delete,
                    iconBg = MaterialTheme.colorScheme.primaryContainer,
                    iconTint = MaterialTheme.colorScheme.primary,
                    title = "Items Wasted",
                    value = "${state.totalWastedItems} items",
                    subtitle = "Wasted",
                    modifier = Modifier.weight(1f)
                )
                MetricCard(
                    icon = Icons.Outlined.Savings,
                    iconBg = VegGreen.copy(alpha = 0.2f),
                    iconTint = VegGreen,
                    title = "Saved Amount",
                    value = String.format(Locale.getDefault(), "RM %.2f", state.totalSavedCost),
                    subtitle = "Saved",
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // 2. Waste Breakdown Card
            ReportSectionCard(
                title = "Waste Breakdown",
                icon = Icons.Outlined.PieChart,
                iconTint = MaterialTheme.colorScheme.primary,
                onViewMore = onNavigateToCategoryBreakdown
            ) {
                if (state.totalWastedItems > 0) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        SolidPieChart(
                            percentages = state.wastedBreakdowns.map { it.percentage },
                            colors = state.wastedBreakdowns.map { getCategoryColor(it.category) },
                            modifier = Modifier.size(130.dp)
                        )

                        Spacer(modifier = Modifier.width(16.dp))

                        Column(
                            verticalArrangement = Arrangement.spacedBy(10.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            state.wastedBreakdowns.forEach { item ->
                                LegendRow(
                                    color = getCategoryColor(item.category),
                                    label = item.category,
                                    count = item.count,
                                    percentage = item.percentage.toInt()
                                )
                            }
                        }
                    }
                } else {
                    EmptyStatePlaceholder(message = "No waste data for this month")
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // 3. Most Wasted Items Card
            ReportSectionCard(
                title = "Most Wasted Items",
                icon = Icons.Outlined.Delete,
                iconTint = MaterialTheme.colorScheme.error,
                onViewMore = onNavigateToWastedItems
            ) {
                if (state.topWastedItems.isNotEmpty()) {
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        state.topWastedItems.take(5).forEach { item ->
                            ItemStatRow(
                                name = item.name,
                                count = item.count,
                                percentage = item.percentage,
                                price = item.totalPrice,
                                progressColor = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                } else {
                    EmptyStatePlaceholder(message = "No wasted items found for this month")
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // 4. Waste Reasons Card
            ReportSectionCard(
                title = "Waste Reasons",
                icon = Icons.Outlined.FormatListBulleted,
                iconTint = MaterialTheme.colorScheme.primary,
                onViewMore = null // 不跳转，故不传
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
                    EmptyStatePlaceholder(message = "No waste reasons recorded for this month")
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // 5. Consumed Items Card
            ReportSectionCard(
                title = "Consumed Items",
                icon = Icons.Outlined.CheckCircleOutline,
                iconTint = VegGreen,
                onViewMore = onNavigateToConsumedItems
            ) {
                if (state.topConsumedItems.isNotEmpty()) {
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        state.topConsumedItems.take(5).forEach { item ->
                            ItemStatRow(
                                name = item.name,
                                count = item.count,
                                percentage = item.percentage,
                                price = item.totalPrice,
                                progressColor = VegGreen
                            )
                        }
                    }
                } else {
                    EmptyStatePlaceholder(message = "No consumed items recorded for this month")
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

// 统一包装容器卡片组件
@Composable
fun ReportSectionCard(
    title: String,
    icon: ImageVector,
    iconTint: Color,
    onViewMore: (() -> Unit)? = null,
    content: @Composable () -> Unit
) {
    Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = iconTint,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = title,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                if (onViewMore != null) {
                    TextButton(
                        onClick = onViewMore,
                        contentPadding = PaddingValues(0.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text("View More", fontSize = 12.sp, color = MaterialTheme.colorScheme.primary)
                            Icon(
                                imageVector = Icons.Default.ChevronRight,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            content()
        }
    }
}

// 统一的 Empty State 组件
@Composable
fun EmptyStatePlaceholder(message: String) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 16.dp),
        contentAlignment = Alignment.Center
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = Icons.Outlined.Info,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                modifier = Modifier.size(16.dp)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = message,
                fontSize = 13.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f)
            )
        }
    }
}

@Composable
fun MetricCard(
    icon: ImageVector,
    iconBg: Color,
    iconTint: Color,
    title: String,
    value: String,
    subtitle: String,
    modifier: Modifier = Modifier
) {
    Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
        modifier = modifier
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(46.dp)
                    .background(iconBg, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = iconTint,
                    modifier = Modifier.size(24.dp)
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = title,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 11.sp,
                    maxLines = 1
                )
                Text(
                    text = value,
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp, // 微调字号防止在部分小屏设备上挤压变形
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1
                )
            }
        }
    }
}

@Composable
fun LegendRow(color: Color, label: String, count: Int, percentage: Int) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier
                .size(10.dp)
                .background(color, CircleShape)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Column {
            Text(label, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
            Text(
                "$count items ($percentage%)",
                fontSize = 11.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

// 统一的列表条目进度条组件（兼容物品统计与原因统计）
@Composable
fun ItemStatRow(
    name: String,
    count: Int,
    percentage: Float,
    price: Double = 0.0,
    progressColor: Color
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = name.replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString() },
                fontWeight = FontWeight.SemiBold,
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.onSurface
            )
            val displayText = if (price > 0) {
                String.format(Locale.getDefault(), "RM %.2f (%d items)", price, count)
            } else {
                "$count items (${percentage.toInt()}%)"
            }
            Text(
                text = displayText,
                fontWeight = FontWeight.Bold,
                fontSize = 13.sp,
                color = progressColor
            )
        }
        Spacer(modifier = Modifier.height(6.dp))
        LinearProgressIndicator(
            progress = { (percentage / 100f).coerceIn(0f, 1f) },
            modifier = Modifier
                .fillMaxWidth()
                .height(6.dp)
                .clip(RoundedCornerShape(3.dp)),
            color = progressColor,
            trackColor = progressColor.copy(alpha = 0.15f),
        )
    }
}

@Composable
fun SolidPieChart(
    percentages: List<Float>,
    colors: List<Color>,
    modifier: Modifier = Modifier
) {
    val textMeasurer = rememberTextMeasurer()

    Canvas(modifier = modifier) {
        val radius = size.minDimension / 2f
        val center = Offset(size.width / 2f, size.height / 2f)
        var startAngle = -90f

        percentages.forEachIndexed { index, pct ->
            if (pct <= 0f) return@forEachIndexed
            val sweepAngle = (pct / 100f) * 360f
            val color = colors.getOrElse(index) { Color.Gray }

            drawArc(
                color = color,
                startAngle = startAngle,
                sweepAngle = sweepAngle,
                useCenter = true
            )

            if (pct >= 8f) {
                val midAngleRad = Math.toRadians((startAngle + sweepAngle / 2f).toDouble())
                val textRadius = radius * 0.62f
                val textX = center.x + textRadius * kotlin.math.cos(midAngleRad).toFloat()
                val textY = center.y + textRadius * kotlin.math.sin(midAngleRad).toFloat()

                val textLayoutResult = textMeasurer.measure(
                    text = "${pct.toInt()}%",
                    style = androidx.compose.ui.text.TextStyle(
                        color = Color.White,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                )

                drawText(
                    textLayoutResult = textLayoutResult,
                    topLeft = Offset(
                        textX - textLayoutResult.size.width / 2f,
                        textY - textLayoutResult.size.height / 2f
                    )
                )
            }

            startAngle += sweepAngle
        }
    }
}

@Composable
fun MonthYearPicker(
    initialMonth: Int,
    initialYear: Int,
    onDismiss: () -> Unit,
    onConfirm: (Int, Int) -> Unit
) {
    var month by remember { mutableStateOf(initialMonth) }
    var year by remember { mutableStateOf(initialYear) }

    val months = DateFormatSymbols().months

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(28.dp),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 6.dp,
            modifier = Modifier.padding(16.dp)
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Select Month & Year",
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.onSurface
                )

                Spacer(modifier = Modifier.height(24.dp))

                // Year Selector
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    IconButton(onClick = { year-- }) {
                        Icon(Icons.Default.ChevronLeft, contentDescription = "Prev Year")
                    }
                    Text(
                        text = year.toString(),
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 16.dp)
                    )
                    IconButton(onClick = { year++ }) {
                        Icon(Icons.Default.ChevronRight, contentDescription = "Next Year")
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Months Grid
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    for (row in 0 until 4) {
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            for (col in 0 until 3) {
                                val m = row * 3 + col
                                val isSelected = m == month
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant)
                                        .clickable { month = m }
                                        .padding(vertical = 12.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = months[m].take(3),
                                        color = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                        fontSize = 14.sp
                                    )
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onDismiss) {
                        Text("Cancel")
                    }
                    TextButton(onClick = { onConfirm(month, year) }) {
                        Text("Confirm")
                    }
                }
            }
        }
    }
}