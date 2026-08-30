package com.example.savebite.ui.screen

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.savebite.R
import com.example.savebite.model.Inventory
import com.example.savebite.model.ShoppingItem
import com.example.savebite.ui.navigation.AppTopBar
import com.example.savebite.ui.viewmodel.InventoryViewModel
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.TimeZone
import java.util.UUID
import java.util.concurrent.TimeUnit

// Custom SelectableDates implementation to disable past dates
@OptIn(ExperimentalMaterial3Api::class)
object PastDateSelectableDates : SelectableDates {
    override fun isSelectableDate(utcTimeMillis: Long): Boolean {
        val todayUtcMillis = Calendar.getInstance(TimeZone.getTimeZone("UTC")).apply {
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }.timeInMillis

        return utcTimeMillis >= todayUtcMillis
    }

    override fun isSelectableYear(year: Int): Boolean {
        val currentYear = Calendar.getInstance().get(Calendar.YEAR)
        return year >= currentYear
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddInventoryScreen(
    itemId: String? = null,
    viewModel: InventoryViewModel? = null,
    batchItems: List<ShoppingItem>? = null,
    storageLocations: List<String> = listOf("Refrigerator", "Pantry", "Freezer"),
    onBackClick: () -> Unit = {},
    onSaveClick: (List<Inventory>) -> Unit = {} // 修正：支持回调 List 应对批量新增
) {
    val isBatchMode = !batchItems.isNullOrEmpty()
    var currentIndex by remember { mutableIntStateOf(0) }
    val accumulatedBatchList = remember { mutableStateListOf<Inventory>() }

    val collectedInventoryItems = remember { mutableStateListOf<Inventory>() }

    // Form Input States
    var name by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var category by remember { mutableStateOf("Dairy & Eggs") }
    var storage by remember(storageLocations) {
        mutableStateOf(storageLocations.firstOrNull() ?: "Refrigerator")
    }
    var quantity by remember { mutableIntStateOf(1) }
    var unit by remember { mutableStateOf("pcs") }
    var price by remember { mutableStateOf("") }
    val isPriceValid = price.isNotBlank() && price.toDoubleOrNull() != null && price.toDouble() >= 0.0
    var attemptedSave by remember { mutableStateOf(false) }

    // Default Dates (Today & 7 Days Later)
    var purchaseDate by remember { mutableStateOf(getTodayFormatted()) }
    var expiryDate by remember { mutableStateOf(getFutureDateFormatted(7)) }
    var notes by remember { mutableStateOf("") }

    // 1. 如果是 Batch 模式，根据 currentIndex 自动更新数据
    LaunchedEffect(currentIndex, batchItems) {
        if (isBatchMode && batchItems != null && currentIndex < batchItems.size) {
            val item = batchItems[currentIndex]
            name = item.name
            quantity = item.quantity
            unit = item.unit
            category = item.category
            price = ""
            description = ""
            notes = ""
            attemptedSave = false
        }
    }

    // 2. 如果是 Edit 模式，读取已有数据
    if (!isBatchMode && itemId != null && viewModel != null) {
        val existingItem by viewModel.getItemById(itemId).collectAsState(initial = null)
        LaunchedEffect(existingItem) {
            existingItem?.let { item ->
                name = item.name
                description = item.description
                price = if (item.price > 0) item.price.toString() else ""
                category = item.category
                storage = item.storage
                quantity = item.quantity
                unit = item.unit
                purchaseDate = item.purchaseDate
                expiryDate = item.expiry
                notes = item.notes
            }
        }
    }

    // Dialog & Dropdown States
    var categoryExpanded by remember { mutableStateOf(false) }
    var storageExpanded by remember { mutableStateOf(false) }
    var unitExpanded by remember { mutableStateOf(false) }
    var showPurchasePicker by remember { mutableStateOf(false) }
    var showExpiryPicker by remember { mutableStateOf(false) }

    val categoryOptions = listOf(
        "Dairy & Eggs",
        "Produce",
        "Meat & Seafood",
        "Bakery & Bread",
        "Beverages",
        "Pantry & Dry Goods",
        "Frozen Foods",
        "Snacks & Sweets",
        "Condiments & Sauces",
        "Canned Goods",
        "Leftovers & Prepared",
        "Spices & Baking"
    )

    val unitOptions = listOf(
        "pcs",
        "pack",
        "box",
        "bottle",
        "can",
        "kg",
        "g",
        "L",
        "ml",
        "oz",
        "lb"
    )

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            AppTopBar(
                title = when {
                    isBatchMode && batchItems != null -> "Add to Inventory (${currentIndex + 1}/${batchItems.size})"
                    itemId != null -> "Edit Inventory"
                    else -> "Add Inventory"
                },
                showBackButton = true,
                onBackClick = onBackClick
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
        ) {
            // 修正：如果为 Batch 模式，顶部加上 Step 进度条
            if (isBatchMode && batchItems != null) {
                StepProgressBar(
                    totalSteps = batchItems.size,
                    currentStep = currentIndex,
                    stepTitles = batchItems.map { it.name }
                )
                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
            }

            // 修正：单一的可滚动容器，防止 Scroll 嵌套 Crash
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // 1. Food Name & Description
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { 
                        Row {
                            Text("Item Name (e.g. Milk)")
                            Text(" *", color = MaterialTheme.colorScheme.error)
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    isError = attemptedSave && name.isBlank()
                )
                if (attemptedSave && name.isBlank()) {
                    Text(
                        text = "Item name is required",
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.padding(start = 8.dp, top = 2.dp)
                    )
                }

                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Subtitle / Description (e.g. Fresh Milk)") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp)
                )

                OutlinedTextField(
                    value = price,
                    onValueChange = { price = it },
                    label = { 
                        Row {
                            Text("Price / Cost (RM)")
                            Text(" *", color = MaterialTheme.colorScheme.error)
                        }
                    },
                    placeholder = { Text("e.g. 12.50") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    isError = attemptedSave && !isPriceValid
                )
                if (attemptedSave && !isPriceValid) {
                    Text(
                        text = if (price.isBlank()) "Price is required" else "Invalid price format",
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.padding(start = 8.dp, top = 2.dp)
                    )
                }

                // 2. Category & Storage Dropdowns
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    ExposedDropdownMenuBox(
                        expanded = categoryExpanded,
                        onExpandedChange = { categoryExpanded = !categoryExpanded },
                        modifier = Modifier.weight(1f)
                    ) {
                        OutlinedTextField(
                            value = category,
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Category") },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = categoryExpanded) },
                            modifier = Modifier.menuAnchor().fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp)
                        )
                        ExposedDropdownMenu(
                            expanded = categoryExpanded,
                            onDismissRequest = { categoryExpanded = false },
                            modifier = Modifier.background(MaterialTheme.colorScheme.surface)
                        ) {
                            categoryOptions.forEach { item ->
                                DropdownMenuItem(
                                    text = { Text(item, color = MaterialTheme.colorScheme.onSurface) },
                                    onClick = {
                                        category = item
                                        categoryExpanded = false
                                    }
                                )
                            }
                        }
                    }

                    ExposedDropdownMenuBox(
                        expanded = storageExpanded,
                        onExpandedChange = { storageExpanded = !storageExpanded },
                        modifier = Modifier.weight(1f)
                    ) {
                        OutlinedTextField(
                            value = storage,
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Storage") },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = storageExpanded) },
                            modifier = Modifier.menuAnchor().fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp)
                        )
                        ExposedDropdownMenu(
                            expanded = storageExpanded,
                            onDismissRequest = { storageExpanded = false },
                            modifier = Modifier.background(MaterialTheme.colorScheme.surface)
                        ) {
                            storageLocations.forEach { loc ->
                                DropdownMenuItem(
                                    text = { Text(loc, color = MaterialTheme.colorScheme.onSurface) },
                                    onClick = {
                                        storage = loc
                                        storageExpanded = false
                                    }
                                )
                            }
                        }
                    }
                }

                // 3. Quantity Selector
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Quantity", fontSize = 16.sp, fontWeight = FontWeight.Medium)

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            OutlinedButton(
                                onClick = { if (quantity > 1) quantity-- },
                                contentPadding = PaddingValues(0.dp),
                                modifier = Modifier.size(36.dp),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Text("-", fontSize = 20.sp)
                            }

                            Text(
                                text = "$quantity",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 4.dp)
                            )

                            OutlinedButton(
                                onClick = { quantity++ },
                                contentPadding = PaddingValues(0.dp),
                                modifier = Modifier.size(36.dp),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Text("+", fontSize = 18.sp)
                            }

                            ExposedDropdownMenuBox(
                                expanded = unitExpanded,
                                onExpandedChange = { unitExpanded = !unitExpanded },
                                modifier = Modifier.width(110.dp)
                            ) {
                                OutlinedTextField(
                                    value = unit,
                                    onValueChange = {},
                                    readOnly = true,
                                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = unitExpanded) },
                                    modifier = Modifier.menuAnchor(),
                                    shape = RoundedCornerShape(8.dp),
                                    singleLine = true
                                )
                                ExposedDropdownMenu(
                                    expanded = unitExpanded,
                                    onDismissRequest = { unitExpanded = false },
                                    modifier = Modifier.background(MaterialTheme.colorScheme.surface)
                                ) {
                                    unitOptions.forEach { option ->
                                        DropdownMenuItem(
                                            text = { Text(option, color = MaterialTheme.colorScheme.onSurface) },
                                            onClick = {
                                                unit = option
                                                unitExpanded = false
                                            }
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                // 4. Dates Section
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Box(modifier = Modifier.weight(1f)) {
                        OutlinedTextField(
                            value = purchaseDate,
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Purchase Date") },
                            trailingIcon = {
                                Icon(
                                    painter = painterResource(R.drawable.calender),
                                    contentDescription = "Purchase Date",
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(20.dp)
                                )
                            },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            enabled = false
                        )
                        Box(
                            modifier = Modifier
                                .matchParentSize()
                                .clickable { showPurchasePicker = true }
                        )
                    }

                    Box(modifier = Modifier.weight(1f)) {
                        OutlinedTextField(
                            value = expiryDate,
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Expiry Date") },
                            trailingIcon = {
                                Icon(
                                    painter = painterResource(R.drawable.calender_clock),
                                    contentDescription = "Expiry Date",
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(20.dp)
                                )
                            },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            enabled = false
                        )
                        Box(
                            modifier = Modifier
                                .matchParentSize()
                                .clickable { showExpiryPicker = true }
                        )
                    }
                }

                // 5. Notes Field
                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = { Text("Notes") },
                    placeholder = { Text("e.g. Keep chilled and shake well before use.") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(110.dp),
                    maxLines = 3,
                    shape = RoundedCornerShape(12.dp)
                )

                Spacer(modifier = Modifier.height(12.dp))

                // 修正：动态按钮文字与保存/下一步逻辑
                val actionButtonText = when {
                    isBatchMode && batchItems != null && currentIndex < batchItems.size - 1 -> "Next Item (${currentIndex + 2}/${batchItems.size})"
                    isBatchMode -> "Finish & Save All"
                    else -> "Save Item"
                }

                Button(
                    onClick = {
                        attemptedSave = true
                        if (name.isNotBlank() && isPriceValid) {
                            val parsedPrice = price.toDoubleOrNull() ?: 0.0
                            val calculatedDaysLeft = calculateDaysLeft(expiryDate)
                            
                            val newFood = Inventory(
                                id = itemId ?: UUID.randomUUID().toString(),
                                name = name,
                                description = description,
                                category = category,
                                storage = storage,
                                quantity = quantity,
                                unit = unit,
                                price = parsedPrice,
                                daysLeft = calculatedDaysLeft,
                                purchaseDate = purchaseDate,
                                expiry = expiryDate,
                                notes = notes
                            )

                            if (isBatchMode && batchItems != null) {
                                accumulatedBatchList.add(newFood)
                                if (currentIndex < batchItems.size - 1) {
                                    currentIndex++
                                    // Reset attemptedSave for the next item in batch mode
                                    attemptedSave = false
                                } else {
                                    onSaveClick(accumulatedBatchList.toList())
                                }
                            } else {
                                onSaveClick(listOf(newFood))
                            }
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(
                        text = actionButtonText,
                        fontSize = 16.sp,
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }

    // DatePicker Dialogs ...
    if (showPurchasePicker) {
        val datePickerState = rememberDatePickerState(selectableDates = PastDateSelectableDates)
        DatePickerDialog(
            onDismissRequest = { showPurchasePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    datePickerState.selectedDateMillis?.let { millis -> purchaseDate = formatDate(millis) }
                    showPurchasePicker = false
                }) { Text("OK", color = MaterialTheme.colorScheme.primary) }
            },
            dismissButton = {
                TextButton(onClick = { showPurchasePicker = false }) { Text("Cancel", color = MaterialTheme.colorScheme.outline) }
            }
        ) { DatePicker(state = datePickerState) }
    }

    if (showExpiryPicker) {
        val datePickerState = rememberDatePickerState(selectableDates = PastDateSelectableDates)
        DatePickerDialog(
            onDismissRequest = { showExpiryPicker = false },
            confirmButton = {
                TextButton(onClick = {
                    datePickerState.selectedDateMillis?.let { millis -> expiryDate = formatDate(millis) }
                    showExpiryPicker = false
                }) { Text("OK", color = MaterialTheme.colorScheme.primary) }
            },
            dismissButton = {
                TextButton(onClick = { showExpiryPicker = false }) { Text("Cancel", color = MaterialTheme.colorScheme.outline) }
            }
        ) { DatePicker(state = datePickerState) }
    }
}

// 步骤进度条组件
@Composable
fun StepProgressBar(
    totalSteps: Int,
    currentStep: Int,
    stepTitles: List<String>,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp, horizontal = 16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        for (i in 0 until totalSteps) {
            val isPassed = i < currentStep
            val isCurrent = i == currentStep

            val activeColor = MaterialTheme.colorScheme.primary
            val inactiveColor = MaterialTheme.colorScheme.outlineVariant
            val circleColor = if (isPassed || isCurrent) activeColor else inactiveColor
            val textColor = if (isPassed || isCurrent) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant

            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.weight(1f)
            ) {
                Box(
                    modifier = Modifier
                        .size(28.dp)
                        .clip(CircleShape)
                        .background(circleColor),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "${i + 1}",
                        color = textColor,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = stepTitles.getOrNull(i) ?: "Item ${i + 1}",
                    fontSize = 10.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    color = if (isCurrent) activeColor else MaterialTheme.colorScheme.onSurfaceVariant,
                    fontWeight = if (isCurrent) FontWeight.Bold else FontWeight.Normal
                )
            }

            if (i < totalSteps - 1) {
                val lineColor = if (i < currentStep) activeColor else inactiveColor
                HorizontalDivider(
                    color = lineColor,
                    thickness = 2.dp,
                    modifier = Modifier
                        .weight(0.8f)
                        .padding(bottom = 16.dp)
                )
            }
        }
    }
}

// Helper Functions
private fun formatDate(millis: Long): String {
    val formatter = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
    formatter.timeZone = TimeZone.getTimeZone("UTC")
    return formatter.format(Date(millis))
}

private fun getTodayFormatted(): String {
    val formatter = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
    return formatter.format(Date())
}

private fun getFutureDateFormatted(daysToAdd: Int): String {
    val calendar = Calendar.getInstance()
    calendar.add(Calendar.DAY_OF_YEAR, daysToAdd)
    val formatter = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
    return formatter.format(calendar.time)
}

private fun calculateDaysLeft(expiryDateStr: String): Int {
    return try {
        val formatter = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
        val expiryDate = formatter.parse(expiryDateStr) ?: return 0
        val today = Date()
        val diffInMillis = expiryDate.time - today.time
        val days = TimeUnit.DAYS.convert(diffInMillis, TimeUnit.MILLISECONDS).toInt()
        if (days < 0) 0 else days + 1
    } catch (e: Exception) {
        0
    }
}