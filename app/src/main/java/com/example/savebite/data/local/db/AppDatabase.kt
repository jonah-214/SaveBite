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
    entities = [
        Inventory::class,
        Storage::class,
        User::class,
        ShoppingItem::class,
        WastedItem::class
    ],
    version = 7, // 提升版本号至 7 以覆盖你之前的 schema 修改
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
                            // 修复点：直接使用已构建好的 instance 句柄，避免再次调用 getDatabase 导致死锁
                            INSTANCE?.let { database ->
                                CoroutineScope(Dispatchers.IO).launch {
                                    database.storageDao().apply {
                                        insertStorage(Storage("Pantry"))
                                        insertStorage(Storage("Refrigerator"))
                                        insertStorage(Storage("Freezer"))
                                    }
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