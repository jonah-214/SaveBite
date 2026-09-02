package com.example.savebite.model

enum class WastePeriod(val label: String) {
    WEEKLY("Weekly"),   // Show data for the last 4 weeks
    MONTHLY("Monthly"), // Show data for the last 6 months
    YEARLY("Yearly")    // Show data for the last 3 years
}