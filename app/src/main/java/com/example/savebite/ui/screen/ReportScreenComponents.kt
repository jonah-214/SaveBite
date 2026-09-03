package com.example.savebite.ui.screen

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.painterResource
import com.example.savebite.R
import com.example.savebite.utils.Currency
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import java.text.DateFormatSymbols

@Composable
fun MetricCard(
    icon: Int,
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
                Icon(painter = painterResource(id = icon), contentDescription = null, tint = iconTint, modifier = Modifier.size(24.dp))
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

            drawArc(color = color, startAngle = startAngle, sweepAngle = sweepAngle, useCenter = true)

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
                    topLeft = Offset(textX - textLayoutResult.size.width / 2f, textY - textLayoutResult.size.height / 2f)
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
            modifier = Modifier.padding(16.dp)
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text("Select Month & Year", style = MaterialTheme.typography.titleLarge)
                Spacer(modifier = Modifier.height(24.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = { year-- }) { Icon(painter = painterResource(R.drawable.chevron_left), null, modifier = Modifier.size(24.dp)) }
                    Text("$year", fontSize = 20.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 16.dp))
                    IconButton(onClick = { year++ }) { Icon(painter = painterResource(R.drawable.chevron_right), null, modifier = Modifier.size(24.dp)) }
                }
                Spacer(modifier = Modifier.height(16.dp))
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
                                        color = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }
                    }
                }
                Spacer(modifier = Modifier.height(24.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                    TextButton(onClick = onDismiss) { Text("Cancel") }
                    TextButton(onClick = { onConfirm(month, year) }) { Text("Confirm") }
                }
            }
        }
    }
}

@Composable
fun ItemStatRow(
    name: String,
    count: Int,
    percentage: Float,
    price: Double = 0.0,
    progressColor: Color
) {
    // Read via LocalConfiguration (observable) instead of Locale.getDefault() (not observable) —
    // so this recomposes if the user changes the system language while the app is running.
    val locale = LocalConfiguration.current.locales[0]

    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = name.replaceFirstChar { if (it.isLowerCase()) it.titlecase(locale) else it.toString() },
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp
            )
            val displayText = if (price > 0) {
                Currency.formatWithCount(price, count)
            } else {
                "$count items (${percentage.toInt()}%)"
            }
            Text(text = displayText, fontWeight = FontWeight.SemiBold, fontSize = 14.sp, color = progressColor)
        }
        Spacer(modifier = Modifier.height(8.dp))
        LinearProgressIndicator(
            progress = { percentage / 100f },
            modifier = Modifier.fillMaxWidth().height(8.dp),
            color = progressColor,
            trackColor = progressColor.copy(alpha = 0.2f),
        )
    }
}

@Composable
fun ReasonRow(reason: String, count: Int, percentage: Float) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(reason, fontSize = 14.sp, fontWeight = FontWeight.Medium)
            LinearProgressIndicator(
                progress = { (percentage / 100f).coerceIn(0f, 1f) },
                modifier = Modifier.fillMaxWidth(0.8f).height(4.dp).clip(RoundedCornerShape(2.dp)),
                color = MaterialTheme.colorScheme.primary,
                trackColor = MaterialTheme.colorScheme.surfaceVariant
            )
        }
        Text("$count items (${percentage.toInt()}%)", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

@Composable
fun LegendRow(color: Color, label: String, count: Int, percentage: Int) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(modifier = Modifier.size(10.dp).background(color, CircleShape))
        Spacer(modifier = Modifier.width(8.dp))
        Column {
            Text(label, fontSize = 12.sp, fontWeight = FontWeight.Bold)
            Text("$count items ($percentage%)", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
fun ReportSectionCard(
    title: String,
    icon: Int,
    iconTint: Color,
    onViewMore: (() -> Unit)? = null,
    content: @Composable ColumnScope.() -> Unit
) {
    Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(painter = painterResource(id = icon), contentDescription = null, tint = iconTint, modifier = Modifier.size(20.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(title, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                }
                if (onViewMore != null) {
                    TextButton(onClick = onViewMore) {
                        Text("View More", fontSize = 12.sp)
                        Icon(painter = painterResource(R.drawable.chevron_right), contentDescription = null, modifier = Modifier.size(16.dp))
                    }
                }
            }
            Spacer(modifier = Modifier.height(12.dp))
            content()
        }
    }
}

@Composable
fun EmptyStatePlaceholder(message: String) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 16.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = message,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            fontSize = 14.sp
        )
    }
}