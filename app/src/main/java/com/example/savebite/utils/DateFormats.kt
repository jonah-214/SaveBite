package com.example.savebite.utils

import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object DateFormats {
    // This is the pattern we use for dates: Day, Month (short name), Year.
    private const val EXPIRY_DISPLAY_PATTERN = "dd MMM yyyy"

    /** Creates a "formatter" that can turn a Date object into text or vice-versa. */
    fun expiryFormatter(): SimpleDateFormat =
        SimpleDateFormat(EXPIRY_DISPLAY_PATTERN, Locale.getDefault())

    /** 
     * Takes a String like "05 Sep 2026" and turns it into a real Date object. 
     * If the text is not a valid date, it returns null instead of crashing.
     */
    fun parseExpiryOrNull(dateStr: String): Date? = try {
        expiryFormatter().parse(dateStr)
    } catch (e: ParseException) {
        null
    }
}
