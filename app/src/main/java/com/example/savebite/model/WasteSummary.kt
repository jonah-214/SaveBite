package com.example.savebite.model

// Simple analytics summary of wasted items for a given period, used on the Dashboard
// instead of a bar chart.
data class WasteSummary(
    val totalItemsWasted: Int = 0,
    val totalValueWasted: Double = 0.0,
    val topCategory: String? = null,
    val topCategoryCount: Int = 0
)
