package com.example.savebite.ui.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.savebite.model.ReportStatus
import com.example.savebite.ui.navigation.AppTopBar
import com.example.savebite.ui.viewmodel.InventoryViewModel

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun InventoryItemToReportScreen(
    viewModel: InventoryViewModel,
    targetStatus: ReportStatus = ReportStatus.CONSUMED,
    onBackClick: () -> Unit,
    onSuccess: () -> Unit
) {
    val items by viewModel.inventoryList.collectAsState()
    val selectedItems = remember(items) { items.filter { it.isConsumed } }

    val isConsumed = targetStatus == ReportStatus.CONSUMED

    // 快捷浪费原因列表
    val quickReasons = listOf("Expired", "Spoiled", "Leftover", "Damaged", "Other")
    var selectedReason by remember { mutableStateOf("Expired") }
    var customReason by remember { mutableStateOf("") }

    // 数量映射表
    val itemQuantities = remember(selectedItems) {
        mutableStateMapOf<String, Int>().apply {
            selectedItems.forEach { this[it.id] = it.quantity }
        }
    }

    // 主色调：Consumed -> 自然绿；Wasted -> 警示红/橘
    val primaryThemeColor = if (isConsumed) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error
    val bgHeaderColor = if (isConsumed) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.errorContainer

    Scaffold(
        topBar = {
            AppTopBar(
                title = if (isConsumed) "Report Consumed" else "Report Wasted",
                showBackButton = true,
                onBackClick = onBackClick
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp, vertical = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Header 状态卡片
            Surface(
                color = bgHeaderColor,
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (isConsumed) "🍽️" else "🗑️",
                        fontSize = 32.sp
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = if (isConsumed) "Confirm Consumption" else "Confirm Waste",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = primaryThemeColor
                        )
                        Text(
                            text = "Adjust quantity for selected items below",
                            fontSize = 12.sp,
                            color = primaryThemeColor.copy(alpha = 0.8f)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // 物品列表微调数量
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(selectedItems, key = { it.id }) { item ->
                    val currentMoveQty = itemQuantities[item.id] ?: item.quantity

                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = MaterialTheme.colorScheme.surface,
                        tonalElevation = 1.dp,
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(
                                width = 1.dp,
                                color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f),
                                shape = RoundedCornerShape(12.dp)
                            )
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = item.name,
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.SemiBold
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = "In stock: ${item.quantity} ${item.unit}",
                                    fontSize = 12.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }

                            // - 数量 + 操作
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Surface(
                                    shape = CircleShape,
                                    color = MaterialTheme.colorScheme.surfaceVariant,
                                    modifier = Modifier
                                        .size(30.dp)
                                        .clickable {
                                            if (currentMoveQty > 1) {
                                                itemQuantities[item.id] = currentMoveQty - 1
                                            }
                                        }
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Icon(
                                            Icons.Default.Remove,
                                            contentDescription = "Decrease",
                                            modifier = Modifier.size(14.dp)
                                        )
                                    }
                                }

                                Text(
                                    text = "$currentMoveQty",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 15.sp,
                                    modifier = Modifier.padding(horizontal = 2.dp)
                                )

                                Surface(
                                    shape = CircleShape,
                                    color = MaterialTheme.colorScheme.surfaceVariant,
                                    modifier = Modifier
                                        .size(30.dp)
                                        .clickable {
                                            if (currentMoveQty < item.quantity) {
                                                itemQuantities[item.id] = currentMoveQty + 1
                                            }
                                        }
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Icon(
                                            Icons.Default.Add,
                                            contentDescription = "Increase",
                                            modifier = Modifier.size(14.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // 如果是 WASTED，提供一键快捷原因选择（不需要一个个填）
            if (!isConsumed) {
                Spacer(modifier = Modifier.height(8.dp))
                Column(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = "Reason for waste:",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(6.dp))

                    // 快捷原因 Chips 流式排布
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        quickReasons.forEach { reason ->
                            val isSelected = selectedReason == reason
                            FilterChip(
                                selected = isSelected,
                                onClick = { selectedReason = reason },
                                label = { Text(reason, fontSize = 12.sp) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = primaryThemeColor.copy(alpha = 0.15f),
                                    selectedLabelColor = primaryThemeColor
                                )
                            )
                        }
                    }

                    // 如果选择了 Other，展开输入框
                    if (selectedReason == "Other") {
                        Spacer(modifier = Modifier.height(4.dp))
                        OutlinedTextField(
                            value = customReason,
                            onValueChange = { customReason = it },
                            placeholder = { Text("Enter custom reason...", fontSize = 13.sp) },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(10.dp),
                            singleLine = true
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // 提示信息
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    Icons.Default.Info,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                    modifier = Modifier.size(14.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "Moved items will be deducted from active inventory.",
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f)
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // 提交确认按钮
            Button(
                onClick = {
                    val finalReason = if (selectedReason == "Other") {
                        customReason.ifBlank { "Wasted" }
                    } else {
                        selectedReason
                    }

                    val itemsWithQty = selectedItems.map { item ->
                        Pair(item, itemQuantities[item.id] ?: item.quantity)
                    }

                    viewModel.processCustomTransfer(
                        itemsWithQty = itemsWithQty,
                        status = targetStatus,
                        reason = if (isConsumed) "Consumed" else finalReason,
                        onSuccess = onSuccess
                    )
                },
                colors = ButtonDefaults.buttonColors(containerColor = primaryThemeColor),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
            ) {
                Text(
                    text = "Confirm & Save (${selectedItems.size} items)",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }

            TextButton(
                onClick = onBackClick,
                modifier = Modifier.padding(top = 2.dp)
            ) {
                Text(
                    text = "Cancel",
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 13.sp
                )
            }
        }
    }
}