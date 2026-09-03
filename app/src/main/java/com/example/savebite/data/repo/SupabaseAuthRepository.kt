package com.example.savebite.data.repo

import android.util.Log
import com.example.savebite.data.remote.SupabaseClientProvider
import com.example.savebite.model.User
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.auth.providers.builtin.Email
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.storage.storage
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put

@Serializable
data class ProfileRow(
    val id: String,
    val username: String,
    val email: String,
    val phone: String,
    val avatar_url: String? = null
)

@Serializable
data class ProfileUpdate(
    val username: String? = null,
    val email: String? = null,
    val phone: String? = null,
    val avatar_url: String? = null
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

    companion object {
        private const val TAG = "SupabaseAuthRepository"
    }

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
                data = buildJsonObject {
                    put("username", username)
                    put("phone", phone)
                }
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

            val localUser = User(
                supabaseUid = uid,
                username = username,
                email = email,
                phone = phone,
            )
            val localId = userRepository.insertUser(localUser)
            Result.success(localUser.copy(id = localId.toInt()))
        } catch (e: Exception) {
            Log.e(TAG, "signUp failed for email=$email", e)
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

            // Inventory/shopping/report data is synced independently by each
            // feature's own ViewModel when its screen loads (see InventoryViewModel,
            // ShoppingViewModel, ReportViewModel) — no post-login sync needed here.

            Result.success(localUser)
        } catch (e: Exception) {
            Log.e(TAG, "login failed for email=$email", e)
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
            Log.e(TAG, "getEmailByPhone failed for phone=$phone", e)
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
            Log.e(TAG, "updateProfile failed for uid=$uid", e)
            Result.failure(mapSignUpException(e))
        }
    }

    // Upload a new avatar, replacing any existing one, then update the profile row (Edit Profile)
    suspend fun uploadAvatar(
        uid: String,
        imageBytes: ByteArray
    ): Result<String> {
        return try {
            val path = "$uid/profile.jpg"
            client.storage.from("avatars").upload(
                path = path,
                data = imageBytes
            ) {
                upsert = true // overwrite in place, so no separate delete-then-upload needed
            }

            val publicUrl = client.storage.from("avatars").publicUrl(path)
            // Cache-bust so the new image isn't served from a stale cache under the same URL
            val bustedUrl = "$publicUrl?t=${System.currentTimeMillis()}"

            client.postgrest.from("profiles").update(
                ProfileUpdate(avatar_url = bustedUrl)
            ) {
                filter { eq("id", uid) }
            }

            Result.success(bustedUrl)
        } catch (e: Exception) {
            Log.e(TAG, "uploadAvatar failed for uid=$uid", e)
            Result.failure(e)
        }
    }

    // Remove the current avatar entirely
    suspend fun removeAvatar(uid: String): Result<Unit> {
        return try {
            client.storage.from("avatars").delete("$uid/profile.jpg")

            client.postgrest.from("profiles").update(
                ProfileUpdate(avatar_url = null)
            ) {
                filter { eq("id", uid) }
            }

            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "removeAvatar failed for uid=$uid", e)
            Result.failure(e)
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
            // Never log password values, only the outcome/context.
            Log.e(TAG, "changePassword failed for email=$email", e)
            Result.failure(e)
        }
    }

    // Send a password reset email via Supabase Auth
    suspend fun sendPasswordReset(email: String): Result<Unit> {
        return try {
            client.auth.resetPasswordForEmail(email)
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "sendPasswordReset failed for email=$email", e)
            Result.failure(e)
        }
    }

    // Logout user
    suspend fun logout() {
        client.auth.signOut()
    }
}