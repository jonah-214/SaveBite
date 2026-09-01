package com.example.savebite.utils

import com.example.savebite.model.Inventory

// Show the section label in the UI, e.g. "Expiring Today", "This Week", "Later"
enum class ExpirySection(val label: String) {
    TODAY("Expiring Today"),
    THIS_WEEK("This Week"),
    LATER("Later")
}

object ExpiryGrouping {

    // Threshold for "This Week" section is 7 days. Items expiring in 1-7 days are grouped under "This Week".
    const val THIS_WEEK_THRESHOLD_DAYS = 7

    fun sectionFor(daysLeft: Int): ExpirySection = when {
        daysLeft <= 0 -> ExpirySection.TODAY
        daysLeft in 1..THIS_WEEK_THRESHOLD_DAYS -> ExpirySection.THIS_WEEK
        else -> ExpirySection.LATER
    }

    // Group the inventory items by their expiry section
    fun group(items: List<Inventory>): Map<ExpirySection, List<Inventory>> =
        items
            .sortedBy { it.daysLeft }
            .groupBy { sectionFor(it.daysLeft) }
}
