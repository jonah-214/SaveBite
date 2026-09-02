package com.example.savebite.model

data class ExpiryItem(
    val id: String,
    val name: String,
    val quantity: String,
    val daysLeft: Int,
    val category: String,
)

data class RecipeSuggestion(
    val name: String,
    val usesText: String,
)
