package com.example.savebite.ui.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.RemoveCircle
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
import com.example.savebite.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ManageStorageScreen(
    storages: List<String> = emptyList(),
    onBackClick: () -> Unit = {},
    onAddStorageClick: (String) -> Unit = {},
    onDeleteStorageClick: (String) -> Unit = {}
) {
    var showAddDialog by remember { mutableStateOf(false) }
    var newStorageName by remember { mutableStateOf("") }
    var storageToDelete by remember { mutableStateOf<String?>(null) }

    Scaffold(
        containerColor = backgroundLight,
        topBar = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp)
            ) {
                TopAppBar(
                    title = {
                        Text(
                            text = "Manage Storage",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = onPrimaryLight
                        )
                    },
                    navigationIcon = {
                        IconButton(onClick = onBackClick) {
                            Icon(
                                painter = painterResource(R.drawable.close),
                                contentDescription = "Back",
                                tint = onPrimaryLight,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = primaryLight
                    )
                )
                Text(
                    text = "Add, edit or delete your storage places",
                    fontSize = 13.sp,
                    color = outlineLight,
                    modifier = Modifier.padding(start = 16.dp, top = 4.dp)
                )
            }
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showAddDialog = true },
                containerColor = onPrimaryContainerLight
            ) {
                Icon(
                    painter = painterResource(R.drawable.add),
                    contentDescription = "Add Storage Place",
                    tint = Color.White,
                    modifier = Modifier.size(36.dp)
                )
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp)
                .verticalScroll(rememberScrollState())
        ) {
            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Your Storage Places",
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                color = onSurfaceVariantLight,
                modifier = Modifier.padding(bottom = 12.dp)
            )

            Card(
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = surfaceContainerLowLight),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column {
                    storages.forEachIndexed { index, place ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Icon Avatar
                            IconButton(
                                onClick = {},
                                enabled = false,
                                modifier = Modifier
                                    .size(44.dp)
                                    .background(secondaryContainerLight, shape = CircleShape)
                            ) {
                                Icon(
                                    painter = painterResource(R.drawable.add),
                                    contentDescription = null,
                                    modifier = Modifier.size(22.dp),
                                    tint = onSecondaryContainerLight
                                )
                            }

                            Spacer(modifier = Modifier.width(12.dp))

                            // Name and Description
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = place,
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = onSurfaceLight
                                )
                            }

                            // Delete Action Button
                            if (place != "All" && place != "Refrigerator") {
                                IconButton(
                                    onClick = { storageToDelete = place },
                                    modifier = Modifier.size(28.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.RemoveCircle,
                                        contentDescription = "Delete",
                                        tint = errorLight,
                                        modifier = Modifier.size(24.dp)
                                    )
                                }
                            }
                        }

                        if (index < storages.size - 1) {
                            HorizontalDivider(
                                color = outlineVariantLight,
                                thickness = 1.dp
                            )
                        }
                    }
                }
            }
        }
    }

    if (showAddDialog) {
        AlertDialog(
            containerColor = surfaceContainerLowestLight,
            onDismissRequest = { showAddDialog = false },
            shape = RoundedCornerShape(20.dp),
            title = {
                Text(
                    text = "Add New Storage Location",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = onSurfaceLight
                )
            },
            text = {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp)
                ) {
                    OutlinedTextField(
                        value = newStorageName,
                        onValueChange = { newStorageName = it },
                        label = { Text("Storage Name") },
                        placeholder = { Text("e.g. Snack Box, Cellar") },
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = primaryLight,
                            focusedLabelColor = primaryLight,
                            unfocusedTextColor = onSurfaceLight,
                            focusedTextColor = onSurfaceLight
                        )
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (newStorageName.isNotBlank()) {
                            onAddStorageClick(newStorageName.trim())
                            newStorageName = ""
                            showAddDialog = false
                        }
                    },
                    enabled = newStorageName.isNotBlank(),
                    colors = ButtonDefaults.buttonColors(containerColor = primaryLight),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text("Save", color = onPrimaryLight)
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddDialog = false }) {
                    Text("Cancel", color = outlineLight)
                }
            }
        )
    }

    if (storageToDelete != null) {
        AlertDialog(
            onDismissRequest = { storageToDelete = null },
            title = { Text(text = "Delete Storage Location", color = onSurfaceLight) },
            text = {
                Text(
                    text = "Are you sure you want to delete \"${storageToDelete}\"? All items in this storage will be moved to \"Refrigerator\".",
                    color = onSurfaceVariantLight
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        storageToDelete?.let { onDeleteStorageClick(it) }
                        storageToDelete = null
                    }
                ) {
                    Text("Delete", color = errorLight)
                }
            },
            dismissButton = {
                TextButton(onClick = { storageToDelete = null }) {
                    Text("Cancel", color = outlineLight)
                }
            },
            containerColor = surfaceContainerLowestLight,
            shape = RoundedCornerShape(16.dp)
        )
    }
}