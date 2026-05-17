package com.reznick.spitecards.data.repository

import com.reznick.spitecards.data.db.GameSessionDao
import com.reznick.spitecards.data.db.entities.GameSessionEntity
import com.reznick.spitecards.data.model.GameResult
import kotlinx.coroutines.flow.Flow
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

class GameHistoryRepository(private val dao: GameSessionDao) {

    fun observeHistory(): Flow<List<GameSessionEntity>> = dao.observeAll()

    suspend fun save(result: GameResult) {
        dao.insert(
            GameSessionEntity(
                id = result.sessionId,
                gameType = result.gameType.name,
                variant = result.variant,
                playersJson = Json.encodeToString(result.players),
                scoresJson = Json.encodeToString(result.scores),
                winnerSeats = result.winnerSeats.joinToString(","),
                playedAt = result.playedAt,
                durationSeconds = result.durationSeconds
            )
        )
    }

    suspend fun delete(id: String) = dao.delete(id)
}
