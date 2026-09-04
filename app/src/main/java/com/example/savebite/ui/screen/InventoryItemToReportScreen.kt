package com.example.savebite.ui.screen

import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.savebite.R
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

    val expiredLabel = stringResource(R.string.waste_reason_expired)
    val otherReasonLabel = stringResource(R.string.waste_reason_other)
    val quickReasons = listOf(
        expiredLabel,
        stringResource(R.string.waste_reason_spoiled),
        stringResource(R.string.waste_reason_leftover),
        stringResource(R.string.waste_reason_damaged),
        otherReasonLabel
    )
    var selectedReason by remember(expiredLabel) { mutableStateOf(expiredLabel) }
    var customReason by remember { mutableStateOf("") }

    // Quantity map
    val itemQuantities = remember(selectedItems) {
        mutableStateMapOf<String, Int>().apply {
            selectedItems.forEach { this[it.id] = it.quantity }
        }
    }

    val primaryThemeColor = if (isConsumed) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error
    val bgHeaderColor = if (isConsumed) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.errorContainer

    Scaffold(
        topBar = {
            AppTopBar(
                title = if (isConsumed) stringResource(R.string.report_consumed_title) else stringResource(R.string.report_wasted_title),
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
            Surface(
                color = bgHeaderColor,
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        painter = painterResource(
                            id = if (isConsumed) R.drawable.consumed else R.drawable.delete
                        ),
                        contentDescription = if (isConsumed) "Consumed" else "Wasted",
                        modifier = Modifier.size(32.dp),
                        tint = primaryThemeColor
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = if (isConsumed) stringResource(R.string.report_confirm_consumed) else stringResource(R.string.report_confirm_wasted),
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = primaryThemeColor
                        )
                        Text(
                            text = stringResource(R.string.report_adjust_qty_hint),
                            fontSize = 12.sp,
                            color = primaryThemeColor.copy(alpha = 0.8f)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

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
                                    text = stringResource(R.string.report_in_stock_hint, item.quantity, item.unit),
                                    fontSize = 12.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }

                            // Quantity adjustment stepper
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
                                            painter = painterResource(R.drawable.remove),
                                            contentDescription = stringResource(R.string.content_desc_decrease),
                                            modifier = Modifier.size(20.dp),
                                            tint = MaterialTheme.colorScheme.onSurfaceVariant
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
                                            painter = painterResource(R.drawable.add),
                                            contentDescription = stringResource(R.string.content_desc_increase),
                                            modifier = Modifier.size(20.dp),
                                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // Waste reason options for WASTED status
            if (!isConsumed) {
                Spacer(modifier = Modifier.height(8.dp))
                Column(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = stringResource(R.string.inventory_waste_reason_label),
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(6.dp))

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

                    if (selectedReason == otherReasonLabel) {
                        Spacer(modifier = Modifier.height(4.dp))
                        OutlinedTextField(
                            value = customReason,
                            onValueChange = { customReason = it },
                            placeholder = { Text(stringResource(R.string.inventory_waste_reason_placeholder), fontSize = 13.sp) },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(10.dp),
                            singleLine = true
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    painter = painterResource(R.drawable.info),
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                    modifier = Modifier.size(14.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = stringResource(R.string.report_deduction_hint),
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f)
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            Button(
                onClick = {
                    val finalReason = if (selectedReason == otherReasonLabel) {
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
                    text = stringResource(R.string.report_action_confirm_save, selectedItems.size),
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
                    text = stringResource(R.string.action_cancel),
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 13.sp
                )
            }
        }
    }
}