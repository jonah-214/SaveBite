package com.example.savebite.data.repo

import android.util.Log
import com.example.savebite.data.remote.SupabaseClientProvider
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.storage.storage
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put

@Serializable
data class ProfileUpdate(
    val username: String? = null,
    val email: String? = null,
    val phone: String? = null,
    val avatar_url: String? = null,
    val is_active: Boolean? = null
)

// Handles editing an already-signed-in user's profile: field updates and avatar upload/removal.
// Account creation/login/password lives in SupabaseAuthRepository — kept separate so each
// class has one job (Edit Profile vs. Authentication) instead of one repository doing both.
class ProfileRepository {
    private val client = SupabaseClientProvider.client

    companion object {
        private const val TAG = "ProfileRepository"
    }

    // Update profile fields in the Supabase 'profiles' table.
    // currentEmail is the user's email as currently stored, so we can tell whether
    // this call is actually changing it and needs to update the Auth login credential too.
    suspend fun updateProfile(
        uid: String,
        username: String,
        email: String,
        phone: String,
        currentEmail: String
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

            // If the email is changing, update the Supabase Auth login credential FIRST.
            // Supabase sends a confirmation link and the login email won't actually switch
            // until the user clicks it, but this keeps the two in sync going forward instead
            // of the profiles table silently drifting away from what the user logs in with.
            if (email != currentEmail) {
                try {
                    client.auth.updateUser { this.email = email }
                } catch (e: Exception) {
                    Log.e(TAG, "Auth email update failed for uid=$uid", e)
                    return Result.failure(Exception("AUTH_EMAIL_UPDATE_FAILED"))
                }
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
            Result.failure(mapSupabaseConflictException(e))
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

    // Flip the profiles.is_active flag — false to deactivate the account (Profile & Settings
    // > Deactivate Account), true to auto-reactivate it on a later successful login
    // (see SupabaseAuthRepository.login() / AuthViewModel.login()).
    suspend fun setAccountActive(uid: String, isActive: Boolean): Result<Unit> {
        return try {
            client.postgrest.from("profiles").update(
                ProfileUpdate(is_active = isActive)
            ) {
                filter { eq("id", uid) }
            }
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "setAccountActive($isActive) failed for uid=$uid", e)
            Result.failure(e)
        }
    }
}
