package com.example.savebite.utils

import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

object DateFormats {
    // Pattern used to SAVE dates to the database (Room + Supabase).
    // ISO 8601 order (year-month-day) so plain string sorting ("a" < "b") already
    // matches chronological order - no need to parse the date just to sort by it.
    private const val STORAGE_PATTERN = "yyyy-MM-dd"

    // Pattern used only to SHOW dates to the user, e.g. "05 Sep 2026".
    private const val DISPLAY_PATTERN = "dd MMM yyyy"

    // Older app versions saved dates using DISPLAY_PATTERN instead of STORAGE_PATTERN.
    // Kept here so existing rows created before this change can still be read.
    private const val LEGACY_STORAGE_PATTERN = DISPLAY_PATTERN

    private fun storageFormatter(): SimpleDateFormat =
        SimpleDateFormat(STORAGE_PATTERN, Locale.US)

    private fun displayFormatter(): SimpleDateFormat =
        SimpleDateFormat(DISPLAY_PATTERN, Locale.getDefault())

    private fun legacyStorageFormatter(): SimpleDateFormat =
        SimpleDateFormat(LEGACY_STORAGE_PATTERN, Locale.getDefault())

    /** Turns a Date into the string that should be SAVED to the database. */
    fun toStorageString(date: Date): String = storageFormatter().format(date)

    /**
     * Turns a stored date string (new ISO format, or the old "dd MMM yyyy" format
     * from before this change) into the human-friendly text shown in the UI.
     * Falls back to returning the raw string if it can't be parsed at all.
     */
    fun toDisplayString(stored: String): String =
        parseExpiryOrNull(stored)?.let { displayFormatter().format(it) } ?: stored

    /**
     * Takes a stored date string and turns it into a real Date object.
     * Accepts both the current storage format and the legacy display format,
     * since existing rows may still be in the old format.
     * If the text is not a valid date in either format, returns null instead of crashing.
     */
    fun parseExpiryOrNull(dateStr: String): Date? =
        try {
            storageFormatter().parse(dateStr)
        } catch (e: ParseException) {
            try {
                legacyStorageFormatter().parse(dateStr)
            } catch (e2: ParseException) {
                null
            }
        }

    /**
     * Zeroes out the time-of-day, keeping only the calendar date.
     * Use this before diffing two dates in whole days (e.g. "days left"), so the
     * result depends only on the dates involved and not on what time it is right now.
     */
    fun startOfDay(date: Date): Date {
        val calendar = Calendar.getInstance()
        calendar.time = date
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)
        return calendar.time
    }
}
