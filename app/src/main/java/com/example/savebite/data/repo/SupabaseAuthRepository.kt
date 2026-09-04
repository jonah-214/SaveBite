package com.example.savebite.data.repo

import android.util.Log
import com.example.savebite.data.remote.SupabaseClientProvider
import com.example.savebite.model.User
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.auth.providers.builtin.Email
import io.github.jan.supabase.postgrest.postgrest
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put

// Data transfer object representing a row in the Supabase 'profiles' table
@Serializable
data class ProfileRow(
    val id: String,
    val username: String,
    val email: String,
    val phone: String,
    val avatar_url: String? = null,
    val is_active: Boolean = true
)


// Result of a successful login attempt, including the user entity and a reactivation flag
data class LoginResult(
    val user: User,
    val wasReactivated: Boolean
)


// Response from the Supabase RPC checking for credential availability.
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

    /*
    * Signs up a new user
    * 1. Checks for credential availability via Supabase RPC
    * 2. Creates the user in Supabase Auth
    * 3. Mirrors the user profile into the local Room database
    */
    suspend fun signUp(
        username: String,
        email: String,
        phone: String,
        password: String
    ): Result<User> {
        return try {
            // Check availability of credentials using a custom database function (RPC)
            val availability = client.postgrest.rpc(
                function = "check_availability",
                parameters = buildJsonObject {
                    put("username_val", username)
                    put("email_val", email)
                    put("phone_val", phone)
                }
            ).decodeSingle<AvailabilityResponse>()

            // Return early if any field is already taken
            if (availability.username_taken) {
                return Result.failure(Exception("CONFLICT_USERNAME"))
            }
            if (availability.email_taken) {
                return Result.failure(Exception("CONFLICT_EMAIL"))
            }
            if (availability.phone_taken) {
                return Result.failure(Exception("CONFLICT_PHONE"))
            }

            // Create the user in Supabase Auth
            val signUpResult = client.auth.signUpWith(Email) {
                this.email = email
                this.password = password
                data = buildJsonObject {
                    put("username", username)
                    put("phone", phone)
                }
            }

            // Handle Supabase email enumeration prevention
            if (signUpResult?.identities?.isEmpty() == true) {
                return Result.failure(Exception("CONFLICT_EMAIL"))
            }

            val uid = client.auth.currentUserOrNull()?.id
            if (uid == null) {
                // If email confirmation is required, the user won't have a session yet
                return Result.failure(Exception("EMAIL_CONFIRMATION_REQUIRED"))
            }

            // Mirror the new user into the local Room database for offline access
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
            Result.failure(mapSupabaseConflictException(e))
        }
    }

    /*
     * Authenticates a user with email/password.
     * 1. Performs remote authentication via Supabase.
     * 2. Fetches the latest profile data.
     * 3. Syncs the profile to the local Room database.
     */
    suspend fun login(
        email: String,
        password: String
    ): Result<LoginResult> {
        return try {
            // Authenticate with Supabase Auth
            val reauth = reauthenticate(email, password)
            if (reauth.isFailure) {
                return Result.failure(reauth.exceptionOrNull() ?: Exception("Login failed"))
            }
            val uid = client.auth.currentUserOrNull()?.id
                ?: return Result.failure(Exception("Login failed: no session"))

            // Pull the latest profile data from Supabase
            val remoteProfile = client.postgrest.from("profiles")
                .select { filter { eq("id", uid) } }
                .decodeSingle<ProfileRow>()

            // Sync this data into the local Room database (Offline-First)
            var localUser = userRepository.getUserBySupabaseUid(uid)
            if (localUser == null) {
                val newUser = User(
                    supabaseUid = uid,
                    username = remoteProfile.username,
                    email = remoteProfile.email,
                    phone = remoteProfile.phone,
                    avatarUrl = remoteProfile.avatar_url
                )
                val localId = userRepository.insertUser(newUser)
                localUser = newUser.copy(id = localId.toInt())
            } else {
                val updated = localUser.copy(
                    username = remoteProfile.username,
                    email = remoteProfile.email,
                    phone = remoteProfile.phone,
                    avatarUrl = remoteProfile.avatar_url
                )
                userRepository.updateUser(updated)
                localUser = updated
            }

            // Return success with reactivation flag if the account was inactive
            Result.success(LoginResult(localUser, wasReactivated = !remoteProfile.is_active))
        } catch (e: Exception) {
            Log.e(TAG, "login failed for email=$email", e)
            Result.failure(e)
        }
    }

    // Performs a standard sign-in request. Used for both login and verification
    // before sensitive operations like password changes.
    suspend fun reauthenticate(email: String, password: String): Result<Unit> {
        return try {
            client.auth.signInWith(Email) {
                this.email = email
                this.password = password
            }
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }


    // Resolves a phone number to its primary registered email address
    // This allows users to log in with a phone number even though Supabase uses email
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

    // Updates the user's password in Supabase
    // Verifies the current password before applying the new one
    suspend fun changePassword(
        email: String,
        currentPassword: String,
        newPassword: String
    ): Result<Unit> {
        val reauth = reauthenticate(email, currentPassword)
        if (reauth.isFailure) {
            return Result.failure(reauth.exceptionOrNull() ?: Exception("Current password is incorrect"))
        }
        return try {
            client.auth.updateUser {
                password = newPassword
            }
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "changePassword failed for email=$email", e)
            Result.failure(e)
        }
    }

    // Triggers a password reset email via Supabase Auth.
    suspend fun sendPasswordReset(email: String): Result<Unit> {
        return try {
            client.auth.resetPasswordForEmail(email)
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "sendPasswordReset failed for email=$email", e)
            Result.failure(e)
        }
    }


    // Terminates the current Supabase session.
    suspend fun logout() {
        client.auth.signOut()
    }
}