package com.example.savebite.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import com.example.savebite.utils.ExpirySection

/**
 * Single source of truth for the Red / Yellow / Green urgency colors used by
 * both the Dashboard expiry card and the Reminder screen, so they always
 * stay in sync:
 *  - SOON (0-3 days left)      -> Red
 *  - THIS_WEEK (4-7 days left) -> Yellow/Amber
 *  - LATER (8+ days left)      -> Green
 *
 * Returns (containerColor, onContainerColor).
 */
@Composable
fun expirySectionColors(section: ExpirySection): Pair<Color, Color> {
    val isDark = isSystemInDarkTheme()
    return when (section) {
        ExpirySection.SOON ->
            MaterialTheme.colorScheme.errorContainer to MaterialTheme.colorScheme.onErrorContainer
        ExpirySection.THIS_WEEK ->
            if (isDark) warningContainerDark to onWarningContainerDark
            else warningContainerLight to onWarningContainerLight
        ExpirySection.LATER ->
            MaterialTheme.colorScheme.primaryContainer to MaterialTheme.colorScheme.onPrimaryContainer
    }
}

/** Convenience overload that buckets [daysLeft] first. */
@Composable
fun expirySectionColors(daysLeft: Int): Pair<Color, Color> =
    expirySectionColors(com.example.savebite.utils.ExpiryGrouping.sectionFor(daysLeft))
