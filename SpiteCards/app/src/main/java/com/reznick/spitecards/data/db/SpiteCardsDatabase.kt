package com.reznick.spitecards.data.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.reznick.spitecards.data.db.entities.GameSessionEntity

@Database(entities = [GameSessionEntity::class], version = 1, exportSchema = false)
abstract class SpiteCardsDatabase : RoomDatabase() {
    abstract fun gameSessionDao(): GameSessionDao

    companion object {
        @Volatile private var INSTANCE: SpiteCardsDatabase? = null

        fun get(context: Context): SpiteCardsDatabase = INSTANCE ?: synchronized(this) {
            INSTANCE ?: Room.databaseBuilder(
                context.applicationContext,
                SpiteCardsDatabase::class.java,
                "spitecards.db"
            ).build().also { INSTANCE = it }
        }
    }
}
