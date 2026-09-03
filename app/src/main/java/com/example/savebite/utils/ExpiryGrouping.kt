package com.example.savebite.utils

import com.example.savebite.R
import com.example.savebite.model.Inventory

// Expiry sections for grouping items
enum class ExpirySection(val labelRes: Int) {
    SOON(R.string.expiry_soon),
    THIS_WEEK(R.string.expiry_this_week),
    LATER(R.string.expiry_later)
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
