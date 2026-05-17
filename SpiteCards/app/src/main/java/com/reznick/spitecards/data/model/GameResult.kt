package com.reznick.spitecards.data.model

data class GameResult(
    val sessionId: String,
    val gameType: GameType,
    val variant: String,
    val config: GameConfig,
    val players: List<Player>,
    val scores: Map<Int, Int>,
    val winnerSeats: List<Int>,
    val playedAt: Long,
    val durationSeconds: Long
)
