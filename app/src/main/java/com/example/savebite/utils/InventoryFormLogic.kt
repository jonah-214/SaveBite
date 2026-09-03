package com.example.savebite.utils

import com.example.savebite.model.Inventory
import java.util.Date
import java.util.Locale
import java.util.UUID
import java.util.concurrent.TimeUnit

/**
 * Pure, framework-free logic for the Add/Edit Inventory form: price cleaning/validation,
 * days-left calculation, and building the Inventory to save. Kept separate from
 * AddInventoryScreen so it can be unit tested without Compose, and so the screen
 * doesn't end up building an Inventory from the form fields in two different places
 * (Save, and stepping back a step in batch mode).
 */
object InventoryFormLogic {

    /** The fields a user fills in on the Add/Edit Inventory screen. */
    data class FormDraft(
        val name: String,
        val description: String,
        val category: String,
        val storage: String,
        val quantity: Int,
        val unit: String,
        val priceInput: String,
        val purchaseDate: String,
        val expiryDate: String,
        val notes: String,
        val isConsumed: Boolean
    )

    /** Strips currency symbols/formatting so the price text can be parsed as a number. */
    fun cleanPriceString(input: String): String =
        input.trim()
            .replace(Currency.PREFIX, "", ignoreCase = true)
            .replace(",", ".")
            .replace(Regex("[^0-9.]"), "")

    /** Parses the price input, returning null if it's blank, not a number, or negative. */
    fun parsePriceOrNull(input: String): Double? {
        val cleaned = cleanPriceString(input)
        val value = cleaned.toDoubleOrNull() ?: return null
        return if (value >= 0.0) value else null
    }

    fun isPriceValid(input: String): Boolean = parsePriceOrNull(input) != null

    /** Formats a stored price back into editable text, e.g. for pre-filling the form. */
    fun formatPriceString(priceValue: Double): String =
        if (priceValue > 0) String.format(Locale.US, "%.2f", priceValue) else ""

    /** Days remaining until [expiryDateStr], counting today as day 1. 0 if already expired or unparsable. */
    fun calculateDaysLeft(expiryDateStr: String): Int {
        val expiryDate = DateFormats.parseExpiryOrNull(expiryDateStr) ?: return 0
        val diffInMillis = expiryDate.time - Date().time
        val days = TimeUnit.DAYS.convert(diffInMillis, TimeUnit.MILLISECONDS).toInt()
        return if (days < 0) 0 else days + 1
    }

    private fun assemble(draft: FormDraft, existingId: String?, price: Double): Inventory = Inventory(
        id = existingId ?: UUID.randomUUID().toString(),
        name = draft.name,
        description = draft.description,
        category = draft.category,
        storage = draft.storage,
        quantity = draft.quantity,
        unit = draft.unit,
        price = price,
        daysLeft = calculateDaysLeft(draft.expiryDate),
        purchaseDate = draft.purchaseDate,
        expiry = draft.expiryDate,
        notes = draft.notes,
        isConsumed = draft.isConsumed
    )

    /**
     * Stashes the current form as a batch-mode draft when the user steps back to a
     * previous item. Doesn't require a valid price yet since the item may still be
     * mid-edit; an invalid/blank price is stored as 0.0, same as before this change.
     */
    fun buildDraft(draft: FormDraft, existingId: String?): Inventory =
        assemble(draft, existingId, parsePriceOrNull(draft.priceInput) ?: 0.0)

    /**
     * Builds the Inventory to actually save. Returns null if the form isn't valid
     * (blank name, or an unparsable/negative price) so the screen can show the
     * validation errors instead of saving.
     */
    fun buildInventoryOrNull(draft: FormDraft, existingId: String?): Inventory? {
        if (draft.name.isBlank()) return null
        val price = parsePriceOrNull(draft.priceInput) ?: return null
        return assemble(draft, existingId, price)
    }
}
