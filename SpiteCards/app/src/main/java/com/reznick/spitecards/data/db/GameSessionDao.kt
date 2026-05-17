package com.reznick.spitecards.data.db

import androidx.room.*
import com.reznick.spitecards.data.db.entities.GameSessionEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface GameSessionDao {
    @Query("SELECT * FROM game_sessions ORDER BY playedAt DESC")
    fun observeAll(): Flow<List<GameSessionEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(session: GameSessionEntity)

    @Query("DELETE FROM game_sessions WHERE id = :id")
    suspend fun delete(id: String)
}
