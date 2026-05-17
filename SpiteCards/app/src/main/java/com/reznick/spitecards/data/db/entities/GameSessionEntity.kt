package com.reznick.spitecards.data.db.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "game_sessions")
data class GameSessionEntity(
    @PrimaryKey val id: String,
    val gameType: String,
    val variant: String,
    val playersJson: String,
    val scoresJson: String,
    val winnerSeats: String,
    val playedAt: Long,
    val durationSeconds: Long
)
