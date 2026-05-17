package com.reznick.spitecards.engine

import com.reznick.spitecards.data.model.Card
import com.reznick.spitecards.data.model.GameType
import kotlinx.serialization.Serializable

@Serializable
data class GameState(
    val gameType: GameType,
    val variant: String,
    val phase: String,
    val deck: List<Card>,
    val hands: Map<Int, List<Card>>,        // seat -> hand
    val tableCards: List<Card>,
    val discardPile: List<Card> = emptyList(),
    val scores: Map<Int, Int>,              // seat -> score
    val currentTurn: Int,                   // seat index
    val roundNumber: Int = 1,
    val gameData: Map<String, String> = emptyMap(), // game-specific serialized state
    val isOver: Boolean = false,
    val winnerSeats: List<Int> = emptyList()
)
