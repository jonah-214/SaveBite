package com.example.savebite.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

import com.example.savebite.data.local.dao.InventoryDao
import com.example.savebite.data.local.dao.StorageDao
import com.example.savebite.data.local.dao.UserDao
import com.example.savebite.model.Inventory
import com.example.savebite.model.Storage
import com.example.savebite.model.User

// Room database
@Database(entities = [User::class, Inventory::class, Storage::class], version = 2)
abstract class AppDatabase : RoomDatabase() {
    abstract fun userDao(): UserDao
    abstract fun inventoryDao(): InventoryDao
    abstract fun storageDao(): StorageDao

    // Singleton instance of the database
    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            if (INSTANCE == null) {
                INSTANCE = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "savebite_database"
                )
                    .fallbackToDestructiveMigration()
                    .build()
            }
            return INSTANCE!!
        }
    }
}
