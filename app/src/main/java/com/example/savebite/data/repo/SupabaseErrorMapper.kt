package com.example.savebite.data.repo

// Map known Supabase/Postgres unique-constraint exceptions (from sign-up or profile update)
// to the CONFLICT_* codes ViewModels already switch on. Shared by SupabaseAuthRepository
// (sign-up) and ProfileRepository (profile update) since both hit the same "profiles" table
// and can fail with the same duplicate-username/email/phone errors.
fun mapSupabaseConflictException(error: Exception): Exception {
    val message = error.message.orEmpty()
    val normalized = message.lowercase()
    val hasUniqueViolation = normalized.contains("duplicate key value violates unique constraint") ||
            normalized.contains("unique constraint") ||
            normalized.contains("23505")

    return when {
        // Supabase Auth duplicate-email response
        normalized.contains("user already registered") ->
            Exception("CONFLICT_EMAIL")

        // Postgres unique-constraint errors from profiles insert/update
        normalized.contains("profiles_username_key") ->
            Exception("CONFLICT_USERNAME")
        normalized.contains("profiles_email_key") ->
            Exception("CONFLICT_EMAIL")
        normalized.contains("profiles_phone_key") ->
            Exception("CONFLICT_PHONE")

        // Alternate Postgres details format:
        // Key (username)=(...) already exists.
        hasUniqueViolation &&
                (normalized.contains("key (username)") || normalized.contains("(username)")) ->
            Exception("CONFLICT_USERNAME")
        hasUniqueViolation &&
                (normalized.contains("key (phone)") || normalized.contains("(phone)")) ->
            Exception("CONFLICT_PHONE")
        hasUniqueViolation &&
                (normalized.contains("key (email)") || normalized.contains("(email)")) ->
            Exception("CONFLICT_EMAIL")

        // Generic fallback patterns
        normalized.contains("username") &&
                (normalized.contains("already") ||
                        normalized.contains("taken") ||
                        normalized.contains("exists") ||
                        normalized.contains("duplicate")) ->
            Exception("CONFLICT_USERNAME")
        normalized.contains("phone") &&
                (normalized.contains("already") ||
                        normalized.contains("used") ||
                        normalized.contains("exists") ||
                        normalized.contains("duplicate")) ->
            Exception("CONFLICT_PHONE")
        normalized.contains("email") &&
                (normalized.contains("already") ||
                        normalized.contains("used") ||
                        normalized.contains("exists") ||
                        normalized.contains("duplicate")) ->
            Exception("CONFLICT_EMAIL")

        else -> error
    }
}
