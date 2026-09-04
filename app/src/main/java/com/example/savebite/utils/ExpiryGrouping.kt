package com.example.savebite.utils

import com.example.savebite.R
import com.example.savebite.model.Inventory

// Defines the logical sections for grouping inventory items based on their urgency
enum class ExpirySection(val labelRes: Int) {
    // Highly urgent items (0-2 days left)
    SOON(R.string.expiry_soon),
    
    // Moderate urgency (3-7 days left)
    THIS_WEEK(R.string.expiry_this_week),
    
    // Low urgency (8+ days left)
    LATER(R.string.expiry_later)
}

// Utility for categorizing and grouping food items based on their days until expiry.
object ExpiryGrouping {

    // Threshold for items considered to be "Expiring Soon"
    const val SOON_THRESHOLD_DAYS = 2

    // Threshold for items considered to be expiring "This Week"
    const val THIS_WEEK_THRESHOLD_DAYS = 7

    // Determines the correct [ExpirySection] for a given number of days
    fun sectionFor(daysLeft: Int): ExpirySection = when {
        daysLeft <= SOON_THRESHOLD_DAYS -> ExpirySection.SOON
        daysLeft in (SOON_THRESHOLD_DAYS + 1)..THIS_WEEK_THRESHOLD_DAYS -> ExpirySection.THIS_WEEK
        else -> ExpirySection.LATER
    }

    // Groups a list of inventory items by their calculated [ExpirySection]
    fun group(items: List<Inventory>): Map<ExpirySection, List<Inventory>> =
        items.groupBy { sectionFor(it.daysLeft) }
}