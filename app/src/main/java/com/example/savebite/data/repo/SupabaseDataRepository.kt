package com.example.savebite.data.repo

import com.example.savebite.data.remote.SupabaseClientProvider
import com.example.savebite.data.remote.SupabaseInventory
import com.example.savebite.data.remote.SupabaseReportItem
import com.example.savebite.data.remote.SupabaseShoppingItem
import com.example.savebite.data.remote.SupabaseStorage

import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.postgrest.postgrest

class SupabaseDataRepository {

    private val client = SupabaseClientProvider.client

    //Retrieving the active authenticated session
    private fun getCurrentUserId(): String? {
        return client.auth.currentUserOrNull()?.id
    }

    // Inventory Part
    suspend fun fetchInventoryItems(): Result<List<SupabaseInventory>> = runCatching {
        val uid = getCurrentUserId() ?: throw Exception("User not logged in")
        client.postgrest.from("inventory_table")
            .select { filter { eq("user_id", uid) } }
            .decodeList<SupabaseInventory>()
    }

    suspend fun upsertInventoryItem(item: SupabaseInventory): Result<Unit> = runCatching {
        val uid = getCurrentUserId() ?: throw Exception("User not logged in")
        client.postgrest.from("inventory_table").upsert(item.copy(userId = uid))
    }

    suspend fun deleteInventoryItem(id: String): Result<Unit> = runCatching {
        val uid = getCurrentUserId() ?: throw Exception("User not logged in")
        client.postgrest.from("inventory_table").delete {
            filter {
                eq("id", id)
                eq("user_id", uid)
            }
        }
    }

    // Shopping Part
    suspend fun fetchShoppingItems(): Result<List<SupabaseShoppingItem>> = runCatching {
        val uid = getCurrentUserId() ?: throw Exception("User not logged in")
        client.postgrest.from("shopping_items")
            .select { filter { eq("user_id", uid) } }
            .decodeList<SupabaseShoppingItem>()
    }

    suspend fun upsertShoppingItem(item: SupabaseShoppingItem): Result<Unit> = runCatching {
        val uid = getCurrentUserId() ?: throw Exception("User not logged in")
        client.postgrest.from("shopping_items").upsert(item.copy(userId = uid))
    }

    suspend fun deleteShoppingItem(id: String): Result<Unit> = runCatching {
        val uid = getCurrentUserId() ?: throw Exception("User not logged in")
        client.postgrest.from("shopping_items").delete {
            filter {
                eq("id", id)
                eq("user_id", uid)
            }
        }
    }

    // Report Part
    suspend fun fetchReportItems(): Result<List<SupabaseReportItem>> = runCatching {
        val uid = getCurrentUserId() ?: throw Exception("User not logged in")
        client.postgrest.from("report_items")
            .select { filter { eq("user_id", uid) } }
            .decodeList<SupabaseReportItem>()
    }

    suspend fun insertReportItem(item: SupabaseReportItem): Result<Unit> = runCatching {
        val uid = getCurrentUserId() ?: throw Exception("User not logged in")
        client.postgrest.from("report_items").insert(item.copy(userId = uid))
    }

    // Storage Part
    suspend fun fetchStorageList(): Result<List<SupabaseStorage>> = runCatching {
        val uid = getCurrentUserId() ?: throw Exception("User not logged in")
        client.postgrest.from("storage_table")
            .select { filter { eq("user_id", uid) } }
            .decodeList<SupabaseStorage>()
    }

    suspend fun upsertStorage(storage: SupabaseStorage): Result<Unit> = runCatching {
        val uid = getCurrentUserId() ?: throw Exception("User not logged in")
        client.postgrest.from("storage_table").upsert(storage.copy(userId = uid))
    }

    suspend fun deleteStorage(name: String): Result<Unit> = runCatching {
        val uid = getCurrentUserId() ?: throw Exception("User not logged in")
        client.postgrest.from("storage_table").delete {
            filter {
                eq("name", name)
                eq("user_id", uid)
            }
        }
    }
}