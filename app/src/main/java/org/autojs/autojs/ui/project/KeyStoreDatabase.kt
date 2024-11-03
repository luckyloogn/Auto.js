package org.autojs.autojs.ui.project

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(entities = [KeyStore::class], version = 1)
abstract class KeyStoreDatabase : RoomDatabase() {
    abstract fun keyStoreDao(): KeyStoreDao

    companion object {
        @Volatile
        private var INSTANCE: KeyStoreDatabase? = null

        fun getDatabase(context: Context): KeyStoreDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    KeyStoreDatabase::class.java,
                    "keystore-database"
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}

