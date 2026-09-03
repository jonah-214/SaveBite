package com.example.savebite.data.remote

import io.github.jan.supabase.auth.Auth
import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.postgrest.Postgrest
import io.github.jan.supabase.storage.Storage

// SupabaseClientProvider initializes and holds the singleton instance of the Supabase Client
// It installs the necessary modules: Auth, Postgrest (Database), and Storage
object SupabaseClientProvider {
    val client = createSupabaseClient(
        supabaseUrl = "https://ehjkuwpuwqthgzueezpi.supabase.co",
        supabaseKey = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6ImVoamt1d3B1d3F0aGd6dWVlenBpIiwicm9sZSI6ImFub24iLCJpYXQiOjE3ODY5NjIzODYsImV4cCI6MjEwMjUzODM4Nn0.66jElqHBveZlckQiWTt0TW1UIK6ilzds7qlKQ0bGopI"
    ) {
        install(Auth)
        install(Postgrest)
        install(Storage)
    }
}