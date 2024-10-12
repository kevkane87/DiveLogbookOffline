package com.example.divelogbookoffline

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(entities = [Dive::class], version = 1)
abstract class DiveDatabase : RoomDatabase() {

    abstract val diveDao: DiveDAO

    companion object {
        // Singleton prevents multiple instances of database opening at the
        // same time.
        @Volatile
        private var INSTANCE: DiveDatabase? = null

        fun getDatabase(context: Context): DiveDatabase {
            // if the INSTANCE is not null, then return it,
            // if it is, then create the database
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    DiveDatabase::class.java,
                    "dive_database"
                ).build()
                INSTANCE = instance
                // return instance
                instance
            }
        }
    }
}