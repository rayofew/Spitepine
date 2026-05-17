package com.reznick.spitecards.engine

import com.reznick.spitecards.data.model.GameConfig
import com.reznick.spitecards.data.model.Player
import com.reznick.spitecards.data.model.RulesStrictness

sealed class ActionResult {
    data class Success(val newState: GameState) : ActionResult()
    data class Illegal(val reason: String) : ActionResult()
    data class Warning(val newState: GameState, val message: String) : ActionResult()
}

interface GameEngine {
    fun initialState(players: List<Player>, config: GameConfig): GameState
    fun applyAction(state: GameState, action: GameAction, strictness: RulesStrictness): ActionResult
    fun isGameOver(state: GameState): Boolean
    fun scores(state: GameState): Map<Int, Int>
    fun winnerSeats(state: GameState): List<Int>
}

fun engineFor(config: GameConfig): GameEngine = when (config.gameType) {
    com.reznick.spitecards.data.model.GameType.CASSINO -> com.reznick.spitecards.engine.cassino.CassinoEngine()
    com.reznick.spitecards.data.model.GameType.CRIBBAGE -> com.reznick.spitecards.engine.cribbage.CribbageEngine()
    com.reznick.spitecards.data.model.GameType.GARBAGE -> com.reznick.spitecards.engine.garbage.GarbageEngine()
    com.reznick.spitecards.data.model.GameType.FREEFORM -> com.reznick.spitecards.engine.freeform.FreeformEngine()
}
