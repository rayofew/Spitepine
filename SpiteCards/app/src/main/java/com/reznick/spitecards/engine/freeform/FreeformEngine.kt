package com.reznick.spitecards.engine.freeform

import com.reznick.spitecards.data.model.*
import com.reznick.spitecards.engine.*

class FreeformEngine : GameEngine {

    override fun initialState(players: List<Player>, config: GameConfig): GameState {
        val activePlayers = players.filter { !it.isTableOnly }
        val deck = standardDeckWith(jokers = config.deckJokers, copies = config.deckCopies).shuffled().toMutableList()
        val dealCount = config.dealCount
        val hands = activePlayers.associate { player ->
            player.seat to deck.take(dealCount).also { repeat(dealCount) { deck.removeFirst() } }
        }
        return GameState(
            gameType = GameType.FREEFORM,
            variant = "freeform",
            phase = "PLAY",
            deck = deck,
            hands = hands,
            tableCards = emptyList(),
            scores = activePlayers.associate { it.seat to 0 },
            currentTurn = activePlayers.first().seat
        )
    }

    override fun applyAction(state: GameState, action: GameAction, strictness: RulesStrictness): ActionResult {
        return when (action) {
            is GameAction.PlayCard -> {
                val hand = state.hands[action.playerSeat] ?: return ActionResult.Illegal("No hand.")
                val card = hand.find { it.id == action.cardId } ?: return ActionResult.Illegal("Card not in hand.")
                val newHand = hand - card
                val newHands = state.hands.toMutableMap().also { it[action.playerSeat] = newHand }
                val newTable = state.tableCards + card
                ActionResult.Success(state.copy(hands = newHands, tableCards = newTable))
            }
            is GameAction.DrawCard -> {
                val drawn = if (action.fromDiscard) {
                    state.discardPile.lastOrNull() ?: return ActionResult.Illegal("Discard pile empty.")
                } else {
                    state.deck.firstOrNull() ?: return ActionResult.Illegal("Deck empty.")
                }
                val hand = (state.hands[action.playerSeat] ?: emptyList()) + drawn
                val newHands = state.hands.toMutableMap().also { it[action.playerSeat] = hand }
                val newDeck = if (action.fromDiscard) state.deck else state.deck.drop(1)
                val newDiscard = if (action.fromDiscard) state.discardPile.dropLast(1) else state.discardPile
                ActionResult.Success(state.copy(deck = newDeck, discardPile = newDiscard, hands = newHands))
            }
            is GameAction.EndTurn -> {
                val seats = state.hands.keys.sorted()
                val next = seats.firstOrNull { it > action.playerSeat } ?: seats.first()
                ActionResult.Success(state.copy(currentTurn = next))
            }
            else -> ActionResult.Illegal("Action not supported in Free-form.")
        }
    }

    override fun isGameOver(state: GameState) = state.isOver
    override fun scores(state: GameState) = state.scores
    override fun winnerSeats(state: GameState) = state.winnerSeats
}
