package com.example.savebite.model

data class ShoppingItem(
    val id: Int,
    val name: String,
    val quantity: Double,
    val unit: String,
    val category: String,
    var purchased: Boolean = false
)
