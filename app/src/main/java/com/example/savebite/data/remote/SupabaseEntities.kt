package com.example.savebite.data.remote

import com.example.savebite.model.Inventory
import com.example.savebite.model.ReportItem
import com.example.savebite.model.ReportStatus
import com.example.savebite.model.ShoppingItem
import com.example.savebite.model.Storage
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

// Inventory DTO
@Serializable
data class SupabaseInventory(
    val id: String,
    @SerialName("user_id") val userId: String? = null,
    val name: String,
    val description: String,
    val category: String,
    val storage: String,
    val quantity: Int,
    val unit: String,
    val price: Double,
    @SerialName("days_left") val daysLeft: Int,
    @SerialName("purchase_date") val purchaseDate: String,
    val expiry: String,
    val notes: String,
    @SerialName("is_consumed") val isConsumed: Boolean
)

// ShoppingItem DTO
@Serializable
data class SupabaseShoppingItem(
    val id: String,
    @SerialName("user_id") val userId: String? = null,
    val name: String,
    val quantity: Int,
    val unit: String,
    val category: String,
    @SerialName("is_purchased") val isPurchased: Boolean
)

// Storage DTO
@Serializable
data class SupabaseStorage(
    val name: String,
    @SerialName("user_id") val userId: String? = null
)

// ReportItem DTO
@Serializable
data class SupabaseReportItem(
    val id: String,
    @SerialName("user_id") val userId: String? = null,
    val name: String,
    val category: String,
    val price: Double,
    val quantity: Int,
    val unit: String,
    val status: String,
    val reason: String,
    val timestamp: Long
)

// --- Mapper（Room <-> Supabase） ---
// Mapper function ( Room & Supabase)

// Converts a local Room Inventory entity into a Supabase DTO, injecting the active user's ID
fun Inventory.toSupabase(userId: String? = null) = SupabaseInventory(
    id = id,
    userId = userId,
    name = name,
    description = description,
    category = category,
    storage = storage,
    quantity = quantity,
    unit = unit,
    price = price,
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
    price = price,
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

// Converts local enum-based ReportItem to remote String status representation.
fun ReportItem.toSupabase(userId: String? = null) = SupabaseReportItem(
    id = id,
    userId = userId,
    name = name,
    category = category,
    price = price,
    quantity = quantity,
    unit = unit,
    status = status.name,
    reason = reason,
    timestamp = timestamp
)

// Converts remote String status back into local ReportStatus enum with a fallback to prevent crash loops on schema changes.
fun SupabaseReportItem.toRoom() = ReportItem(
    id = id,
    name = name,
    category = category,
    price = price,
    quantity = quantity,
    unit = unit,
    status = try {
        ReportStatus.valueOf(status.uppercase())
    } catch (e: Exception) {
        ReportStatus.CONSUMED
    },
    reason = reason,
    timestamp = timestamp
)

fun Storage.toSupabase(userId: String? = null) = SupabaseStorage(name = name, userId = userId)
fun SupabaseStorage.toRoom() = Storage(name = name)
