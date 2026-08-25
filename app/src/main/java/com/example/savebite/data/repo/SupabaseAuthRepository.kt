package com.example.savebite.data.repo

import com.example.savebite.data.remote.SupabaseClientProvider
import com.example.savebite.model.User
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.auth.providers.builtin.Email
import io.github.jan.supabase.postgrest.postgrest
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put

@Serializable
data class ProfileRow(
    val id: String,
    val username: String,
    val email: String,
    val phone: String
)

@Serializable
data class ProfileUpdate(
    val username: String,
    val email: String,
    val phone: String
)

@Serializable
data class AvailabilityResponse(
    val username_taken: Boolean,
    val email_taken: Boolean,
    val phone_taken: Boolean
)

class SupabaseAuthRepository(
    private val userRepository: UserRepository
) {
    private val client = SupabaseClientProvider.client

    // Sign up: creates Supabase Auth user, inserts profile row, mirrors into Room
    suspend fun signUp(
        username: String,
        email: String,
        phone: String,
        password: String
    ): Result<User> {
        return try {
            val availability = client.postgrest.rpc(
                function = "check_availability",
                parameters = buildJsonObject {
                    put("username_val", username)
                    put("email_val", email)
                    put("phone_val", phone)
                }
            ).decodeSingle<AvailabilityResponse>()

            if (availability.username_taken) {
                return Result.failure(Exception("CONFLICT_USERNAME"))
            }
            if (availability.email_taken) {
                return Result.failure(Exception("CONFLICT_EMAIL"))
            }
            if (availability.phone_taken) {
                return Result.failure(Exception("CONFLICT_PHONE"))
            }

            val signUpResult = client.auth.signUpWith(Email) {
                this.email = email
                this.password = password
            }

            // Supabase returns an empty identities list (no exception) when the email is already registered, to prevent email enumeration.
            if (signUpResult?.identities?.isEmpty() == true) {
                return Result.failure(Exception("CONFLICT_EMAIL"))
            }

            val uid = client.auth.currentUserOrNull()?.id
            if (uid == null) {
                // No session but identities were present -> likely "Confirm email" is enabled and a confirmation email was just sent.
                return Result.failure(Exception("EMAIL_CONFIRMATION_REQUIRED"))
            }

            client.postgrest.from("profiles").insert(
                ProfileRow(id = uid, username = username, email = email, phone = phone)
            )

            val localUser = User(
                supabaseUid = uid,
                username = username,
                email = email,
                phone = phone,
                passwordHash = ""
            )
            val localId = userRepository.insertUser(localUser)
            Result.success(localUser.copy(id = localId.toInt()))
        } catch (e: Exception) {
            Result.failure(mapSignUpException(e))
        }
    }

    // Map known Supabase/Postgres sign-up exceptions to user-friendly error codes
    private fun mapSignUpException(error: Exception): Exception {
        val message = error.message.orEmpty()
        val normalized = message.lowercase()
        val hasUniqueViolation = normalized.contains("duplicate key value violates unique constraint") ||
                normalized.contains("unique constraint") ||
                normalized.contains("23505")

        return when {
            // Supabase Auth duplicate-email response
            normalized.contains("user already registered") ->
                Exception("CONFLICT_EMAIL")

            // Postgres unique-constraint errors from profiles insert
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

    // Login: authenticates with Supabase, syncs profile into Room, returns local User
    suspend fun login(
        email: String,
        password: String
    ): Result<User> {
        return try {
            client.auth.signInWith(Email) {
                this.email = email
                this.password = password
            }
            val uid = client.auth.currentUserOrNull()?.id
                ?: return Result.failure(Exception("Login failed: no session"))

            // Pull latest profile from Supabase (source of truth for profile fields)
            val remoteProfile = client.postgrest.from("profiles")
                .select { filter { eq("id", uid) } }
                .decodeSingle<ProfileRow>()

            // Upsert into local Room
            var localUser = userRepository.getUserBySupabaseUid(uid)
            if (localUser == null) {
                val newUser = User(
                    supabaseUid = uid,
                    username = remoteProfile.username,
                    email = remoteProfile.email,
                    phone = remoteProfile.phone,
                    passwordHash = ""
                )
                val localId = userRepository.insertUser(newUser)
                localUser = newUser.copy(id = localId.toInt())
            } else {
                val updated = localUser.copy(
                    username = remoteProfile.username,
                    email = remoteProfile.email,
                    phone = remoteProfile.phone
                )
                userRepository.updateUser(updated)
                localUser = updated
            }
            Result.success(localUser)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // Resolve a phone number login to its registered email via Supabase
    suspend fun getEmailByPhone(phone: String): String? {
        return try {
            client.postgrest.rpc(
                function = "get_email_by_phone",
                parameters = buildJsonObject {
                    put("phone_val", phone)
                }
            ).decodeAs<String?>()
        } catch (e: Exception) {
            null
        }
    }

    // Update profile fields in the Supabase 'profiles' table
    suspend fun updateProfile(
        uid: String,
        username: String,
        email: String,
        phone: String
    ): Result<Unit> {
        return try {
            val availability = client.postgrest.rpc(
                function = "check_availability",
                parameters = buildJsonObject {
                    put("username_val", username)
                    put("email_val", email)
                    put("phone_val", phone)
                    put("exclude_id", uid)
                }
            ).decodeSingle<AvailabilityResponse>()

            if (availability.username_taken) {
                return Result.failure(Exception("CONFLICT_USERNAME"))
            }
            if (availability.email_taken) {
                return Result.failure(Exception("CONFLICT_EMAIL"))
            }
            if (availability.phone_taken) {
                return Result.failure(Exception("CONFLICT_PHONE"))
            }

            client.postgrest.from("profiles").update(
                ProfileUpdate(
                    username = username,
                    email = email,
                    phone = phone
                )
            ) {
                filter { eq("id", uid) }
            }
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(mapSignUpException(e))
        }
    }

    // Change password: re-authenticate with the current password first (to verify it's correct), then update to the new one.
    suspend fun changePassword(
        email: String,
        currentPassword: String,
        newPassword: String
    ): Result<Unit> {
        return try {
            // Authenticate with the current password, check if it's correct.
            client.auth.signInWith(Email) {
                this.email = email
                this.password = currentPassword
            }
            client.auth.updateUser {
                password = newPassword
            }
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // Send a password reset email via Supabase Auth
    suspend fun sendPasswordReset(email: String): Result<Unit> {
        return try {
            client.auth.resetPasswordForEmail(email)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // Logout user
    suspend fun logout() {
        client.auth.signOut()
    }
}