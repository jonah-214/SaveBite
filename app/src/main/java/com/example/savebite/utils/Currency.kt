package com.example.savebite.utils

import java.util.Locale

// Make the currency sharable to each screen instead of writing hardcode "RM" string in each screen
object Currency {
    const val PREFIX = "RM"

    // e.g. "RM 12.50"
    fun format(amount: Double): String =
        String.format(Locale.getDefault(), "%s %.2f", PREFIX, amount)

    // e.g. "RM 12.50 (3 items)"
    fun formatWithCount(amount: Double, count: Int): String =
        String.format(Locale.getDefault(), "%s %.2f (%d items)", PREFIX, amount, count)
}
