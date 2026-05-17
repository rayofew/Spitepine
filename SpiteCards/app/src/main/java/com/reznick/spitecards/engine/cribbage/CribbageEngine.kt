package com.reznick.spitecards.engine.cribbage

import com.reznick.spitecards.data.model.*
import com.reznick.spitecards.engine.*

private const val PHASE_DISCARD_TO_CRIB = "DISCARD_TO_CRIB"
private const val PHASE_FLIP_STARTER = "FLIP_STARTER"
private const val PHASE_PEGGING = "PEGGING"
private const val PHASE_COUNTING = "COUNTING"

class CribbageEngine : GameEngine {

    override fun initialState(players: List<Player>, config: GameConfig): GameState {
        val activePlayers = players.filter { !it.isTableOnly }
        require(activePlayers.size in 2..3) { "Cribbage requires 2–3 players." }
        val deck = STANDARD_DECK.shuffled().toMutableList()
        val dealCount = if (activePlayers.size == 2) 6 else 5
        val hands = activePlayers.associate { player ->
            player.seat to deck.take(dealCount).also { repeat(dealCount) { deck.removeFirst() } }
        }
        val target = if (config.shortGame) 61 else 121
        return GameState(
            gameType = GameType.CRIBBAGE,
            variant = if (config.shortGame) "short" else "standard",
            phase = PHASE_DISCARD_TO_CRIB,
            deck = deck,
            hands = hands,
            tableCards = emptyList(),
            scores = activePlayers.associate { it.seat to 0 },
            currentTurn = activePlayers.first().seat,
            gameData = mapOf(
                "dealer" to activePlayers.first().seat.toString(),
                "crib" to "",
                "starter" to "",
                "pegging_count" to "0",
                "target" to target.toString()
            )
        )
    }

    override fun applyAction(state: GameState, action: GameAction, strictness: RulesStrictness): ActionResult {
        return when (state.phase) {
            PHASE_DISCARD_TO_CRIB -> handleDiscardToCrib(state, action)
            PHASE_FLIP_STARTER -> handleFlipStarter(state, action)
            PHASE_PEGGING -> handlePegging(state, action, strictness)
            PHASE_COUNTING -> handleCounting(state, action)
            else -> ActionResult.Illegal("Unknown phase: ${state.phase}")
        }
    }

    private fun handleDiscardToCrib(state: GameState, action: GameAction): ActionResult {
        if (action !is GameAction.PlayCard) return ActionResult.Illegal("Discard a card to the crib.")
        val hand = state.hands[action.playerSeat] ?: return ActionResult.Illegal("No hand.")
        val card = hand.find { it.id == action.cardId } ?: return ActionResult.Illegal("Card not in hand.")
        val newHand = hand - card
        val newHands = state.hands.toMutableMap().also { it[action.playerSeat] = newHand }
        val crib = (state.gameData["crib"]?.split(",")?.filter { it.isNotEmpty() } ?: emptyList()) + card.id
        val newData = state.gameData.toMutableMap().also { it["crib"] = crib.joinToString(",") }

        val allDiscarded = newHands.values.all { it.size == 4 }
        val nextPhase = if (allDiscarded) PHASE_FLIP_STARTER else PHASE_DISCARD_TO_CRIB
        val dealer = state.gameData["dealer"]?.toIntOrNull() ?: state.currentTurn
        val nextTurn = if (allDiscarded) dealer else state.currentTurn

        return ActionResult.Success(state.copy(hands = newHands, gameData = newData, phase = nextPhase, currentTurn = nextTurn))
    }

    private fun handleFlipStarter(state: GameState, action: GameAction): ActionResult {
        if (action !is GameAction.FlipStarterCard) return ActionResult.Illegal("Flip the starter card.")
        val starter = state.deck.first()
        val newDeck = state.deck.drop(1)
        val newData = state.gameData.toMutableMap().also { it["starter"] = starter.id; it["pegging_count"] = "0" }
        // His heels: if starter is a Jack, dealer scores 2
        val dealer = state.gameData["dealer"]?.toIntOrNull() ?: state.currentTurn
        val newScores = state.scores.toMutableMap()
        if (starter.rank == Rank.JACK) newScores[dealer] = (newScores[dealer] ?: 0) + 2
        val nonDealer = state.hands.keys.firstOrNull { it != dealer } ?: dealer
        return ActionResult.Success(
            state.copy(deck = newDeck, gameData = newData, scores = newScores, phase = PHASE_PEGGING, currentTurn = nonDealer)
        )
    }

    private fun pegValue(card: Card) = minOf(card.rank.pip, 10)

    private fun handlePegging(state: GameState, action: GameAction, strictness: RulesStrictness): ActionResult {
        if (action !is GameAction.PlayCard) return ActionResult.Illegal("Play a card to peg.")
        if (action.playerSeat != state.currentTurn) return ActionResult.Illegal("Not your turn.")
        val hand = state.hands[action.playerSeat] ?: return ActionResult.Illegal("No hand.")
        val card = hand.find { it.id == action.cardId } ?: return ActionResult.Illegal("Card not in hand.")
        val currentCount = state.gameData["pegging_count"]?.toIntOrNull() ?: 0
        val newCount = currentCount + pegValue(card)
        if (newCount > 31) {
            return when (strictness) {
                RulesStrictness.STRICT -> ActionResult.Illegal("Would exceed 31 ($newCount). Say 'Go'.")
                RulesStrictness.SOFT -> ActionResult.Warning(state, "That would exceed 31 — say 'Go' instead?")
                RulesStrictness.OFF -> ActionResult.Success(state) // let it slide
            }
        }
        val newHand = hand - card
        val newHands = state.hands.toMutableMap().also { it[action.playerSeat] = newHand }
        val newTable = state.tableCards + card
        val newData = state.gameData.toMutableMap().also { it["pegging_count"] = newCount.toString() }
        var points = 0
        if (newCount == 15 || newCount == 31) points += 2
        val newScores = state.scores.toMutableMap().also { it[action.playerSeat] = (it[action.playerSeat] ?: 0) + points }

        val target = state.gameData["target"]?.toIntOrNull() ?: 121
        if ((newScores[action.playerSeat] ?: 0) >= target) {
            return ActionResult.Success(state.copy(hands = newHands, tableCards = newTable, gameData = newData, scores = newScores, isOver = true, winnerSeats = listOf(action.playerSeat)))
        }

        val allHandsEmpty = newHands.values.all { it.isEmpty() }
        val nextPhase = if (allHandsEmpty) PHASE_COUNTING else PHASE_PEGGING
        val seats = state.hands.keys.toList().sorted()
        val nextSeat = seats.firstOrNull { it > action.playerSeat } ?: seats.first()
        return ActionResult.Success(state.copy(hands = newHands, tableCards = newTable, gameData = newData, scores = newScores, phase = nextPhase, currentTurn = nextSeat))
    }

    private fun handleCounting(state: GameState, action: GameAction): ActionResult {
        // Counting phase: app auto-scores (or soft-mode lets players input)
        // For now: transition to a new round or end game
        return ActionResult.Success(state.copy(isOver = true, winnerSeats = listOf(state.scores.maxBy { it.value }.key)))
    }

    override fun isGameOver(state: GameState) = state.isOver
    override fun scores(state: GameState) = state.scores
    override fun winnerSeats(state: GameState) = state.winnerSeats
}
