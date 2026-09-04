package com.example.savebite.data.remote

import com.example.savebite.BuildConfig
import io.github.jan.supabase.auth.Auth
import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.postgrest.Postgrest
import io.github.jan.supabase.storage.Storage

// SupabaseClientProvider initializes and holds the singleton instance of the Supabase Client
// It installs the necessary modules: Auth, Postgrest (Database), and Storage.
// URL/key come from local.properties (via BuildConfig) rather than being hardcoded here,
// matching how the Gemini API key is wired in AppContainer.
object SupabaseClientProvider {
    val client = createSupabaseClient(
        supabaseUrl = BuildConfig.SUPABASE_URL,
        supabaseKey = BuildConfig.SUPABASE_KEY
    ) {
        install(Auth)
        install(Postgrest)
        install(Storage)
    }
}