package com.example.savebite.screen

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.savebite.model.Inventory

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InventoryDetailScreen(
    detail: Inventory,
    onBackClick: () -> Unit = {},
    onEditClick: () -> Unit = {},
    onDeleteClick: () -> Unit = {}
) {
    Scaffold(
        containerColor = Color.White,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Inventory Details",
                        color = Color.White,
                        fontSize = 20.sp
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            painter = painterResource(com.example.savebite.R.drawable.back),
                            contentDescription = "Back",
                            tint = Color.White,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color(0xFF148A1E)
                )
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(20.dp)
        ) {
            // 1. Header Row: Name & Category Badge
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = detail.name,
                    fontSize = 32.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.Black
                )

                // Light Blue Category Pill
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = Color(0xFFD0E4FF)
                ) {
                    Text(
                        text = detail.category,
                        color = Color(0xFF0056D2),
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(4.dp))

            // Subtitle / Description
            Text(
                text = detail.description,
                fontSize = 18.sp,
                fontWeight = FontWeight.Medium,
                color = Color.Black
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Light Green Storage Pill Tag
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = Color(0xFFE2F3E4)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    // Refrigerator Icon (Uses a built-in vector or painterResource)
                    Icon(
                        painter = painterResource(com.example.savebite.R.drawable.refrigerator),
                        contentDescription = "Storage",
                        tint = Color(0xFF148A1E),
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = detail.storage,
                        color = Color(0xFF148A1E),
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
            HorizontalDivider(color = Color(0xFFE0E0E0), thickness = 1.dp)
            Spacer(modifier = Modifier.height(20.dp))

            // 2. Quantity & Status Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Quantity Section
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    // Quantity Green Icon Box
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = Color.Transparent,
                        border = BorderStroke(2.dp, Color(0xFF148A1E)),
                        modifier = Modifier.size(48.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                painter = painterResource(com.example.savebite.R.drawable.inventory),
                                contentDescription = "Quantity",
                                tint = Color(0xFF148A1E),
                                modifier = Modifier.size(24.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    Column {
                        Text(
                            text = "Quantity",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color.Black
                        )
                        Text(
                            text = "${detail.quantity}",
                            fontSize = 16.sp,
                            color = Color.Gray
                        )
                    }
                }

                // Vertical Divider Line
                Divider(
                    modifier = Modifier
                        .height(50.dp)
                        .width(1.dp),
                    color = Color(0xFFE0E0E0)
                )

                // Status Days Left Section
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .weight(1f)
                        .padding(start = 16.dp)
                ) {
                    // Orange Clock Icon
                    Icon(
                        painter = painterResource(com.example.savebite.R.drawable.clock), // replace with clock icon
                        contentDescription = "Status",
                        tint = Color(0xFFFFB300),
                        modifier = Modifier.size(48.dp)
                    )

                    Spacer(modifier = Modifier.width(12.dp))

                    Column {
                        Text(
                            text = "Status",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color.Black
                        )
                        Text(
                            text = "${detail.daysLeft} days left",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium,
                            color = Color(0xFFFF8F00)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // 3. Details Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                border = BorderStroke(1.dp, Color(0xFFD0D0D0)),
                colors = CardDefaults.cardColors(containerColor = Color.White)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "Details",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF148A1E)
                    )

                    // Purchase Date Row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            painter = painterResource(com.example.savebite.R.drawable.calendar),
                            contentDescription = null,
                            tint = Color(0xFF148A1E),
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = "Purchase Date",
                            fontSize = 14.sp,
                            color = Color.Black,
                            modifier = Modifier.weight(1f)
                        )
                        Text(
                            text = detail.purchaseDate,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium,
                            color = Color.Black
                        )
                    }

                    HorizontalDivider(color = Color(0xFFF0F0F0), thickness = 1.dp)

                    // Expiry Date Row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            painter = painterResource(com.example.savebite.R.drawable.calendar_clock),
                            contentDescription = null,
                            tint = Color.Black,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = "Expiry Date",
                            fontSize = 14.sp,
                            color = Color.Black,
                            modifier = Modifier.weight(1f)
                        )
                        Row {
                            Text(
                                text = detail.expiry,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Medium,
                                color = Color.Black
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "(${detail.daysLeft} days left)",
                                fontSize = 14.sp,
                                color = Color(0xFFFF8F00)
                            )
                        }
                    }

                    HorizontalDivider(color = Color(0xFFF0F0F0), thickness = 1.dp)

                    // Notes Row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.Top
                    ) {
                        Icon(
                            painter = painterResource(com.example.savebite.R.drawable.note),
                            contentDescription = null,
                            tint = Color(0xFF148A1E),
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = "Notes",
                            fontSize = 14.sp,
                            color = Color.Black,
                            modifier = Modifier.weight(0.4f)
                        )
                        Text(
                            text = detail.notes,
                            fontSize = 14.sp,
                            color = Color.Black,
                            modifier = Modifier.weight(0.6f)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            // 4. Edit & Delete Action Buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Edit Button
                OutlinedButton(
                    onClick = onEditClick,
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp),
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(1.dp, Color(0xFFCCCCCC))
                ) {
                    Icon(
                        painter = painterResource(com.example.savebite.R.drawable.edit),
                        contentDescription = "Edit",
                        tint = Color(0xFF148A1E),
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Edit",
                        color = Color(0xFF148A1E),
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Medium
                    )
                }

                // Delete Button
                OutlinedButton(
                    onClick = onDeleteClick,
                    modifier = Modifier
                        .weight(1.2f)
                        .height(48.dp),
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(1.dp, Color(0xFFCCCCCC))
                ) {
                    Icon(
                        painter = painterResource(com.example.savebite.R.drawable.delete),
                        contentDescription = "Delete",
                        tint = Color.Red,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Delete",
                        color = Color.Red,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }
    }
}