package com.example.savebite.ui.screen

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.savebite.R
import com.example.savebite.model.Inventory
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
        // Calculate the start of today in UTC millis
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
    storageLocations: List<String> = listOf("Pantry", "Refrigerator", "Freezer"),
    onBackClick: () -> Unit = {},
    onSaveClick: (Inventory) -> Unit = {}
) {
    // Form Input States
    var name by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var category by remember { mutableStateOf("Dairy") }
    var storage by remember(storageLocations) {
        mutableStateOf(storageLocations.firstOrNull() ?: "Refrigerator")
    }
    var quantity by remember { mutableIntStateOf(1) }
    var unit by remember { mutableStateOf("pcs") }

    // Default Dates (Today & 5 Days Later)
    var purchaseDate by remember { mutableStateOf(getTodayFormatted()) }
    var expiryDate by remember { mutableStateOf(getFutureDateFormatted(5)) }
    var notes by remember { mutableStateOf("") }

    // If editing, load data
    if (itemId != null && viewModel != null) {
        val existingItem by viewModel.getItemById(itemId).collectAsState(initial = null)
        LaunchedEffect(existingItem) {
            existingItem?.let { item ->
                name = item.name
                description = item.description
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
                title = if (itemId != null) "Edit Inventory" else "Add Inventory",
                showBackButton = true,
                onBackClick = onBackClick
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // 1. Food Name & Description
            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text("Item Name (e.g. Milk)") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                shape = RoundedCornerShape(12.dp)
            )

            OutlinedTextField(
                value = description,
                onValueChange = { description = it },
                label = { Text("Subtitle / Description (e.g. Fresh Milk)") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                shape = RoundedCornerShape(12.dp)
            )

            // 2. Category & Storage Dropdowns
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Category Dropdown
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
                        modifier = Modifier
                            .menuAnchor()
                            .fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = MaterialTheme.colorScheme.primary,
                            unfocusedBorderColor = MaterialTheme.colorScheme.outline
                        )
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

                // Storage Location Dropdown
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
                        modifier = Modifier
                            .menuAnchor()
                            .fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = MaterialTheme.colorScheme.primary,
                            unfocusedBorderColor = MaterialTheme.colorScheme.outline
                        )
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
                    Text(
                        text = "Quantity",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Medium
                    )

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        // Decrement Button
                        OutlinedButton(
                            onClick = { if (quantity > 1) quantity-- },
                            contentPadding = PaddingValues(0.dp),
                            modifier = Modifier.size(36.dp),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text("-", fontSize = 20.sp)
                        }

                        // Quantity Display
                        Text(
                            text = "$quantity",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 4.dp)
                        )

                        // Increment Button
                        OutlinedButton(
                            onClick = { quantity++ },
                            contentPadding = PaddingValues(0.dp),
                            modifier = Modifier.size(36.dp),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text("+", fontSize = 18.sp)
                        }

                        // Unit Options Dropdown
                        ExposedDropdownMenuBox(
                            expanded = unitExpanded,
                            onExpandedChange = { unitExpanded = !unitExpanded },
                            modifier = Modifier.width(110.dp)
                        ) {
                            OutlinedTextField(
                                value = unit,
                                onValueChange = {},
                                readOnly = true,
                                modifier = Modifier.menuAnchor(),
                                shape = RoundedCornerShape(8.dp),
                                singleLine = true,
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                                    unfocusedBorderColor = MaterialTheme.colorScheme.outline,
                                    unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                                    focusedContainerColor = MaterialTheme.colorScheme.surface
                                )
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

            // 4. Dates Section with Calendar Dialogs
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Purchase Date Box
                Box(modifier = Modifier.weight(1f)) {
                    OutlinedTextField(
                        value = purchaseDate,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Purchase Date") },
                        trailingIcon = {
                            Icon(
                                painter = painterResource(R.drawable.calendar),
                                contentDescription = "Purchase Date",
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(20.dp)
                            )
                        },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        enabled = false,
                        colors = OutlinedTextFieldDefaults.colors(
                            disabledTextColor = MaterialTheme.colorScheme.onSurface,
                            disabledBorderColor = MaterialTheme.colorScheme.outline,
                            disabledLabelColor = MaterialTheme.colorScheme.onSurfaceVariant,
                            disabledTrailingIconColor = MaterialTheme.colorScheme.primary
                        )
                    )
                    Box(
                        modifier = Modifier
                            .matchParentSize()
                            .clickable { showPurchasePicker = true }
                    )
                }

                // Expiry Date Box
                Box(modifier = Modifier.weight(1f)) {
                    OutlinedTextField(
                        value = expiryDate,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Expiry Date") },
                        trailingIcon = {
                            Icon(
                                painter = painterResource(R.drawable.calendar_clock),
                                contentDescription = "Expiry Date",
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(20.dp)
                            )
                        },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        enabled = false,
                        colors = OutlinedTextFieldDefaults.colors(
                            disabledTextColor = MaterialTheme.colorScheme.onSurface,
                            disabledBorderColor = MaterialTheme.colorScheme.outline,
                            disabledLabelColor = MaterialTheme.colorScheme.onSurfaceVariant,
                            disabledTrailingIconColor = MaterialTheme.colorScheme.primary
                        )
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

            // 6. Save Action Button
            Button(
                onClick = {
                    if (name.isNotBlank()) {
                        val calculatedDaysLeft = calculateDaysLeft(expiryDate)

                        val newFood = Inventory(
                            id = itemId ?: UUID.randomUUID().toString(),
                            name = name,
                            description = description,
                            category = category,
                            storage = storage,
                            quantity = quantity,
                            unit = unit,
                            daysLeft = calculatedDaysLeft,
                            purchaseDate = purchaseDate,
                            expiry = expiryDate,
                            notes = notes
                        )
                        onSaveClick(newFood)
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                shape = RoundedCornerShape(12.dp),
                enabled = name.isNotBlank()
            ) {
                Text(
                    text = "Save Item",
                    fontSize = 16.sp,
                    color = Color.Black,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }

    // --- PURCHASE DATE PICKER DIALOG ---
    if (showPurchasePicker) {
        val datePickerState = rememberDatePickerState(
            selectableDates = PastDateSelectableDates
        )
        DatePickerDialog(
            onDismissRequest = { showPurchasePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    datePickerState.selectedDateMillis?.let { millis ->
                        purchaseDate = formatDate(millis)
                    }
                    showPurchasePicker = false
                }) {
                    Text("OK", color = MaterialTheme.colorScheme.primary)
                }
            },
            dismissButton = {
                TextButton(onClick = { showPurchasePicker = false }) {
                    Text("Cancel", color = MaterialTheme.colorScheme.outline)
                }
            }
        ) {
            DatePicker(
                state = datePickerState,
                colors = DatePickerDefaults.colors(
                    selectedDayContainerColor = MaterialTheme.colorScheme.primary,
                    todayDateBorderColor = MaterialTheme.colorScheme.primary,
                    todayContentColor = MaterialTheme.colorScheme.primary
                )
            )
        }
    }

    // --- EXPIRY DATE PICKER DIALOG ---
    if (showExpiryPicker) {
        val datePickerState = rememberDatePickerState(
            selectableDates = PastDateSelectableDates
        )
        DatePickerDialog(
            onDismissRequest = { showExpiryPicker = false },
            confirmButton = {
                TextButton(onClick = {
                    datePickerState.selectedDateMillis?.let { millis ->
                        expiryDate = formatDate(millis)
                    }
                    showExpiryPicker = false
                }) {
                    Text("OK", color = MaterialTheme.colorScheme.primary)
                }
            },
            dismissButton = {
                TextButton(onClick = { showExpiryPicker = false }) {
                    Text("Cancel", color = MaterialTheme.colorScheme.outline)
                }
            }
        ) {
            DatePicker(
                state = datePickerState,
                colors = DatePickerDefaults.colors(
                    selectedDayContainerColor = MaterialTheme.colorScheme.primary,
                    todayDateBorderColor = MaterialTheme.colorScheme.primary,
                    todayContentColor = MaterialTheme.colorScheme.primary
                )
            )
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