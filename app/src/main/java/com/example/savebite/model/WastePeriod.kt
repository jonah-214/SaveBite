package com.example.savebite.model

import com.example.savebite.R

//Represents the time period for filtering waste report data on the dashboard.
// Each entry holds a reference to its display name in string resources.
enum class WastePeriod(val labelRes: Int) {
    // Show data for the last 4 weeks.
    WEEKLY(R.string.dashboard_period_weekly),

    // Show data for the last 6 months.
    MONTHLY(R.string.dashboard_period_monthly),

    // Show data for the last 3 years.
    YEARLY(R.string.dashboard_period_yearly)
}