package com.example.savebite.model


// Result of a background cloud sync (Supabase -> Room) triggered by a screen on load.
sealed class SyncStatus {
    data object Idle : SyncStatus()
    data object Syncing : SyncStatus()
    data class Error(val message: String) : SyncStatus()
}
