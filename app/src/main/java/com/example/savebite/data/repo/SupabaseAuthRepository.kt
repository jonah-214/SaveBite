package com.example.savebite.data.repo

import com.example.savebite.data.remote.SupabaseCilentProvider
import com.example.savebite.model.User
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.auth.providers.builtin.Email
import io.github.jan.supabase.postgrest.postgrest
import kotlinx.serialization.Serializable

@Serializable
data class ProfileRow(
    val id: String,
    val username: String,
    val email: String,
    val phone: String
)

class SupabaseAuthRepository(
    private val userRepository: UserRepository
) {
    private val client = SupabaseCilentProvider.cilent

    // Sign up: creates Supabase Auth user, inserts profile row, mirrors into Room
    suspend fun signUp(
        username: String,
        email: String,
        phone: String,
        password: String
    ): Result<User> {
        return try {
            client.auth.signUpWith(Email) {
                this.email = email
                this.password = password
            }
            val uid = client.auth.currentUserOrNull()?.id
                ?: return Result.failure(Exception("Signup failed: no session"))

            // Insert profile row in Supabase
            client.postgrest["profiles"].insert(
                ProfileRow(
                    id = uid,
                    username = username,
                    email = email,
                    phone = phone
                )
            )

            // Mirror into local Room (passwordHash left blank/unused now)
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
            Result.failure(e)
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
            val remoteProfile = client.postgrest["profiles"]
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

    // Logout user
    suspend fun logout() {
        client.auth.signOut()
    }
}