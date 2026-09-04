package com.example.savebite.ui.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.savebite.R
import com.example.savebite.ui.navigation.AppTopBar
import com.example.savebite.ui.viewmodel.ShoppingViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddShoppingItemScreen(
    itemId: String? = null,
    viewModel: ShoppingViewModel,
    onBackClick: () -> Unit
) {
    val items by viewModel.items.collectAsState()
    val existingItem = remember(itemId, items) { items.find { it.id == itemId } }

    var name by remember { mutableStateOf("") }
    var quantity by remember { mutableStateOf("1") }
    var unit by remember { mutableStateOf("pcs") }
    var category by remember { mutableStateOf("Dairy & Eggs") }

    // Synchronizes form inputs when an existing item is loaded for editing.
    LaunchedEffect(existingItem) {
        existingItem?.let {
            name = it.name
            quantity = it.quantity.toString()
            unit = it.unit
            category = it.category
        }
    }

    var categoryExpanded by remember { mutableStateOf(false) }
    var unitExpanded by remember { mutableStateOf(false) }

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
        topBar = {
            AppTopBar(
                title = if (itemId != null) stringResource(R.string.shopping_edit_title) else stringResource(R.string.shopping_add_title),
                showBackButton = true,
                onBackClick = onBackClick
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text(stringResource(R.string.shopping_name_label)) },
                placeholder = { Text(stringResource(R.string.shopping_name_placeholder)) },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                singleLine = true
            )

            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = quantity,
                    onValueChange = { if (it.all { char -> char.isDigit() }) quantity = it },
                    label = { Text(stringResource(R.string.shopping_quantity_label)) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true
                )

                ExposedDropdownMenuBox(
                    expanded = unitExpanded,
                    onExpandedChange = { unitExpanded = !unitExpanded },
                    modifier = Modifier.weight(1f)
                ) {
                    OutlinedTextField(
                        value = unit,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text(stringResource(R.string.shopping_unit_label))},
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = unitExpanded) },
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

            // Category Dropdown
            ExposedDropdownMenuBox(
                expanded = categoryExpanded,
                onExpandedChange = { categoryExpanded = !categoryExpanded }
            ) {
                OutlinedTextField(
                    value = category,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text(stringResource(R.string.shopping_category_label)) },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = categoryExpanded) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .menuAnchor(),
                    shape = RoundedCornerShape(12.dp)
                )
                ExposedDropdownMenu(
                    expanded = categoryExpanded,
                    onDismissRequest = { categoryExpanded = false }
                ) {
                    categoryOptions.forEach { option ->
                        DropdownMenuItem(
                            text = { Text(option) },
                            onClick = {
                                category = option
                                categoryExpanded = false
                            }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.weight(1f))

            Button(
                onClick = {
                    if (name.isNotBlank()) {
                        val parsedQty = quantity.toIntOrNull() ?: 1
                        val cleanUnit = unit.ifBlank { "pcs" }

                        if (existingItem != null) {
                            viewModel.updateItem(
                                existingItem.copy(
                                    name = name.trim(),
                                    quantity = parsedQty,
                                    unit = cleanUnit,
                                    category = category
                                )
                            )
                        } else {
                            viewModel.addItem(
                                name = name.trim(),
                                quantity = parsedQty,
                                unit = cleanUnit,
                                category = category
                            )
                        }
                        onBackClick()
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
                    text = if (itemId != null) stringResource(R.string.shopping_action_update) else stringResource(R.string.shopping_action_save),
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}