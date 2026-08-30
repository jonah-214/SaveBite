package com.example.savebite.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.UUID

@Entity(tableName = "inventory_table")
data class Inventory(
    @PrimaryKey
    val id: String = UUID.randomUUID().toString(),
    val name: String,
    val description: String = "",
    val category: String = "General",
    val storage: String,
    val quantity: Int,
    val unit: String,
    val price: Double = 0.0, // 新增：采购价格/发票总金额
    val daysLeft: Int,
    val purchaseDate: String = "",
    val expiry: String,
    val notes: String = "",
    val isConsumed: Boolean = false
) {

    fun calculateLostCost(wastedQty: Double): Double {
        if (quantity <= 0) return 0.0
        return (wastedQty / quantity) * price
    }
}