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
import androidx.compose.material.icons.outlined.EmojiEvents
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.savebite.ui.navigation.AppTopBar
import com.example.savebite.ui.viewmodel.ReportViewModel
import java.text.DateFormatSymbols

// Visualization Colors
val VegGreen = Color(0xFF55823B)
val FruitOrange = Color(0xFFECA338)
val GrainBlue = Color(0xFF4A84C4)
val OtherPurple = Color(0xFF8E63B4)
val CategoryColorsList = listOf(VegGreen, FruitOrange, GrainBlue, OtherPurple)

@Composable
fun ReportScreen(viewModel: ReportViewModel) {
    val state by viewModel.uiState.collectAsState()
    var showMonthPicker by remember { mutableStateOf(false) }

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
                showBackButton = false
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

            // 改动 1：Waste Summary 标题 + 放在右侧的小巧日历按钮
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

                // 精简版胶囊日历按钮
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

            // Summary Metric Cards
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                MetricCard(
                    icon = Icons.Outlined.Delete,
                    iconBg = MaterialTheme.colorScheme.primaryContainer,
                    iconTint = MaterialTheme.colorScheme.primary,
                    title = "Total Items",
                    value = "${state.totalItems} items",
                    subtitle = "Wasted",
                    modifier = Modifier.weight(1f)
                )
                MetricCard(
                    icon = Icons.Outlined.EmojiEvents,
                    iconBg = MaterialTheme.colorScheme.tertiaryContainer,
                    iconTint = MaterialTheme.colorScheme.onTertiaryContainer,
                    title = "Most Wasted",
                    value = state.mostWastedName.ifEmpty { "-" },
                    subtitle = "${state.mostWastedCount} items",
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Waste Breakdown Card
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
                        Text("Waste Breakdown", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = MaterialTheme.colorScheme.onSurface)
                        TextButton(onClick = { /* View More */ }, contentPadding = PaddingValues(0.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text("View More", fontSize = 12.sp, color = MaterialTheme.colorScheme.primary)
                                Icon(Icons.Default.ChevronRight, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(16.dp))
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    if (state.totalItems > 0) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            SolidPieChart(
                                percentages = state.breakdowns.map { it.percentage },
                                colors = CategoryColorsList,
                                modifier = Modifier.size(140.dp)
                            )

                            Spacer(modifier = Modifier.width(16.dp))

                            Column(
                                verticalArrangement = Arrangement.spacedBy(10.dp),
                                modifier = Modifier.weight(1f)
                            ) {
                                state.breakdowns.forEachIndexed { index, item ->
                                    LegendRow(
                                        color = CategoryColorsList.getOrElse(index) { Color.Gray },
                                        label = item.category,
                                        count = item.count,
                                        percentage = item.percentage.toInt()
                                    )
                                }
                            }
                        }
                    } else {
                        Box(
                            modifier = Modifier.fillMaxWidth().height(140.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("No waste data for this month", color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Most Wasted Items Card
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
                        Text("Most Wasted Items", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = MaterialTheme.colorScheme.onSurface)
                        TextButton(onClick = { /* View More */ }, contentPadding = PaddingValues(0.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text("View More", fontSize = 12.sp, color = MaterialTheme.colorScheme.primary)
                                Icon(Icons.Default.ChevronRight, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(16.dp))
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    if (state.topItems.isNotEmpty()) {
                        state.topItems.take(5).forEach { item ->
                            ItemStatRow(
                                name = item.name,
                                count = item.count,
                                percentage = item.percentage,
                                progressColor = MaterialTheme.colorScheme.primary
                            )
                        }
                    } else {
                        Text("No items found", color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.padding(vertical = 16.dp))
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // 改动 2：底部 Consumed Items 卡片
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
                                imageVector = Icons.Outlined.CheckCircleOutline,
                                contentDescription = null,
                                tint = VegGreen,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Consumed Items", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = MaterialTheme.colorScheme.onSurface)
                        }
                        TextButton(onClick = { /* View More */ }, contentPadding = PaddingValues(0.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text("View More", fontSize = 12.sp, color = MaterialTheme.colorScheme.primary)
                                Icon(Icons.Default.ChevronRight, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(16.dp))
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // 绑定显示的 Consumed 食材列表
                    if (state.topItems.isNotEmpty()) {
                        state.topItems.take(5).forEach { item ->
                            ItemStatRow(
                                name = item.name,
                                count = item.count,
                                percentage = item.percentage,
                                progressColor = VegGreen
                            )
                        }
                    } else {
                        Text("No consumed items recorded for this month", color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.padding(vertical = 16.dp))
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
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
                        fontSize = 12.sp,
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
            modifier = Modifier.padding(16.dp),
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

            Column {
                Text(title, color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 11.sp)
                Text(value, fontWeight = FontWeight.Bold, fontSize = 17.sp, color = MaterialTheme.colorScheme.onSurface)
                Text(subtitle, color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 11.sp)
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

@Composable
fun ItemStatRow(
    name: String,
    count: Int,
    percentage: Float,
    progressColor: Color = MaterialTheme.colorScheme.primary
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(getItemEmoji(name), fontSize = 20.sp)
        Spacer(modifier = Modifier.width(10.dp))
        Text(
            text = name,
            fontWeight = FontWeight.SemiBold,
            fontSize = 13.sp,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.width(70.dp)
        )

        Spacer(modifier = Modifier.width(8.dp))

        LinearProgressIndicator(
            progress = { (percentage / 100f).coerceIn(0f, 1f) },
            color = progressColor,
            trackColor = MaterialTheme.colorScheme.surfaceVariant,
            modifier = Modifier
                .weight(1f)
                .height(4.dp)
                .clip(RoundedCornerShape(2.dp))
        )

        Spacer(modifier = Modifier.width(12.dp))

        Text(
            "$count",
            fontSize = 12.sp,
            color = MaterialTheme.colorScheme.onSurface,
            fontWeight = FontWeight.Medium
        )

        Spacer(modifier = Modifier.width(12.dp))

        Text(
            "${percentage.toInt()}%",
            fontSize = 12.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.width(30.dp)
        )
    }
}

private fun getItemEmoji(name: String): String {
    return when (name.trim().lowercase()) {
        "lettuce" -> "🥬"
        "tomato", "tomatoes" -> "🍅"
        "bread" -> "🍞"
        "banana", "bananas" -> "🍌"
        "potato", "potatoes" -> "🥔"
        "apple", "apples" -> "🍎"
        "milk" -> "🥛"
        "egg", "eggs" -> "🥚"
        "cheese" -> "🧀"
        "chicken" -> "🍗"
        else -> "🥗"
    }
}