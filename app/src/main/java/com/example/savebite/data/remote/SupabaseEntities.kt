package com.example.savebite.data.remote

import com.example.savebite.model.Inventory
import com.example.savebite.model.ReportItem
import com.example.savebite.model.ShoppingItem
import com.example.savebite.model.Storage
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

// 1. Inventory DTO
@Serializable
data class SupabaseInventory(
    val id: String,
    @SerialName("user_id") val userId: String? = null,
    val name: String,
    val description: String = "",
    val category: String = "General",
    val storage: String,
    val quantity: Int,
    val unit: String,
    @SerialName("days_left") val daysLeft: Int,
    @SerialName("purchase_date") val purchaseDate: String = "",
    val expiry: String,
    val notes: String = "",
    @SerialName("is_consumed") val isConsumed: Boolean = false
)

// 2. ShoppingItem DTO
@Serializable
data class SupabaseShoppingItem(
    val id: String,
    @SerialName("user_id") val userId: String? = null,
    val name: String,
    val quantity: Int,
    val unit: String,
    val category: String,
    @SerialName("is_purchased") val isPurchased: Boolean = false
)

// 3. Storage DTO
@Serializable
data class SupabaseStorage(
    val name: String,
    @SerialName("user_id") val userId: String? = null
)

// 4. ReportItem DTO
@Serializable
data class SupabaseReportItem(
    val id: String,
    @SerialName("user_id") val userId: String? = null,
    val name: String,
    val category: String,
    val price: Double = 0.0,
    val quantity: Int = 1,
    val unit: String = "pcs",
    val status: String, // "CONSUMED" 或 "WASTED"
    val reason: String = ""
)

// --- Mapper 转换函数（Room <-> Supabase） ---

fun Inventory.toSupabase(userId: String? = null) = SupabaseInventory(
    id = id,
    userId = userId,
    name = name,
    description = description,
    category = category,
    storage = storage,
    quantity = quantity,
    unit = unit,
    daysLeft = daysLeft,
    purchaseDate = purchaseDate,
    expiry = expiry,
    notes = notes,
    isConsumed = isConsumed
)

fun SupabaseInventory.toRoom() = Inventory(
    id = id,
    name = name,
    description = description,
    category = category,
    storage = storage,
    quantity = quantity,
    unit = unit,
    daysLeft = daysLeft,
    purchaseDate = purchaseDate,
    expiry = expiry,
    notes = notes,
    isConsumed = isConsumed
)

fun ShoppingItem.toSupabase(userId: String? = null) = SupabaseShoppingItem(
    id = id,
    userId = userId,
    name = name,
    quantity = quantity,
    unit = unit,
    category = category,
    isPurchased = isPurchased
)

fun SupabaseShoppingItem.toRoom() = ShoppingItem(
    id = id,
    name = name,
    quantity = quantity,
    unit = unit,
    category = category,
    isPurchased = isPurchased
)

fun ReportItem.toSupabase(userId: String? = null) = SupabaseReportItem(
    id = id,
    userId = userId,
    name = name,
    category = category,
    price = price,
    quantity = quantity,
    unit = unit,
    status = status.name,
    reason = reason
)

fun SupabaseReportItem.toRoom() = ReportItem(
    id = id,
    name = name,
    category = category,
    price = price,
    quantity = quantity,
    unit = unit,
    status = com.example.savebite.model.ReportStatus.valueOf(status),
    reason = reason
)

fun Storage.toSupabase(userId: String? = null) = SupabaseStorage(name = name, userId = userId)
fun SupabaseStorage.toRoom() = Storage(name = name)
