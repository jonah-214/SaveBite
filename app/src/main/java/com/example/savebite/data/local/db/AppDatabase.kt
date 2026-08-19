package com.example.savebite.data.local.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.savebite.data.local.dao.InventoryDao
import com.example.savebite.data.local.dao.ShoppingDao
import com.example.savebite.data.local.dao.StorageDao
import com.example.savebite.data.local.dao.UserDao
import com.example.savebite.data.local.dao.WastedItemDao
import com.example.savebite.model.Inventory
import com.example.savebite.model.ShoppingItem
import com.example.savebite.model.Storage
import com.example.savebite.model.User
import com.example.savebite.model.WastedItem
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(
    entities = [Inventory::class, Storage::class, User::class, ShoppingItem::class, WastedItem::class],
    version = 6,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun inventoryDao(): InventoryDao
    abstract fun storageDao(): StorageDao
    abstract fun userDao(): UserDao
    abstract fun shoppingDao(): ShoppingDao

    abstract fun wastedItemDao(): WastedItemDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "savebite_database"
                )
                    .fallbackToDestructiveMigration()
                    .addCallback(object : RoomDatabase.Callback() {
                        override fun onCreate(db: SupportSQLiteDatabase) {
                            super.onCreate(db)
                            CoroutineScope(Dispatchers.IO).launch {
                                getDatabase(context).storageDao().apply {
                                    insertStorage(Storage("Pantry"))
                                    insertStorage(Storage("Refrigerator"))
                                    insertStorage(Storage("Freezer"))
                                }
                            }
                        }
                    })
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}