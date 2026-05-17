package com.reznick.spitecards.engine.cassino

import com.reznick.spitecards.data.model.*
import com.reznick.spitecards.engine.*

class CassinoEngine : GameEngine {

    override fun initialState(players: List<Player>, config: GameConfig): GameState {
        val deck = STANDARD_DECK.shuffled().toMutableList()
        val hands = mutableMapOf<Int, List<Card>>()
        players.filter { !it.isTableOnly }.forEach { player ->
            hands[player.seat] = deck.take(4).also { repeat(4) { deck.removeFirst() } }
        }
        val tableCards = deck.take(4).also { repeat(4) { deck.removeFirst() } }
        val scores = players.associate { it.seat to 0 }
        return GameState(
            gameType = GameType.CASSINO,
            variant = config.cassinoVariant.name,
            phase = "PLAY",
            deck = deck,
            hands = hands,
            tableCards = tableCards,
            scores = scores,
            currentTurn = players.first { !it.isTableOnly }.seat
        )
    }

    override fun applyAction(state: GameState, action: GameAction, strictness: RulesStrictness): ActionResult {
        if (action.playerSeat != state.currentTurn) {
            return ActionResult.Illegal("It's not your turn.")
        }
        return when (action) {
            is GameAction.PlayCard -> handlePlayCard(state, action, strictness)
            is GameAction.EndTurn -> ActionResult.Illegal("Cassino: must play a card to end your turn.")
            else -> ActionResult.Illegal("Action not valid in Cassino.")
        }
    }

    private fun handlePlayCard(
        state: GameState,
        action: GameAction.PlayCard,
        strictness: RulesStrictness
    ): ActionResult {
        val hand = state.hands[action.playerSeat] ?: return ActionResult.Illegal("No hand for seat.")
        val played = hand.find { it.id == action.cardId }
            ?: return ActionResult.Illegal("Card not in hand.")

        val targets = action.targetCardIds.mapNotNull { id -> state.tableCards.find { it.id == id } }

        return if (targets.isEmpty()) {
            // Trail: place card on table
            val newHand = hand - played
            val newTable = state.tableCards + played
            val newHands = state.hands.toMutableMap().also { it[action.playerSeat] = newHand }
            ActionResult.Success(advanceTurn(state.copy(hands = newHands, tableCards = newTable)))
        } else {
            // Capture attempt
            val captureValue = captureValue(played, state.variant)
            val targetSum = targets.sumOf { captureValue(it, state.variant) }
            if (captureValue != targetSum) {
                return when (strictness) {
                    RulesStrictness.STRICT -> ActionResult.Illegal("Capture values don't match ($captureValue vs $targetSum).")
                    RulesStrictness.SOFT -> ActionResult.Warning(executeCapture(state, action.playerSeat, played, targets), "That capture looks off — values don't match.")
                    RulesStrictness.OFF -> ActionResult.Success(executeCapture(state, action.playerSeat, played, targets))
                }
            }
            ActionResult.Success(executeCapture(state, action.playerSeat, played, targets))
        }
    }

    private fun captureValue(card: Card, variant: String): Int = when {
        variant == CassinoVariant.ROYAL.name && card.rank == Rank.JACK -> 11
        variant == CassinoVariant.ROYAL.name && card.rank == Rank.QUEEN -> 12
        variant == CassinoVariant.ROYAL.name && card.rank == Rank.KING -> 13
        card.rank.pip > 10 -> 0 // face cards can't capture in standard
        else -> card.rank.pip
    }

    private fun executeCapture(
        state: GameState,
        seat: Int,
        played: Card,
        targets: List<Card>
    ): GameState {
        val captured = targets + played
        val newTable = state.tableCards - targets.toSet()
        val newHand = (state.hands[seat] ?: emptyList()) - played
        val newHands = state.hands.toMutableMap().also { it[seat] = newHand }
        val isSweep = newTable.isEmpty()
        val bonusPoints = if (isSweep) 1 else 0
        val newScores = state.scores.toMutableMap()
        newScores[seat] = (newScores[seat] ?: 0) + bonusPoints
        val newData = state.gameData.toMutableMap()
        val capturedKey = "captured_$seat"
        val existing = newData[capturedKey]?.split(",")?.filter { it.isNotEmpty() } ?: emptyList()
        newData[capturedKey] = (existing + captured.map { it.id }).joinToString(",")
        return advanceTurn(state.copy(hands = newHands, tableCards = newTable, scores = newScores, gameData = newData))
    }

    private fun advanceTurn(state: GameState): GameState {
        val activeSeat = state.hands.entries
            .filter { it.value.isNotEmpty() }
            .map { it.key }
        if (activeSeat.isEmpty()) {
            // Redeal or game over
            return if (state.deck.isEmpty()) finalScoring(state) else redeal(state)
        }
        val next = activeSeat.firstOrNull { it > state.currentTurn } ?: activeSeat.first()
        return state.copy(currentTurn = next)
    }

    private fun redeal(state: GameState): GameState {
        val deck = state.deck.toMutableList()
        val newHands = state.hands.keys.associateWith {
            deck.take(4).also { repeat(4) { deck.removeFirst() } }
        }
        return state.copy(deck = deck, hands = newHands)
    }

    private fun finalScoring(state: GameState): GameState {
        val scores = state.scores.toMutableMap()
        val capturedBySeat = state.hands.keys.associateWith { seat ->
            state.gameData["captured_$seat"]?.split(",")?.filter { it.isNotEmpty() }
                ?.mapNotNull { id -> STANDARD_DECK.find { it.id == id } } ?: emptyList()
        }

        // Most cards (3 pts)
        val maxCards = capturedBySeat.maxOf { it.value.size }
        capturedBySeat.filter { it.value.size == maxCards }.forEach { scores[it.key] = (scores[it.key] ?: 0) + 3 }

        // Most spades (1 pt)
        val spadesBySeat = capturedBySeat.mapValues { it.value.count { c -> c.suit == Suit.SPADES } }
        val maxSpades = spadesBySeat.maxOf { it.value }
        spadesBySeat.filter { it.value == maxSpades }.forEach { scores[it.key] = (scores[it.key] ?: 0) + 1 }

        // Big Cassino: 10♦ (2 pts)
        capturedBySeat.forEach { (seat, cards) ->
            if (cards.any { it.rank == Rank.TEN && it.suit == Suit.DIAMONDS }) scores[seat] = (scores[seat] ?: 0) + 2
            // Little Cassino: 2♠ (1 pt)
            if (cards.any { it.rank == Rank.TWO && it.suit == Suit.SPADES }) scores[seat] = (scores[seat] ?: 0) + 1
            // Aces (1 pt each)
            scores[seat] = (scores[seat] ?: 0) + cards.count { it.rank == Rank.ACE }
        }

        // Spade variant bonus
        if (state.variant == CassinoVariant.SPADE.name) {
            capturedBySeat.forEach { (seat, cards) ->
                scores[seat] = (scores[seat] ?: 0) + cards.count { it.suit == Suit.SPADES } / 2
            }
        }

        val winner = scores.maxBy { it.value }.key
        return state.copy(scores = scores, isOver = true, winnerSeats = listOf(winner))
    }

    override fun isGameOver(state: GameState) = state.isOver
    override fun scores(state: GameState) = state.scores
    override fun winnerSeats(state: GameState) = state.winnerSeats
}
