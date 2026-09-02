package com.example.savebite.utils

import com.example.savebite.model.Inventory

// Show the section label in the UI, e.g. "Expiring Soon", "This Week", "Later"
enum class ExpirySection(val label: String) {
    SOON("Expiring Soon"),
    THIS_WEEK("This Week"),
    LATER("Later")
}

object ExpiryGrouping {

    // Items expiring in 0-2 days (inclusive) are "Expiring Soon".
    const val SOON_THRESHOLD_DAYS = 2

    // Items expiring in 4-7 days are grouped under "This Week". 8+ days falls under "Later".
    const val THIS_WEEK_THRESHOLD_DAYS = 7

    fun sectionFor(daysLeft: Int): ExpirySection = when {
        daysLeft <= SOON_THRESHOLD_DAYS -> ExpirySection.SOON
        daysLeft in (SOON_THRESHOLD_DAYS + 1)..THIS_WEEK_THRESHOLD_DAYS -> ExpirySection.THIS_WEEK
        else -> ExpirySection.LATER
    }

    // Group the inventory items by their expiry section
    fun group(items: List<Inventory>): Map<ExpirySection, List<Inventory>> =
        items.groupBy { sectionFor(it.daysLeft) }
}
