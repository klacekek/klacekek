package com.example.heatertracker.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters

@Database(entities = [TournamentEntry::class], version = 1, exportSchema = false)
@TypeConverters(Converters::class)
abstract class HeaterDatabase : RoomDatabase() {

    abstract fun tournamentDao(): TournamentDao

    companion object {
        @Volatile
        private var INSTANCE: HeaterDatabase? = null

        fun getDatabase(context: Context): HeaterDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    HeaterDatabase::class.java,
                    "heater_tracker.db"
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}
