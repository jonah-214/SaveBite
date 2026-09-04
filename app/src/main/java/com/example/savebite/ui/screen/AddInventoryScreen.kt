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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.savebite.R
import com.example.savebite.model.DefaultStorages
import com.example.savebite.model.Inventory
import com.example.savebite.model.ShoppingItem
import com.example.savebite.ui.navigation.AppTopBar
import com.example.savebite.ui.viewmodel.InventoryViewModel
import com.example.savebite.utils.Currency
import com.example.savebite.utils.DateFormats
import com.example.savebite.utils.InventoryFormLogic
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.TimeZone

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

// Screen for adding a new item or editing an existing one in the inventory.
// Supports batch adding from a list of shopping items
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddInventoryScreen(
    itemId: String? = null,
    viewModel: InventoryViewModel? = null,
    batchItems: List<ShoppingItem>? = null,
    storageLocations: List<String> = DefaultStorages.ALL,
    onBackClick: () -> Unit = {},
    onNavigateToShoppingList: () -> Unit = {},
    onSaveClick: (List<Inventory>) -> Unit = {}
) {
    val isBatchMode = !batchItems.isNullOrEmpty()
    var currentIndex by remember { mutableIntStateOf(0) }
    val accumulatedBatchList = remember { mutableStateListOf<Inventory>() }
    val scrollState = rememberScrollState()
    
    LaunchedEffect(currentIndex) {
        scrollState.animateScrollTo(0)
    }

    // Form Input States
    val dairyLabel = stringResource(R.string.category_dairy)
    var name by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var category by remember(dairyLabel) { mutableStateOf(dairyLabel) }
    var storage by remember(storageLocations) {
        mutableStateOf(storageLocations.firstOrNull() ?: DefaultStorages.FALLBACK)
    }
    var quantity by remember { mutableIntStateOf(1) }
    var unit by remember { mutableStateOf("pcs") }
    var price by remember { mutableStateOf("") }
    var purchaseDate by remember { mutableStateOf(getTodayFormatted()) }
    var expiryDate by remember { mutableStateOf(getFutureDateFormatted(7)) }
    var notes by remember { mutableStateOf("") }
    var isConsumed by remember { mutableStateOf(false) }

    fun currentFormDraft() = InventoryFormLogic.FormDraft(
        name = name,
        description = description,
        category = category,
        storage = storage,
        quantity = quantity,
        unit = unit,
        priceInput = price,
        purchaseDate = purchaseDate,
        expiryDate = expiryDate,
        notes = notes,
        isConsumed = isConsumed
    )

    val handlePreviousStep = {
        if (isBatchMode && currentIndex > 0) {
            val currentDraft = InventoryFormLogic.buildDraft(currentFormDraft(), itemId)

            if (currentIndex < accumulatedBatchList.size) {
                accumulatedBatchList[currentIndex] = currentDraft
            } else {
                accumulatedBatchList.add(currentDraft)
            }

            currentIndex--
        } else {
            onBackClick()
        }
    }
    
    val isPriceValid = remember(price) { InventoryFormLogic.isPriceValid(price) }
    var attemptedSave by remember { mutableStateOf(false) }

    // Batch initialization
    LaunchedEffect(currentIndex, batchItems) {
        if (isBatchMode && batchItems != null && currentIndex < batchItems.size) {
            if (currentIndex < accumulatedBatchList.size) {
                val savedItem = accumulatedBatchList[currentIndex]
                name = savedItem.name
                description = savedItem.description
                category = savedItem.category
                storage = savedItem.storage
                quantity = savedItem.quantity
                unit = savedItem.unit
                price = InventoryFormLogic.formatPriceString(savedItem.price)
                purchaseDate = savedItem.purchaseDate
                expiryDate = savedItem.expiry
                notes = savedItem.notes
                isConsumed = savedItem.isConsumed
            } else {
                val item = batchItems[currentIndex]
                name = item.name
                quantity = item.quantity
                unit = item.unit
                category = item.category
                price = ""
                description = ""
                notes = ""
                isConsumed = false
            }
            attemptedSave = false
        }
    }

    // Edit initialization
    if (!isBatchMode && itemId != null && viewModel != null) {
        val existingItem by viewModel.getItemById(itemId).collectAsState(initial = null)
        LaunchedEffect(existingItem) {
            existingItem?.let { item ->
                name = item.name
                description = item.description
                price = InventoryFormLogic.formatPriceString(item.price)
                category = item.category
                storage = item.storage
                quantity = item.quantity
                unit = item.unit
                purchaseDate = item.purchaseDate
                expiryDate = item.expiry
                notes = item.notes
                isConsumed = item.isConsumed
            }
        }
    }

    var categoryExpanded by remember { mutableStateOf(false) }
    var storageExpanded by remember { mutableStateOf(false) }
    var unitExpanded by remember { mutableStateOf(false) }
    var showPurchasePicker by remember { mutableStateOf(false) }
    var showExpiryPicker by remember { mutableStateOf(false) }

    val categoryOptions = listOf(
        stringResource(R.string.category_dairy),
        stringResource(R.string.category_produce),
        stringResource(R.string.category_meat),
        stringResource(R.string.category_bakery),
        stringResource(R.string.category_beverages),
        stringResource(R.string.category_pantry),
        stringResource(R.string.category_frozen),
        stringResource(R.string.category_snacks),
        stringResource(R.string.category_condiments),
        stringResource(R.string.category_canned),
        stringResource(R.string.category_prepared),
        stringResource(R.string.category_spices)
    )

    val unitOptions = listOf("pcs", "pack", "box", "bottle", "can", "kg", "g", "L", "ml", "oz", "lb")

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            AppTopBar(
                title = when {
                    isBatchMode && batchItems != null -> stringResource(R.string.inventory_batch_add_title, currentIndex + 1, batchItems.size)
                    itemId != null -> stringResource(R.string.inventory_edit_title)
                    else -> stringResource(R.string.inventory_add_title)
                },
                showBackButton = true,
                onBackClick = {
                    if (isBatchMode) {
                        onNavigateToShoppingList()
                    } else {
                        onBackClick()
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
        ) {
            if (isBatchMode && batchItems != null) {
                StepProgressBar(
                    totalSteps = batchItems.size,
                    currentStep = currentIndex,
                    stepTitles = batchItems.map { it.name }
                )
                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
            }

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(scrollState)
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { 
                        Row {
                            Text(stringResource(R.string.inventory_name_label))
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
                        text = stringResource(R.string.inventory_name_required),
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.padding(start = 8.dp, top = 2.dp)
                    )
                }

                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text(stringResource(R.string.inventory_description_label)) },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp)
                )

                OutlinedTextField(
                    value = price,
                    onValueChange = { price = it },
                    label = { 
                        Row {
                            Text(stringResource(R.string.inventory_price_label, Currency.PREFIX))
                            Text(" *", color = MaterialTheme.colorScheme.error)
                        }
                    },
                    placeholder = { Text(stringResource(R.string.inventory_price_placeholder)) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    isError = attemptedSave && !isPriceValid
                )
                if (attemptedSave && !isPriceValid) {
                    Text(
                        text = if (price.isBlank()) stringResource(R.string.inventory_price_required) else stringResource(R.string.inventory_price_invalid),
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.padding(start = 8.dp, top = 2.dp)
                    )
                }

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
                            label = { Text(stringResource(R.string.inventory_category_label)) },
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
                            label = { Text(stringResource(R.string.inventory_storage_label)) },
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
                        Text(stringResource(R.string.inventory_quantity_label), fontSize = 16.sp, fontWeight = FontWeight.Medium)

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

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Box(modifier = Modifier.weight(1f)) {
                        OutlinedTextField(
                            value = DateFormats.toDisplayString(purchaseDate),
                            onValueChange = {},
                            readOnly = true,
                            label = { Text(stringResource(R.string.inventory_purchase_date_label)) },
                            trailingIcon = {
                                Icon(
                                    painter = painterResource(R.drawable.calender),
                                    contentDescription = stringResource(R.string.inventory_purchase_date_label),
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
                            value = DateFormats.toDisplayString(expiryDate),
                            onValueChange = {},
                            readOnly = true,
                            label = { Text(stringResource(R.string.inventory_expiry_date_label)) },
                            trailingIcon = {
                                Icon(
                                    painter = painterResource(R.drawable.calender_clock),
                                    contentDescription = stringResource(R.string.inventory_expiry_date_label),
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

                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = { Text(stringResource(R.string.inventory_notes_label)) },
                    placeholder = { Text(stringResource(R.string.inventory_notes_placeholder)) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(110.dp),
                    maxLines = 3,
                    shape = RoundedCornerShape(12.dp)
                )

                Spacer(modifier = Modifier.height(12.dp))

                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    val actionButtonText = when {
                        isBatchMode && batchItems != null && currentIndex < batchItems.size - 1 -> stringResource(R.string.inventory_action_next_item, currentIndex + 2, batchItems.size)
                        isBatchMode -> stringResource(R.string.inventory_action_finish_save)
                        else -> stringResource(R.string.inventory_action_save_item)
                    }

                    Button(
                        onClick = {
                            attemptedSave = true
                            val newFood = InventoryFormLogic.buildInventoryOrNull(currentFormDraft(), itemId)
                            if (newFood != null) {
                                if (isBatchMode && batchItems != null) {
                                    if (currentIndex < accumulatedBatchList.size) {
                                        accumulatedBatchList[currentIndex] = newFood
                                    } else {
                                        accumulatedBatchList.add(newFood)
                                    }

                                    if (currentIndex < batchItems.size - 1) {
                                        currentIndex++
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

                    OutlinedButton(
                        onClick = { handlePreviousStep() },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp),
                        shape = RoundedCornerShape(12.dp),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
                    ) {
                        val backButtonText = if (isBatchMode && currentIndex > 0) {
                            stringResource(R.string.inventory_action_back_previous)
                        } else {
                            stringResource(R.string.inventory_action_cancel_back)
                        }

                        Text(
                            text = backButtonText,
                            fontSize = 16.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }
        }
    }

    if (showPurchasePicker) {
        val datePickerState = rememberDatePickerState(selectableDates = PastDateSelectableDates)
        DatePickerDialog(
            onDismissRequest = { showPurchasePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    datePickerState.selectedDateMillis?.let { millis -> purchaseDate = formatDate(millis) }
                    showPurchasePicker = false
                }) { Text(stringResource(R.string.action_ok), color = MaterialTheme.colorScheme.primary) }
            },
            dismissButton = {
                TextButton(onClick = { showPurchasePicker = false }) { Text(stringResource(R.string.action_cancel), color = MaterialTheme.colorScheme.outline) }
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
                }) { Text(stringResource(R.string.action_ok), color = MaterialTheme.colorScheme.primary) }
            },
            dismissButton = {
                TextButton(onClick = { showExpiryPicker = false }) { Text(stringResource(R.string.action_cancel), color = MaterialTheme.colorScheme.outline) }
            }
        ) { DatePicker(state = datePickerState) }
    }
}

/**
 * Visual indicator showing progress through a multi-step batch add process.
 */
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
                    text = stepTitles.getOrNull(i) ?: stringResource(R.string.inventory_step_item_title, i + 1),
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

private fun formatDate(millis: Long): String {
    val formatter = SimpleDateFormat("yyyy-MM-dd", Locale.US)
    formatter.timeZone = TimeZone.getTimeZone("UTC")
    return formatter.format(Date(millis))
}

private fun getTodayFormatted(): String = DateFormats.toStorageString(Date())

private fun getFutureDateFormatted(daysToAdd: Int): String {
    val calendar = Calendar.getInstance()
    calendar.add(Calendar.DAY_OF_YEAR, daysToAdd)
    return DateFormats.toStorageString(calendar.time)
}