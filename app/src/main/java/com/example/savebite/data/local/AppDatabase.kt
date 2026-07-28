package com.example.savebite.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.savebite.data.local.dao.UserDao
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
            if (INSTANCE == null) {
                INSTANCE = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "savebite_database"
                ).build()
            }
            return INSTANCE!!
        }
    }
}