package com.example.savebite.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.savebite.model.Storage
import kotlinx.coroutines.flow.Flow

@Dao
interface StorageDao {

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertStorage(storage: Storage)

    @Query("SELECT name FROM storage_table")
    fun getAllStorageNames(): Flow<List<String>>
}