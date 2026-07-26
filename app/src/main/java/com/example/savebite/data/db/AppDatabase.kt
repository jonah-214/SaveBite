package com.example.savebite.data.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.savebite.data.dao.UserDao
import com.example.savebite.model.User

// Room database
@Database(entities = [User::class], version = 1)
abstract class AppDatabase : RoomDatabase() {

    // User DAO
    abstract fun userDao(): UserDao

    // Singleton instance of the database
    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "savebite_database"
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}